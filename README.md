# Livraria

API REST para gerenciamento de catálogo de livros, com persistência em **MongoDB** e cache em **Redis**, documentada via **OpenAPI/Swagger UI**.

## Stack

- Java 21 / Spring Boot 3.5
- Spring Web · Spring Data MongoDB · Spring Data Redis · Spring Cache
- MapStruct · Lombok · Jakarta Validation
- SpringDoc OpenAPI 2.x (Swagger UI)
- JUnit 5 · Mockito · Testcontainers (MongoDB + Redis) · MockMvc · AssertJ
- JaCoCo (mínimo 80% de cobertura)

## Como executar

Pré-requisitos: Docker + JDK 21.

```bash
docker compose up -d         # MongoDB (27017) e Redis (6379)
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/book` | Cadastra um livro (ISBN único) |
| `GET` | `/book` | Lista livros (paginado, filtro opcional `?genre=`) |
| `GET` | `/book/{id}` | Busca por id (cache Redis) |
| `PUT` | `/book/{id}` | Atualiza um livro |
| `DELETE` | `/book/{id}` | Remove um livro |

Erros padronizados via `ProblemDetail` (RFC 7807) através do `GlobalExceptionHandler` (400, 404, etc.).

## Testes

```bash
./mvnw clean verify
```

- **Unitários** (Mockito) na camada de serviço — sucesso e erros (ISBN duplicado, livro inexistente, id nulo).
- **Integração** (Spring Boot + MockMvc + Testcontainers) na camada de controller, com Mongo e Redis reais subindo via Docker. Cobre 200/201/204/400/404 e o caching no Redis.
- **Cobertura mínima**: 80% no bundle e nos pacotes `services`/`controllers` (linha + branch). Configurado no `jacoco-maven-plugin`; relatório em `target/site/jacoco/index.html`.

## Estrutura

```
src/main/java/com/cesar/livraria
├── controllers     # BookController + anotações OpenAPI
├── services        # BookService (cache Redis)
├── repository      # BookRepository (Spring Data Mongo)
├── entities        # Book, Genre
├── request         # BookRequest (records + Jakarta Validation)
├── response        # BookResponse, PageResponse<T>
├── mappers         # MapStruct (BookMapper)
├── exception       # ResourceNotFoundException, IsbnAlreadyExistsException, GlobalExceptionHandler
└── config          # MongoConfig, CacheConfig (Redis + Jackson), OpenApiConfig
```
