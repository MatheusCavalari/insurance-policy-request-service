# Insurance Policy Request Service

Microsserviço orientado a eventos para gerenciamento do ciclo de vida de solicitações de apólice de seguros.

## 1. Visão geral

Este projeto implementa o serviço de **solicitações de apólice** do desafio da seguradora ACME. A solução recebe solicitações via API REST, persiste os dados em PostgreSQL, consulta uma API de fraude mockada para classificação de risco, aplica regras de validação conforme o perfil do cliente, processa eventos externos de pagamento e subscrição, permite cancelamento quando aplicável e publica eventos a cada alteração de estado.

A solução foi construída com foco em:

- Clean Architecture
- Event-Driven Architecture
- baixo acoplamento entre domínio e infraestrutura
- observabilidade
- testes de unidade e integração
- consistência entre persistência e publicação de eventos via outbox

---

## 2. Escopo da solução

A solução implementa os seguintes comportamentos principais:

- recebimento de solicitações de apólice via REST
- persistência em banco de dados
- consulta por `requestId` e `customerId`
- integração com API de fraude via mock server
- processamento de eventos de pagamento e subscrição
- cancelamento de solicitações
- transições de estado conforme ciclo de vida
- publicação de eventos de mudança de estado
- observabilidade com healthchecks, métricas e logs
- testes automatizados com Mockito e Testcontainers

---

## 3. Arquitetura da solução

### 3.1 Estilo arquitetural

A solução adota uma combinação de:

- **Clean Architecture**
- **DDD tático leve**
- **EDA (Event-Driven Architecture)**

Estrutura principal de pacotes:

- `domain`: regras de negócio, estados, estratégias, portas
- `application`: casos de uso / serviços de aplicação
- `infrastructure`: persistência, mensageria, observabilidade, integração externa
- `interfaces`: controllers REST e consumers SQS

### 3.2 Diagrama de componentes

```mermaid
flowchart LR
    Client[Client / Postman / Curl]
    API[Insurance Policy Request Service]
    DB[(PostgreSQL)]
    FRAUD[WireMock Fraud API]
    SQS[(LocalStack SQS)]
    OUTBOX[(Outbox Table)]

    Client --> API
    API --> DB
    API --> FRAUD
    API --> OUTBOX
    API --> SQS
    SQS --> API
```

### 3.3 Fluxo principal

```mermaid
sequenceDiagram
    participant C as Cliente
    participant A as API
    participant D as PostgreSQL
    participant F as Fraud API
    participant O as Outbox
    participant Q as SQS
    participant E as Eventos externos

    C->>A: POST /policy-requests
    A->>D: salva solicitação (RECEIVED)
    A->>O: grava evento POLICY_REQUEST_RECEIVED
    A-->>C: 201 Created

    A->>Q: scheduler publica outbox
    Q->>A: consume POLICY_REQUEST_RECEIVED
    A->>F: consulta fraude
    F-->>A: classificação de risco
    A->>D: atualiza VALIDATED / PENDING / REJECTED
    A->>O: grava evento de alteração de estado

    E->>Q: payment / underwriting event
    Q->>A: consumer processa evento
    A->>D: atualiza status externo + reconcilia estado final
    A->>O: grava novo evento de status
```

### 3.4 Máquina de estados

```mermaid
stateDiagram-v2
    [*] --> RECEIVED
    RECEIVED --> VALIDATED
    RECEIVED --> REJECTED
    RECEIVED --> CANCELED

    VALIDATED --> PENDING

    PENDING --> PENDING
    PENDING --> APPROVED
    PENDING --> REJECTED
    PENDING --> CANCELED

    APPROVED --> [*]
    REJECTED --> [*]
    CANCELED --> [*]
```

### 3.5 Estrutura por camadas

