# Rlze Bancodigital API

API REST para gerenciamento de contas e movimentações financeiras.

## 🚀 Como Executar o Projeto - via **Docker**

1. Necessário: **Docker** instalado.
2. Clone o repositório e acesse a pasta raiz.
3. Utilize o comando `docker-compose up --build` para subir a aplicação **java** e o banco **MySQL**.  
4. A API estará disponível em A API estará disponível em http://localhost:8080, e o banco MySQL na porta 3032.  
5. Os testes podem ser executados nos endpoints via **Postman** ou no **swagger** (http://localhost:8080/swagger-ui/index.html).  
6. Para visualizar as notificações e logs, é necessário acompanhar os **logs da aplicação no Docker**.

## 🏗️ Decisões de design e arquitetura  

### Arquitetura hexagonal 
Optei por uma versão pragmática da Arquitetura Hexagonal para garantir o desacoplamento entre a lógica de negócio e os detalhes de infraestrutura.  

- **Domínio Isolado:** O núcleo (domain) não conhece frameworks como JPA ou Web. Isso facilita testes unitários puros, rápidos e sem necessidade de subir o contexto do Spring.  
- **Portas e Adaptadores:** A comunicação com o mundo externo é feita através de interfaces (ports). Se amanhã precisar trocar o MySQL por MongoDB ou enviar notificações via Kafka em vez de Spring, alteramos apenas o adaptador na camada de infrastructure, sem tocar na regra de transferência.      

src/main/java/rlze/bancodigitalapi/  
├── **application/** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;# Use Cases e Serviços (Regras de Aplicação)  
├── **domain/** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;# Modelos de Domínio, Exceções e Eventos  
└── **infrastructure/** &nbsp;&nbsp;# Adaptadores de entrada (Web) e saída (Persistence/Config)  

### Evolução de schema com Flyway  
O uso do *Flyway* foi adotado para garantir versionamento do banco, preservação de histórico, imutabilidade e consistência de dados, além da carga inicial de contas.  

### Consistência e Concorrência
Para garantir a integridade dos saldos sem sacrificar a performance com bloqueios pesados no banco de dados, adotei o Locking Otimista:   

- **Implementação:** atributo version na tabela "contas" e a anotação @Version do JPA na "ContaEntity".  
- **Funcionamento:** Toda vez que uma conta é lida, o JPA guarda sua versão. No momento do UPDATE, o Hibernate executa um SQL similar a:  
UPDATE contas SET saldo = ?, version = 1 WHERE id = ? AND version = 0;  
- **Vantagem:** Se dois processos tentarem sacar da mesma conta ao mesmo tempo, o primeiro a chegar incrementará a versão. O segundo processo falhará ao tentar dar o UPDATE (pois a versão 0 não existe mais), lançando uma ObjectOptimisticLockingFailureException. Isso impede o fenômeno de Lost Update (atualização perdida) e garante que o saldo final esteja sempre correto.

### Transacionalidade e Atomicidade
**Propriedades ACID:** Toda a lógica de transferência é envolvida pela anotação @Transactional. Se o débito na conta de origem for bem-sucedido, mas o crédito na conta de destino falhar (ou o sistema cair), é executado o rollback automático.

### Sistema de Notificações  
Para atender ao requisito de notificação pós-transferência, implementei uma solução baseada em Spring Application Events com execução assíncrona (@Async). Dessa forma consigo obter:  
- **Desacoplamento de Domínio:** O TransferenciaService não conhece o mecanismo de notificação. Ele apenas publica um evento. Isso permite que, no futuro, possamos trocar o listener local por um KafkaProducer sem alterar uma única linha da regra de negócio.  
- **Performance:** O uso da anotação @Async garante que o thread principal da API não fique bloqueado aguardando o envio da notificação, mantendo a baixa latência da resposta para o usuário.  

Optei por não utilizar uma solução como Kafka ou RabbitMQ visando a evitar aumentar a complexidade (necessidade de mais um container, configuração de tópico, serialização).  

Também poderia ser utilizada uma aplicação de envio de e-mail (SendGrid, Jakarta Mail) ou SMS (ex.: Twilio), o que optei por não fazer em prol da simplicidade. Mas poderia ser atendido através do Listener e de ajuste na camada de gestão de contas, cadastrando o e-mail ou telefone do cliente para enviar um e-mail ou mensagem de texto.  

### Padronização de erros  
Mensagens e retorno padronizados (detalhados no swagger):  
&nbsp;&nbsp;**404 Not Found**: Recurso inexistente.  
&nbsp;&nbsp;**422 Unprocessable Entity**: Erros de regra de negócio (Ex: saldo insuficiente).  
&nbsp;&nbsp;**400 Bad Request**: Parâmetros inválidos ou JSON malformado.  
&nbsp;&nbsp;**409 Conflict**: Conflito de atualização simultânea.  

## 📋 Decisões de negócio  

Decisões tomadas para fins de simplificação do escopo.  

### Saldo das contas  
Optei por não permitir que uma conta fique com saldo negativo.    

### Consulta de contas  
Optei por criar apenas consulta pelo nome do titular, e permitir também a consulta por todas as contas cadastradas.

### Envio da notificação  
Optei por não incluir envio de mensagem ou e-mail para o cliente ao realizar uma transferência. Ao invés disso usei a estrutura de evento para simular uma mensagem via saída de texto da aplicação.

## 🛠️ Tecnologias

Java 21+  
Spring Boot 3.5.11  
Spring Data JPA  
MySQL 8 (via Docker)  
Flyway
Lombok  
Maven

## 📍 Endpoints Principais
**Método -> URL -> Descrição**  
**POST** ->	http://localhost:8080/v1/contas &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-> Cria uma conta  
**GET** -> &nbsp;&nbsp;http://localhost:8080/v1/contas?nomeTitular=teste &nbsp;-> Pesquisa contas pelo nome do titular  
**GET** -> &nbsp;&nbsp;http://localhost:8080/v1/contas &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-> Pesquisa contas (todas)  
**POST** -> http://localhost:8080/v1/transferencias &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-> Realiza uma transferência entre contas

**Documentação Swagger:** http://localhost:8080/swagger-ui/index.html

## 🔗 Formato das Requisições/Respostas

### Criar conta

**Request**  
{  
&nbsp;&nbsp;&nbsp;&nbsp;"nomeTitular": "Fabiano Moraes",  
&nbsp;&nbsp;&nbsp;&nbsp;"saldo": 80  
}

### Pesquisar contas

**Response**  
[  
&nbsp;{  
&nbsp;&nbsp;&nbsp;&nbsp;"id": "16e718b4-5449-4ba8-99b7-cad4d1d96d6f",  
&nbsp;&nbsp;&nbsp;&nbsp;"nomeTitular": "Fabiano Moraes",  
&nbsp;&nbsp;&nbsp;&nbsp;"saldo": 80.00  
&nbsp;},  
&nbsp;{  
&nbsp;&nbsp;&nbsp;&nbsp;"id": "506dc261-2730-11f1-9746-8e822b232c7c",  
&nbsp;&nbsp;&nbsp;&nbsp;"nomeTitular": "Joaquim Silveira",  
&nbsp;&nbsp;&nbsp;&nbsp;"saldo": 900.00  
&nbsp;},  
&nbsp;{  
&nbsp;&nbsp;&nbsp;&nbsp;"id": "506dcb41-2730-11f1-9746-8e822b232c7c",  
&nbsp;&nbsp;&nbsp;&nbsp;"nomeTitular": "Bob Silva",  
&nbsp;&nbsp;&nbsp;&nbsp;"saldo": 600.00  
&nbsp;}  
]

### Realizar transferência

**Request**  
{  
&nbsp;&nbsp;&nbsp;&nbsp;"idContaOrigem": "506dc261-2730-11f1-9746-8e822b232c7",  
&nbsp;&nbsp;&nbsp;&nbsp;"idContaDestino": "506dcb41-2730-11f1-9746-8e822b232c7c",  
&nbsp;&nbsp;&nbsp;&nbsp;"valor": 10  
}

## 📖 Passo a passo da implementação:

1 - Criação do repositório no github, com initial commit simulando scaffolding básico com configuração de banco e estrutura de pastas.  

2 - Implementação inicial do escopo de CONTA  
&nbsp;&nbsp;-> Flyway para tabela Contas e registros iniciais  
&nbsp;&nbsp;-> Controllers, Services e repositories (ports e adapters): consultar contas e criar nova conta  

3 - Implementação inicial do escopo de transferência entre contas  
&nbsp;&nbsp;-> Controllers, Services e repositories (ports e adapters): debitar, creditar, realizar transferência  

4 - Implementação inicial sistema de notificação de transferências  
&nbsp;&nbsp;-> Spring Application event (notifier e listener)  

5 - Testes unitários  
&nbsp;&nbsp;-> Testes básicos nas classes usecase (conta e transferencia) e model (conta)  

6 - Documentação swagger  

7 - Logs básicos  
&nbsp;&nbsp;-> Camada de controller, service e erros  

8 - Teste end2end  

## 📡 Melhorias futuras consideradas

- Notificação via e-mail ou mensagem para o titular da conta.
- Maior cobertura de testes unitários.
- Análise estática de código (com SonarQube, por exemplo).
- Testes de performance (com JMeter ou K6, por exemplo), quantificando eventual necessidade de ajustes de arquitetura e escalabilidade.
- Observabilidade e monitoramento: métricas padrão via actuator, métricas customizadas, logs com correlation ID. Através do uso de ferramentas como Prometheus, Grafana e Opentelemetry.  

---
Desenvolvido por Fernando Cardoso.
