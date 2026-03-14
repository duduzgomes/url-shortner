# URL Shortener

Sistema de encurtamento de URLs de alta disponibilidade construído com Java e Spring Boot. O projeto implementa os principais desafios de arquitetura de sistemas distribuídos, como geração de identificadores únicos em ambiente distribuído, cache inteligente, rate limiting, load balancing, e observabilidade completa.

Dado uma URL longa, o sistema gera uma URL encurtada única. Ao acessar a URL encurtada, o usuário é redirecionado para a URL original.

---

## Requisitos do Sistema

### Funcionais

- Encurtar uma URL longa gerando um short code único
- Redirecionar o usuário para a URL original ao acessar a URL encurtada

### Não Funcionais

| Requisito                       | Valor                   |
| ------------------------------- | ----------------------- |
| URLs geradas por dia            | 100 milhões             |
| Proporção leitura/escrita       | 10:1                    |
| Retenção dos dados              | 10 anos                 |
| Disponibilidade                 | 24x7                    |
| Charset permitido no short code | Base 62 (0-9, a-z, A-Z) |
| Tamanho máximo do short code    | 7 caracteres            |

---

## Tecnologias

| Tecnologia                  | Versão  | Uso                             |
| --------------------------- | ------- | ------------------------------- |
| Java                        | 21      | Linguagem principal             |
| Spring Boot                 | 4.0.3   | Framework base                  |
| Spring Data Cassandra       | 5.0.3   | Persistência                    |
| Spring Data Redis (Lettuce) | 4.0.3   | Cache e geração de IDs          |
| Apache Cassandra            | 5.0     | Banco de dados principal        |
| Redis                       | 7.2     | Cache e contador de IDs         |
| HashIds                     | 1.0.3   | Encoding Base 62 com ofuscação  |
| Resilience4j                | 2.2.0   | Rate Limiting                   |
| Micrometer                  | 1.15.0  | Coleta de métricas              |
| Springdoc OpenAPI           | 2.8.5   | Documentação Swagger            |
| Nginx                       | 1.29    | Load Balancer                   |
| Prometheus                  | 1.4.3   | Armazenamento de métricas       |
| Grafana                     | latest  | Visualização de métricas        |
| Testcontainers              | 1.21.0  | Testes de integração            |
| JUnit 5                     | 5.12.0  | Framework de testes             |
| Mockito                     | 5.17.0  | Mocks nos testes                |
| AssertJ                     | 3.27.3  | Assertions fluentes             |
| JaCoCo                      | 0.8.12  | Cobertura de código             |
| SonarQube                   | lastest | Inspeção da qualidade de código |
| Docker                      | 4.64.0  | Containerização                 |
| Maven                       | 3.9.9   | Build e dependências            |

---

## Endpoints

### POST /api/v1/shorten

Encurta uma URL longa.

**Request:**

```json
{
  "longUrl": "https://www.google.com"
}
```

**Response 201:**

```json
{
  "shortUrl": "http://localhost:80/xK9p"
}
```

**Response 400 — URL inválida:**

```json
{
  "status": 400,
  "error": "Validação falhou",
  "message": "URL inválida, verifique o formato. Exemplo: https://www.google.com",
  "timestamp": "2026-03-14T00:00:00Z"
}
```

**Response 429 — Rate limit excedido:**

```json
{
  "status": 429,
  "error": "Rate limit excedido",
  "message": "Limite de requisições excedido.",
  "timestamp": "2026-03-14T00:00:00Z"
}

---

### GET /{shortCode}

Redireciona para a URL original.

**Response 302:**

```

Location: https://www.google.com

````

**Response 404 — Short code não encontrado:**

```json
{
  "status": 404,
  "error": "URL não encontrada",
  "message": "URL não encontrada para o código: xK9p",
  "timestamp": "2026-03-14T00:00:00Z"
}
````

---

## Como Executar

### Pré-requisitos

- Java 21
- Maven 3.9+
- Docker

---

### Desenvolvimento Local

**1. Sobe a infraestrutura:**

```bash
docker compose up -d cassandra redis
```

**2. Cria o keyspace no Cassandra:**

```bash
docker exec -it url-shortener-cassandra cqlsh -e "
CREATE KEYSPACE IF NOT EXISTS url_shortener
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
"
```

**3. Sobe a aplicação:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

### Docker Compose Completo

Sobe todos os serviços incluindo load balancer, monitoramento e ferramentas visuais:

```bash
docker compose up --build -d
```

---

### Comandos Úteis

```bash
# verifica status dos containers
docker compose ps

# logs de uma instância da API
docker compose logs -f api-1

# rebuilda e sobe apenas as instâncias da API
docker compose up --build -d api-1 api-2 api-3

# derruba tudo preservando os volumes
docker compose down

# derruba tudo e apaga os volumes
docker compose down -v
```

---

## Testes

```bash
# unitários
mvn test

# integração (requer Docker)
mvn verify -DskipUTs=true

# todos
mvn verify

# relatório de cobertura
open target/site/jacoco/index.html
```

---

### Serviços Disponíveis

| Serviço               | URL                                 | Credenciais   |
| --------------------- | ----------------------------------- | ------------- |
| API via Load Balancer | http://localhost:80                 | -             |
| Swagger UI            | http://localhost:80/swagger-ui.html | -             |
| Actuator Health       | http://localhost:80/actuator/health | -             |
| Redis Commander       | http://localhost:8081               | -             |
| Prometheus            | http://localhost:9090               | -             |
| Grafana               | http://localhost:3000               | admin / admin |

#### Métricas da Aplicação

| Métrica                        | Descrição                |
| ------------------------------ | ------------------------ |
| `url_shortened_total`          | Total de URLs encurtadas |
| `url_cache_hit_total`          | Total de cache hits      |
| `url_cache_miss_total`         | Total de cache misses    |
| `http_server_requests_seconds` | Latência por endpoint    |

```

```