```mermaid
flowchart TB
    subgraph Interfaces
      REST[REST Controllers]
      CONSUMERS[SQS Consumers]
    end

    subgraph Application
      USECASES[Use Cases / Services]
    end

    subgraph Domain
      MODEL[PolicyRequest Aggregate]
      RULES[Risk Strategies]
      PORTS[Ports]
    end

    subgraph Infrastructure
      PERSIST[Repositories / JPA]
      MSG[Publisher / Scheduler / Outbox]
      EXT[Fraud Gateway]
      OBS[Health / Metrics / Logging]
    end

    REST --> USECASES
    CONSUMERS --> USECASES
    USECASES --> MODEL
    USECASES --> PORTS
    PORTS --> PERSIST
    PORTS --> MSG
    PORTS --> EXT
```

---

## 4. Decisões arquiteturais

### 4.1 REST para entrada e consulta
A entrada principal do domínio é uma API REST para criação, consulta e cancelamento.

### 4.2 Mensageria para fluxos assíncronos
Pagamento, subscrição, análise assíncrona e publicação de mudanças de estado são tratados via filas SQS (LocalStack em ambiente local).

### 4.3 Outbox Pattern
Toda alteração relevante de estado gera um registro em outbox. Um scheduler publica os eventos pendentes. Isso reduz o risco de inconsistência entre banco e mensageria.

### 4.4 Idempotência
Eventos externos de pagamento e subscrição são protegidos por uma tabela de mensagens processadas, evitando reprocessamento por `eventId`.

### 4.5 Strategy Pattern
As regras de validação por classificação de risco foram isoladas em estratégias (`REGULAR`, `HIGH_RISK`, `PREFERRED`, `NO_INFORMATION`).

### 4.6 Aggregate rico
O aggregate `PolicyRequest` centraliza transições de estado, histórico e reconciliação de status assíncronos.

### 4.7 WireMock para fraude
A API de fraude não foi implementada como serviço real. O comportamento foi simulado com WireMock, conforme orientação do desafio.

---

## 5. Premissas adotadas

- O escopo do projeto cobre **somente** o serviço de solicitações de apólice.
- A emissão da apólice foi representada pelo estado final `APPROVED`.
- O mock da fraude foi dirigido por `customerId`, facilitando cenários determinísticos em testes manuais.
- O campo principal de validação das regras de risco é o `insuredAmount`.
- Solicitações em estado `APPROVED` ou `REJECTED` não podem mais ser canceladas.
- Mensagens externas usam JSON.
- Para testes manuais no Windows, o envio para o SQS foi documentado usando `docker cp` + `file://`, evitando problemas de escaping no terminal.
- Os campos persistidos foram mantidos compatíveis com o escopo funcional do desafio.
- O serviço publica eventos de alteração de estado para permitir notificação e encadeamento com outros serviços do ecossistema.

---

## 6. Tecnologias utilizadas

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Flyway
- PostgreSQL
- Spring Cloud AWS SQS
- LocalStack
- WireMock
- Micrometer
- Prometheus Registry
- JUnit 5
- Mockito
- Testcontainers

---

## 7. Como executar o projeto

### 7.1 Pré-requisitos

- JDK 17
- Maven
- Docker Desktop
- PowerShell ou terminal compatível
- Curl, Postman ou Insomnia

### 7.2 Subir infraestrutura local

```bash
docker compose up -d postgres localstack wiremock
```

### 7.3 Criar filas no LocalStack

```bash
docker exec insurance-localstack awslocal sqs create-queue --queue-name payment-processed-queue
docker exec insurance-localstack awslocal sqs create-queue --queue-name underwriting-processed-queue
docker exec insurance-localstack awslocal sqs create-queue --queue-name policy-request-received-queue
docker exec insurance-localstack awslocal sqs create-queue --queue-name policy-status-changed-queue
```

Opcional: listar filas

```cmd
docker exec insurance-localstack awslocal sqs list-queues
```

### 7.4 Subir a aplicação

```cmd
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

### 7.5 Smoke test inicial

```cmd
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

---

## 8. Infraestrutura local

O `docker-compose.yml` sobe os seguintes componentes:

