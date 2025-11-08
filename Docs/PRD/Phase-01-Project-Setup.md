# Phase 1: Project Setup & Infrastructure

**Estimated Time:** 4-6 hours
**Dependencies:** None
**Status:** Not Started

## Overview

Set up the foundational infrastructure for the InvoiceMe ERP system, including backend (Spring Boot), frontend (Next.js), database (PostgreSQL), and development environment configuration.

## Objectives

- Initialize clean repository structure
- Configure Spring Boot backend with all required dependencies
- Set up Next.js frontend with TypeScript and Tailwind CSS
- Configure PostgreSQL database with Flyway migrations
- Set up environment variables and basic security

## Tasks

### 1.1 Initialize Repository Structure

Create the following directory structure:

```
invoiceme/
├── backend/                  # Spring Boot API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/invoiceme/
│   │   │   └── resources/
│   │   │       ├── db/migration/
│   │   │       └── application.properties
│   │   └── test/
│   ├── pom.xml (or build.gradle)
│   ├── README.md
│   └── .env.example
├── frontend/                 # Next.js React app
│   ├── src/
│   │   ├── app/
│   │   ├── components/
│   │   ├── lib/
│   │   └── types/
│   ├── public/
│   ├── package.json
│   ├── tsconfig.json
│   ├── tailwind.config.js
│   ├── README.md
│   └── .env.example
├── docs/                     # Documentation
│   └── README.md
├── database/                 # SQL scripts
│   └── README.md
├── .gitignore
└── README.md
```

**Action Items:**
- [ ] Create root directory structure
- [ ] Initialize Git repository
- [ ] Create comprehensive .gitignore (exclude .env, target/, node_modules/, etc.)

---

### 1.2 Set Up Spring Boot Backend

**Technology Stack:**
- Java 17+
- Spring Boot 3.x
- Maven or Gradle

**Required Dependencies:**

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Core Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Data Access -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Flyway -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>

    <!-- OpenAI Client -->
    <dependency>
        <groupId>com.theokanning.openai-gpt3-java</groupId>
        <artifactId>service</artifactId>
        <version>0.18.2</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Action Items:**
- [ ] Create Spring Boot project structure
- [ ] Configure pom.xml or build.gradle with dependencies
- [ ] Create main application class (`InvoiceMeApplication.java`)
- [ ] Verify application starts successfully

---

### 1.3 Set Up Next.js Frontend

**Technology Stack:**
- Next.js 14+ (App Router)
- TypeScript 5.x
- Tailwind CSS
- React 18+

**Installation Commands:**

```bash
cd frontend
npx create-next-app@latest . --typescript --tailwind --app --no-src-dir
```

**Additional Dependencies:**

```bash
npm install axios react-hook-form zod @hookform/resolvers
npm install date-fns
npm install zustand  # Optional: lightweight state management
npm install @radix-ui/react-dialog @radix-ui/react-dropdown-menu  # For shadcn/ui components
```

**Project Structure:**

```
frontend/src/
├── app/
│   ├── layout.tsx
│   ├── page.tsx
│   ├── customers/
│   ├── invoices/
│   └── payments/
├── components/
│   ├── ui/              # shadcn/ui components
│   ├── customers/
│   ├── invoices/
│   └── shared/
├── lib/
│   ├── api.ts           # API client
│   └── utils.ts
└── types/
    ├── customer.ts
    ├── invoice.ts
    └── payment.ts
```

**Action Items:**
- [ ] Initialize Next.js project
- [ ] Install dependencies
- [ ] Configure TypeScript (tsconfig.json)
- [ ] Configure Tailwind CSS
- [ ] Create basic layout and navigation
- [ ] Verify dev server runs (`npm run dev`)

---

### 1.4 Configure PostgreSQL Database and Flyway

**Database Setup:**

```bash
# Install PostgreSQL (macOS)
brew install postgresql@15
brew services start postgresql@15

# Create database
createdb invoiceme
```

**Flyway Configuration (`application.properties`):**

```properties
# Database
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/invoiceme}
spring.datasource.username=${DATABASE_USERNAME:postgres}
spring.datasource.password=${DATABASE_PASSWORD:postgres}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

**Action Items:**
- [ ] Install PostgreSQL
- [ ] Create `invoiceme` database
- [ ] Configure Flyway in `application.properties`
- [ ] Create `db/migration` directory
- [ ] Verify Flyway connects successfully

---

### 1.5 Set Up Environment Variables and Security

**Backend `.env.example`:**

```
DATABASE_URL=jdbc:postgresql://localhost:5432/invoiceme
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
OPENAI_API_KEY=sk-your-key-here
OPENAI_MODEL=gpt-4o-mini
JWT_SECRET=change-me-in-production-use-strong-secret
```

**Frontend `.env.example`:**

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

**Basic Security Configuration:**

Create `backend/src/main/java/com/invoiceme/config/SecurityConfig.java`:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // For demo; enable in production
            .cors().and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/payments/link/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("demo")
            .password("{noop}password")
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
```

**Action Items:**
- [ ] Create `.env.example` files
- [ ] Add `.env` to `.gitignore`
- [ ] Create basic security configuration
- [ ] Document demo credentials (username: demo, password: password)

---

## Verification Checklist

After completing Phase 1, verify:

- [ ] Git repository initialized with proper .gitignore
- [ ] Backend Spring Boot application starts without errors
- [ ] Frontend Next.js dev server runs on localhost:3000
- [ ] PostgreSQL database `invoiceme` exists
- [ ] Flyway connects to database successfully
- [ ] Environment variables loaded correctly
- [ ] Basic security prevents unauthorized access

## Next Steps

Proceed to [Phase 2: Domain Model & Database Schema](Phase-02-Domain-Model.md)

---

## Reference Files

- Main PRD: `Docs/PRD/PRD.md` (Section 4: Architecture & Technical Stack)
- Implementation Checklist: PRD Section 9, Phase 1
