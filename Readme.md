# Rlze Bancodigital API

API REST para gerenciamento de contas e movimentações financeiras.

## Tecnologias

Java 21+  
Spring Boot 3.5.11  
Spring Data JPA  
MySQL 8 (via Docker)  
Lombok  
Maven

## Estrutura do Projeto
src/main/java/com/bancodigital/  
│  
├── application/                # Casos de Uso (Orquestração)  
│   ├── dto/                    # Requests/Responses da API  
│   ├── ports/  
│   │   ├── in/                 # Interfaces de entrada (Use Cases)  
│   │   └── out/                # Interfaces de saída (Gateways/Repositórios)  
│   └── usecases/               # Implementação da lógica de aplicação  
│  
├── domain/                     # O Coração (Regras de Negócio Puristas)  
│   ├── model/                  # Entidades de Domínio (Ricas em comportamento)  
│   ├── exception/              # Exceções de negócio (ex: SaldoInsuficienteException)  
│   └── service/                # Domain Services (Lógica que envolve múltiplas entidades)  
│  
├── infrastructure/             # Detalhes de Implementação (Adaptadores)  
│   ├── adapters/  
│   │   ├── in/web/             # Controllers REST (Adapter Entrada)  
│   │   └── out/persistence/    # Implementação Repositories (Adapter Saída)  
│   │       ├── entity/         # Entidades do Banco (JPA)  
│   │       └── mapper/         # Conversores (Domain <-> JPA Entity)  
│   ├── external/               # Adaptores para Notificações (E-mail, SMS, SNS)  
│   └── config/                 # Bean Definitions e Bean Validation  
│
└── BancodigitalApplication.java

## 🚀 Como Executar o Projeto - via **Docker**

1. Necessário: **Docker** instalado.
2. Clone o repositório e acesse a pasta raiz.
3. Utilize o comando `docker-compose up --build` para subir a aplicação **java** e o banco **MySQL**.
4. Os testes podem ser executados nos endpoints via **Postman**.

## Endpoints Principais
Método ->	URL -> Descrição  
POST ->	-> 
POST ->	 ->	 
POST ->  ->  
GET ->  ->	 
GET ->  ->	

## Formato das Requisições/Respostas
### Exemplo 1

Request  
{  
"nome": "aaa",  
"descricao": "aaa"  
}

Response
{  
"id": 1,  
"nome": "aa",  
"descricao": "aa"
}

### Resultado Detalhado da Pauta

Response  
{  
"pautaId": 1,  
"totalSim": 5,  
"totalNao": 3,  
"totalVotos": 8,  
"resultado": "SIM",  
"sessoesAbertas": false,  
"resultadosPorSessao": [  
{  
"sessaoId": 1,  
"totalSim": 3,  
"totalNao": 2,  
"resultado": "SIM",  
"aberta": false  
},  
{  
"sessaoId": 2,  
"totalSim": 2,  
"totalNao": 1,  
"resultado": "SIM",  
"aberta": false  
}  
]}

## ==> DECISÕES DE DESIGN E ARQUITETURA

### Arquitetura hexagonal 
Optei por uma versão pragmática da Arquitetura Hexagonal para garantir o desacoplamento entre a lógica de negócio e os detalhes de infraestrutura.  

- Domínio Isolado: O núcleo (domain) não conhece frameworks como JPA ou Web. Isso facilita testes unitários puros, rápidos e sem necessidade de subir o contexto do Spring.  
- Portas e Adaptadores: A comunicação com o mundo externo é feita através de interfaces (ports). Se amanhã decidirmos trocar o MySQL por MongoDB ou enviar notificações via Kafka em vez de e-mail, alteramos apenas o adaptador na camada de infrastructure, sem tocar na regra de transferência.  

### Evolução de schema com Flyway  
O uso do Flyway foi adotado para:  

- Versionamento de Banco: Garantir que todos os ambientes (Desenvolvimento, Teste, Produção) estejam na mesma versão do schema.
- Imutabilidade: Scripts de migração (V1, V2...) garantem que o histórico de mudanças seja preservado, evitando o uso de ddl-auto: update, que é perigoso em ambientes produtivos.