- **postgres**: persistência do serviço
- **localstack**: broker local com SQS
- **wiremock**: mock server da API de fraude
- **app**: aplicação Spring Boot

---

## 9. Ciclo de vida da solicitação

Estados suportados:

- `RECEIVED`
- `VALIDATED`
- `PENDING`
- `APPROVED`
- `REJECTED`
- `CANCELED`

Resumo do fluxo:

1. criação via REST -> `RECEIVED`
2. processamento assíncrono da fraude
3. se aprovado nas regras -> `VALIDATED` e depois `PENDING`
4. se reprovado nas regras -> `REJECTED`
5. se pagamento e subscrição aprovarem -> `APPROVED`
6. se pagamento ou subscrição negarem -> `REJECTED`
7. cancelamento permitido até o encerramento final

---

## 10. Regras por classificação de risco

### REGULAR
- vida ou residencial: até `500000`
- auto: até `350000`
- demais categorias: até `255000`

### HIGH_RISK
- auto: até `250000`
- residencial: até `150000`
- demais categorias: até `125000`

### PREFERRED
- vida: menor que `800000`
- auto e residencial: menor que `450000`
- demais categorias: até `375000`

### NO_INFORMATION
- vida ou residencial: até `200000`
- auto: até `75000`
- demais categorias: até `55000`

---

## 11. Endpoints REST

### 11.1 Criar solicitação

`POST /policy-requests`

Exemplo:

```bash
curl --request POST   --url http://localhost:8080/policy-requests   --header 'Content-Type: application/json'   --data '{
    "customerId": "22222222-2222-2222-2222-222222222222",
    "productId": 123,
    "category": "AUTO",
    "salesChannel": "MOBILE",
    "paymentMethod": "CREDIT_CARD",
    "totalMonthlyPremiumAmount": 75.25,
    "insuredAmount": 200000.00,
    "coverages": {
      "Roubo": 100000.00,
      "Perda Total": 100000.00
    },
    "assistances": ["Guincho 24h", "Chaveiro"]
  }'
```

Resposta esperada:

```json
{
  "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "RECEIVED",
  "createdAt": "2026-03-15T03:47:04.934248Z"
}
```

### 11.2 Consultar por id

`GET /policy-requests/{requestId}`

```bash
curl http://localhost:8080/policy-requests/{requestId}
```

### 11.3 Consultar por customerId

`GET /policy-requests?customerId={customerId}`

```bash
curl "http://localhost:8080/policy-requests?customerId=22222222-2222-2222-2222-222222222222"
```

### 11.4 Cancelar solicitação

`POST /policy-requests/{requestId}/cancel`

```bash
curl --request POST http://localhost:8080/policy-requests/{requestId}/cancel
```

---

## 12. Integração com fraude

A classificação de risco é simulada pelo WireMock.

Exemplos de cenários usados:

- `22222222-2222-2222-2222-222222222222` -> `REGULAR`
- `33333333-3333-3333-3333-333333333333` -> `HIGH_RISK`
- `44444444-4444-4444-4444-444444444444` -> `PREFERRED`
- `55555555-5555-5555-5555-555555555555` -> `NO_INFORMATION`

Teste direto do mock:

```bash
curl "http://localhost:8089/fraud-analysis/test-id?customerId=33333333-3333-3333-3333-333333333333"
```

---

## 13. Mensageria e eventos

### 13.1 Filas utilizadas

- `payment-processed-queue`
- `underwriting-processed-queue`
- `policy-request-received-queue`
- `policy-status-changed-queue`

### 13.2 Método escolhido para testes manuais

Para reproduzir os testes de mensageria de forma confiável no Windows, os payloads foram salvos em arquivos `.json` e enviados com:

1. `docker cp` para dentro do container do LocalStack
2. `awslocal sqs send-message --message-body file:///...`

### 13.3 Exemplo de payload de pagamento aprovado

Arquivo `manual-tests/payment-approved.json`:

```json
{
  "eventId": "33333333-3333-3333-3333-333333333333",
  "requestId": "REQUEST_ID",
  "status": "APPROVED",
  "occurredAt": "2026-03-15T04:20:00Z"
}
```

