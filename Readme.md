# Rlze Bancodigital API

API REST para gerenciamento de contas e movimentações financeiras.

## 🚀 Como Executar o Projeto - via **Docker**

1. Necessário: **Docker** instalado.
2. Clone o repositório e acesse a pasta raiz.
3. Utilize o comando `docker-compose up --build` para subir a aplicação **java** e o banco **MySQL**.
4. Os testes podem ser executados nos endpoints via **Postman**.

## ==> DECISÕES DE DESIGN E ARQUITETURA

### Arquitetura hexagonal 
Optei por uma versão pragmática da Arquitetura Hexagonal para garantir o desacoplamento entre a lógica de negócio e os detalhes de infraestrutura.  

- Domínio Isolado: O núcleo (domain) não conhece frameworks como JPA ou Web. Isso facilita testes unitários puros, rápidos e sem necessidade de subir o contexto do Spring.  
- Portas e Adaptadores: A comunicação com o mundo externo é feita através de interfaces (ports). Se amanhã precisar trocar o MySQL por MongoDB ou enviar notificações via Kafka em vez de Spring, alteramos apenas o adaptador na camada de infrastructure, sem tocar na regra de transferência.  

### Evolução de schema com Flyway  
O uso do Flyway foi adotado para garantir versionamento do banco, preservação de histórico, imutabilidade e consistência de dados no banco.  

### Consistência e Concorrência
Para garantir a integridade dos saldos sem sacrificar a performance com bloqueios pesados no banco de dados, adotei o Locking Otimista:   

- Implementação: atributo version na tabela "contas" e a anotação @Version do JPA na "ContaEntity".  
- Funcionamento: Toda vez que uma conta é lida, o JPA guarda sua versão. No momento do UPDATE, o Hibernate executa um SQL similar a:  
UPDATE contas SET saldo = ?, version = 1 WHERE id = ? AND version = 0;  
- Vantagem: Se dois processos tentarem sacar da mesma conta ao mesmo tempo, o primeiro a chegar incrementará a versão. O segundo processo falhará ao tentar dar o UPDATE (pois a versão 0 não existe mais), lançando uma ObjectOptimisticLockingFailureException. Isso impede o fenômeno de Lost Update (atualização perdida) e garante que o saldo final esteja sempre correto.

### Transacionalidade e Atomicidade
Propriedades ACID: Toda a lógica de transferência é envolvida pela anotação @Transactional. Se o débito na conta de origem for bem-sucedido, mas o crédito na conta de destino falhar (ou o sistema cair), o banco realiza o rollback automático, garantindo que o dinheiro nunca "desapareça".

### Sistema de Notificações  
Para atender ao requisito de notificação pós-transferência, implementei uma solução baseada em Spring Application Events com execução assíncrona (@Async). Dessa forma consigo obter:  
- Desacoplamento de Domínio: O TransferenciaService não conhece o mecanismo de notificação. Ele apenas publica um evento. Isso permite que, no futuro, possamos trocar o listener local por um KafkaProducer sem alterar uma única linha da regra de negócio.  
- Performance: O uso da anotação @Async garante que o thread principal da API não fique bloqueado aguardando o envio da notificação, mantendo a baixa latência da resposta para o usuário.  

Optei por não utilizar uma solução como Kafka ou RabbitMQ visando a evitar aumentar a complexidade (necessidade de mais um container, configuração de tópico, serialização).     

Também poderia ser utilizada uma aplicação de envio de e-mail (SendGrid, Jakarta Mail) ou SMS (ex.: Twilio), o que optei por não fazer em prol da simplicidade. Mas poderia ser atendido através do Listener e de ajuste na camada de gestão de contas, cadastrando o e-mail ou telefone do cliente para enviar um e-mail ou mensagem de texto.  

## ==> DECISÕES DE NEGÓCIO  

Decisões tomadas para fins de simplificação do escopo.  

### Saldo das contas  
Optei por não permitir que uma conta fique com saldo negativo.    

### Consulta de contas  
Optei por criar apenas consulta pelo nome do titular, e permitir também a consulta por todas as contas cadastradas.  

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
├── **application/**                # Casos de Uso (Orquestração)  
│   ├── **dto/**                    # Requests/Responses da API  
│   ├── **ports/**  
│   │   ├── **in/**                 # Interfaces de entrada (Use Cases)  
│   │   └── **out/**                # Interfaces de saída (Gateways/Repositórios)  
│   └── **usecases/**               # Implementação da lógica de aplicação  
│  
├── **domain/**                     # O Coração (Regras de Negócio Puristas)  
│   ├── **model/**                  # Entidades de Domínio (Ricas em comportamento)  
│   ├── **exception/**              # Exceções de negócio (ex: SaldoInsuficienteException)  
│   └── **service/**                # Domain Services (Lógica que envolve múltiplas entidades)  
│  
├── **infrastructure/**             # Detalhes de Implementação (Adaptadores)  
│   ├── **adapters/**  
│   │   ├── **in/web/**             # Controllers REST (Adapter Entrada)  
│   │   └── **out/persistence/**    # Implementação Repositories (Adapter Saída)  
│   │       ├── **entity/**         # Entidades do Banco (JPA)  
│   │       └── **mapper/**         # Conversores (Domain <-> JPA Entity)  
│   ├── **external/**               # Adaptores para Notificações (E-mail, SMS, SNS)  
│   └── **config/**                 # Bean Definitions e Bean Validation  
│
└── BancodigitalApplication.java

## Endpoints Principais
**Método -> URL -> Descrição**  
**POST** ->	http://localhost:8080/v1/contas -> Cria uma conta  
**GET** -> http://localhost:8080/v1/contas?nomeTitular=teste -> Pesquisa contas pelo nome do titular  
**GET** -> http://localhost:8080/v1/contas -> Pesquisa contas (todas)  
**POST** -> http://localhost:8080/v1/transferencias -> Realiza uma transferência entre contas

**Documentação Swagger:** http://localhost:8080/swagger-ui/index.html

## Formato das Requisições/Respostas

### Criar conta

**Request**  
{  
"nomeTitular": "Fabiano Moraes",  
"saldo": 80  
}

### Pesquisar contas

**Response**  
[  
{  
"id": "16e718b4-5449-4ba8-99b7-cad4d1d96d6f",  
"nomeTitular": "Fabiano Moraes",  
"saldo": 80.00  
},  
{  
"id": "506dc261-2730-11f1-9746-8e822b232c7c",  
"nomeTitular": "Joaquim Silveira",  
"saldo": 900.00  
},  
{  
"id": "506dcb41-2730-11f1-9746-8e822b232c7c",  
"nomeTitular": "Bob Silva",  
"saldo": 600.00  
}  
]

### Realizar transferência

**Request**  
{  
"idContaOrigem": "506dc261-2730-11f1-9746-8e822b232c7",  
"idContaDestino": "506dcb41-2730-11f1-9746-8e822b232c7c",  
"valor": 10  
}

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

Testes de performance (com JMeter, por exemplo), quantificando eventual necessidade de ajustes de arquitetura e escalabilidade.  

Observabilidade e monitoramento: métricas padrão via actuator, métricas customizadas, logs com correlation ID. Através do uso de ferramentas como Prometheus, Grafana e Opentelemetry.  

---
Desenvolvido por Fernando Cardoso.
