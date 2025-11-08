# InvoiceMe Backend API

Spring Boot REST API with DDD, CQRS, and VSA architecture.

## Build & Run

```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test

# Test coverage
./mvnw test jacoco:report
```

## Architecture

- **Domain Layer:** Pure business logic entities
- **Application Layer:** CQRS commands and queries (VSA organized)
- **Infrastructure Layer:** Repositories, external services
- **API Layer:** REST controllers

## API Documentation

API runs on http://localhost:8080

### Authentication
Demo credentials: `demo:password` (Basic Auth)

### Endpoints
- `POST /api/customers` - Create customer
- `GET /api/customers` - List customers
- `POST /api/invoices` - Create invoice
- `GET /api/invoices` - List invoices
- `POST /api/payments` - Record payment

See full API documentation in Docs/PRD/