Envio:

```bash
docker cp ./manual-tests/payment-approved.json insurance-localstack:/tmp/payment-approved.json
docker exec insurance-localstack awslocal sqs send-message   --queue-url http://localhost:4566/000000000000/payment-processed-queue   --message-body file:///tmp/payment-approved.json
```

### 13.4 Exemplo de payload de pagamento negado

```json
{
  "eventId": "55555555-5555-5555-5555-555555555555",
  "requestId": "REQUEST_ID",
  "status": "DENIED",
  "occurredAt": "2026-03-15T04:40:00Z"
}
```

### 13.5 Exemplo de payload de subscrição aprovada

```json
{
  "eventId": "44444444-4444-4444-4444-444444444444",
  "requestId": "REQUEST_ID",
  "status": "APPROVED",
  "occurredAt": "2026-03-15T04:21:00Z"
}
```

Envio:

```bash
docker cp ./manual-tests/underwriting-approved.json insurance-localstack:/tmp/underwriting-approved.json
docker exec insurance-localstack awslocal sqs send-message   --queue-url http://localhost:4566/000000000000/underwriting-processed-queue   --message-body file:///tmp/underwriting-approved.json
```

### 13.6 Exemplo de payload de subscrição negada

```json
{
  "eventId": "66666666-6666-6666-6666-666666666666",
  "requestId": "REQUEST_ID",
  "status": "DENIED",
  "occurredAt": "2026-03-15T04:45:00Z"
}
```

### 13.7 Fluxos testados manualmente

- REGULAR -> `PENDING`
- HIGH_RISK acima do limite -> `REJECTED`
- pagamento `DENIED` -> `REJECTED`
- subscrição `DENIED` -> `REJECTED`
- pagamento `APPROVED` + subscrição `APPROVED` -> `APPROVED`
- idempotência por `eventId`
- cancelamento -> `CANCELED`

---

## 14. Observabilidade

### 14.1 Healthchecks

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/outbox
curl http://localhost:8080/actuator/health/sqsQueues
```

### 14.2 Métricas

```bash
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/metrics/insurance.outbox.pending
curl http://localhost:8080/actuator/metrics/insurance.outbox.failed
curl http://localhost:8080/actuator/prometheus
```

### 14.3 Logs

A aplicação adiciona `X-Correlation-Id` em cada request/resposta HTTP e propaga esse identificador para os logs usando MDC.

Exemplo:

```bash
curl -i http://localhost:8080/actuator/health -H "X-Correlation-Id: demo-123"
```

---

## 15. Testes

### 15.1 Unitários
- máquina de estados
- estratégias de risco
- casos de uso
- consumers
- outbox factory
- scheduler
- publisher
- tratamento de erro
- health indicators
- parser de eventos SQS

### 15.2 Integração
- persistência com PostgreSQL via Testcontainers
- repositórios principais
- outbox
- mensagens processadas

### 15.3 Execução

Todos os testes:

```cmd
.\mvnw.cmd test
```

Exemplos específicos:

```cmd
.\mvnw.cmd -Dtest=PolicyRequestRepositoryIT test
.\mvnw.cmd -Dtest=OutboxEventRepositoryIT test
.\mvnw.cmd -Dtest=ProcessedMessageRepositoryIT test
.\mvnw.cmd -Dtest=GlobalExceptionHandlerTest test
```

---

## 16. Validação dos fluxos

---

## 17. Melhorias futuras

- tracing distribuído
- dashboards Grafana/Prometheus
- autenticação e autorização
- CI/CD
- API versioning

---

## 18. Conclusão

A solução foi construída com foco em clareza arquitetural, testabilidade, observabilidade e reprodutibilidade. O projeto entrega o fluxo de ponta a ponta do serviço de solicitações de apólice, cobrindo criação, validação por fraude, processamento assíncrono, cancelamento, publicação de eventos e documentação de execução e demonstração.
