# Digital Wallet API

API completa para gestão de contas bancárias com suporte a depósitos, saques, transferências com taxa de 1% e histórico de transações.

## Tecnologias

- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Data JPA + Hibernate**
- **PostgreSQL 15**
- **Bucket4j** (rate limiting)
- **Testcontainers** (testes integrados com PostgreSQL real)
- **Springdoc OpenAPI** (Swagger UI)

## Como rodar

### Com Docker Compose (recomendado)

```bash
# 1. Clone o repositório
git clone <url>
cd digitalwallet

# 2. Configure variáveis de ambiente (opcional)
cp .env.example .env

# 3. Suba tudo com Docker
docker-compose up --build

# 4. Acesse
# Frontend: http://localhost:8080
# Swagger:  http://localhost:8080/swagger-ui.html
```

### Só o banco (desenvolvimento local)

```bash
# Sobe apenas o PostgreSQL
docker-compose up postgres-db

# Roda a aplicação localmente
./mvnw spring-boot:run
```

## 🧪 Testes

```bash
# Requer Docker rodando (Testcontainers sobe o PostgreSQL automaticamente)
./mvnw test
```

Os testes cobrem:
- Transferência com taxa de 1% aplicada corretamente
- Saldo insuficiente lança exceção
- Transferência para a mesma conta é rejeitada
- Conta inexistente lança exceção
- Race condition com pessimistic locking (10 threads simultâneas)

## 📡 Endpoints da API

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/contas` | Cria uma nova conta |
| `GET` | `/api/contas/{id}` | Consulta saldo da conta |
| `POST` | `/api/contas/{id}/deposito` | Realiza depósito |
| `POST` | `/api/contas/{id}/saque` | Realiza saque |
| `GET` | `/api/contas/{id}/extrato` | Histórico de transações |
| `POST` | `/api/transferencias` | Transfere entre contas (+ 1% taxa) |

### Exemplos

```bash
# Criar conta com R$ 100,00
curl -X POST http://localhost:8080/api/contas \
  -H 'Content-Type: application/json' \
  -d '{"saldoInicialEmCentavos": 10000}'

# Depositar R$ 50,00 na conta 1
curl -X POST http://localhost:8080/api/contas/1/deposito \
  -H 'Content-Type: application/json' \
  -d '{"valorEmCentavos": 5000}'

# Transferir R$ 30,00 da conta 1 para a conta 2
curl -X POST http://localhost:8080/api/transferencias \
  -H 'Content-Type: application/json' \
  -d '{"origemId": 1, "destinoId": 2, "valorEmCentavos": 3000}'
```

> **Nota:** todos os valores são em centavos. R$ 1,00 = 100 centavos.

## 🔒 Segurança

- **Pessimistic locking** nas transferências para evitar race conditions
- **Rate limiting** via Bucket4j (60 req/min por IP)
- Transações com `@Transactional` para atomicidade

## 🌐 Deploy

Para colocar no ar, use qualquer plataforma que suporte Docker:

- **Railway** → conecte o repositório, adicione PostgreSQL addon
- **Render** → use `docker-compose.yml` ou Dockerfile
- **Fly.io** → `fly launch` com Dockerfile
- **AWS/GCP/Azure** → use container registry + managed PostgreSQL
