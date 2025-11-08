# InvoiceMe - AI-Assisted Invoicing System

AI-powered ERP invoicing system demonstrating DDD, CQRS, and VSA architecture patterns.

## Project Structure

```
Invoice_AI/
├── backend/          # Spring Boot API
├── frontend/         # Next.js React app
├── database/         # SQL scripts and migrations
├── docs/             # Technical documentation
└── Docs/PRD/         # Product requirements and implementation guides
```

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2
- PostgreSQL 15
- Flyway migrations
- OpenAI API integration

### Frontend
- Next.js 14 (App Router)
- TypeScript 5
- Tailwind CSS
- React Hook Form + Zod
- Bun package manager

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+ (or Bun)
- PostgreSQL 15+
- Maven 3.8+

### Setup

1. **Database**
   ```bash
   createdb invoiceme
   ```

2. **Backend**
   ```bash
   cd backend
   cp .env.example .env
   # Edit .env with your values (add OpenAI API key)
   ./mvnw spring-boot:run
   ```

3. **Frontend**
   ```bash
   cd frontend
   cp .env.example .env.local
   bun install
   bun run dev
   ```

4. **Access**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Demo Credentials: demo/password

## Documentation

See [Docs/PRD/](Docs/PRD/) for complete implementation guides.

## License

MIT