### Consistência e Concorrência
Para garantir a integridade dos saldos sem sacrificar a performance com bloqueios pesados no banco de dados, adotei o Locking Otimista:   

Implementação: Utilize Atributo version (INT) na tabela contas e a anotação @Version do JPA na ContaEntity.  

Funcionamento: Toda vez que uma conta é lida, o JPA guarda sua versão. No momento do UPDATE, o Hibernate executa um SQL similar a:  
UPDATE contas SET saldo = ?, version = 1 WHERE id = ? AND version = 0;  

Vantagem: Se dois processos tentarem sacar da mesma conta ao mesmo tempo, o primeiro a chegar incrementará a versão. O segundo processo falhará ao tentar dar o UPDATE (pois a versão 0 não existe mais), lançando uma ObjectOptimisticLockingFailureException. Isso impede o fenômeno de Lost Update (atualização perdida) e garante que o saldo final esteja sempre correto.

### Transacionalidade e Atomicidade
Propriedades ACID: Toda a lógica de transferência é envolvida pela anotação @Transactional. Se o débito na conta de origem for bem-sucedido, mas o crédito na conta de destino falhar (ou o sistema cair), o banco realiza o rollback automático, garantindo que o dinheiro nunca "desapareça".

### Sistema de Notificações  
Para atender ao requisito de notificação pós-transferência, implementei uma solução baseada em Spring Application Events com execução assíncrona (@Async).  

Decisão de Arquitetura: Por que não Kafka ou RabbitMQ?  
Embora mensageria externa (como Kafka ou RabbitMQ) seja o padrão para sistemas de larga escala, para este desafio optei pelo Spring Events pelos seguintes motivos:  
Redução de Sobrecarga (YAGNI): Introduzir um broker de mensagens externo apenas para uma notificação simples aumentaria a complexidade de infraestrutura desnecessariamente (mais um container Docker, configuração de tópicos, serialização, etc.).  
Consistência dentro da JVM: O Spring Events permite que a notificação seja disparada imediatamente após o commit da transação, mantendo o rastro de execução dentro da mesma aplicação.  
Desacoplamento de Domínio: O TransferenciaService não conhece o mecanismo de notificação. Ele apenas publica um evento. Isso permite que, no futuro, possamos trocar o listener local por um KafkaProducer sem alterar uma única linha da regra de negócio.  
Performance: O uso da anotação @Async garante que o thread principal da API não fique bloqueado aguardando o envio da notificação, mantendo a baixa latência da resposta para o usuário.  


## ==> JOURNAL (passo a passo da implementação):

1 - Criação do repositório no github, com initial commit simulando scaffolding básico com configuração de banco e estrutura de pastas.  

2 - Implementação inicial do escopo de CONTA  
-> Flyway para tabela Contas e registros iniciais  
-> Controllers, Services e repositories (ports e adapters): consultar contas e criar nova conta  

3 - Implementação inicial do escopo de transferência entre contas
-> Controllers, Services e repositories (ports e adapters): debitar, creditar, realizar transferência  

4 - Implementação inicial sistema de notificação de transferências  
-> Spring Application event (notifier e listener)  

5 - Testes unitários
-> Testes básicos nas classes usecase (conta e transferencia) e model (conta)  

6 - Documentação swagger  

7 - Logs básicos  

8 - Teste end2end  
-> ajustes códigos HTTP de retorno em cenários de erro  



## Melhorias futuras recomendadas

Maior cobertura de testes unitários.  
Análise estática de código (com SonarQube, por exemplo).  
Testes de performance (com JMeter, por exemplo), quantificando a necessidade de ajustes de arquitetura e escalabilidade citados no próximo item.  
Observabilidade e monitoramento: métricas padrão via actuator, métricas customizadas, logs com correlation ID. Através do uso de ferramentas como Prometheus, Grafana e Opentelemetry.

## Validações / Exceções

Campos obrigatórios não podem ser nulos ou vazios.

## Observações

Configurações de URLs e porta podem ser ajustadas via application.yml.  
Segurança das APIs foi abstraída para fins de teste.  
Todos os endpoints usam JSON para entrada e saída.  
A aplicação pode ser testada com Postman ou qualquer cliente HTTP.

---
Desenvolvido por Fernando Cardoso.
