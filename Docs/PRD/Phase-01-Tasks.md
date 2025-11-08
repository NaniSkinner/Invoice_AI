# Phase 1 Tasks - Project Setup & Infrastructure

**Execution Guide with Step-by-Step Commands**

**Status:** ✅ COMPLETED
**Actual Time:** ~2 hours (using automation)
**Completed Date:** November 8, 2025
**Estimated Time:** 4-6 hours
**Prerequisites:** Java 17+, Node.js 18+, PostgreSQL 15+, Git installed

## Completion Summary

All Phase 1 tasks have been completed successfully:
- ✅ Directory structure created
- ✅ Spring Boot backend configured with Maven
- ✅ Next.js frontend configured with Bun
- ✅ PostgreSQL database created
- ✅ Environment variables set up
- ✅ Security configuration implemented
- ✅ Backend compiles successfully
- ✅ Frontend builds successfully
- ✅ README documentation added

### Next Steps
Proceed to **Phase 2: Domain Model & Database Schema** (Phase-02-Tasks.md)

---

## Original Task Documentation

---

## Task 1.1: Initialize Repository Structure

### Step 1.1.1: Create root project directory
```bash
# Navigate to your projects directory
cd ~/dev/Gauntlet

# Verify Invoice_AI directory exists (should already exist)
ls -la Invoice_AI
```

**Expected Output:**
```
drwxr-xr-x  Invoice_AI/
```

### Step 1.1.2: Create main directory structure
```bash
cd Invoice_AI

# Create all main directories
mkdir -p backend/src/main/java/com/invoiceme
mkdir -p backend/src/main/resources/db/migration
mkdir -p backend/src/test/java/com/invoiceme
mkdir -p frontend/src/app
mkdir -p frontend/src/components
mkdir -p frontend/src/lib
mkdir -p frontend/src/types
mkdir -p frontend/public
mkdir -p docs
mkdir -p database

# Verify structure
tree -L 2 -d
```

**Expected Output:**
```
.
├── Docs
├── backend
│   └── src
├── database
├── docs
└── frontend
    ├── public
    └── src
```

### Step 1.1.3: Initialize Git (if not already done)
```bash
# Check if git is already initialized
git status

# If not initialized, run:
git init

# Verify
git status
```

**Expected Output:**
```
On branch main
No commits yet
```

### Step 1.1.4: Create comprehensive .gitignore
```bash
cat > .gitignore << 'EOF'
# Backend (Spring Boot)
backend/target/
backend/.env
backend/*.log
backend/.DS_Store
backend/.idea/
backend/*.iml

# Frontend (Next.js)
frontend/node_modules/
frontend/.next/
frontend/out/
frontend/.env.local
frontend/.env.production.local
frontend/.DS_Store
frontend/.idea/

# Database
database/*.sql.bak
database/temp/

# IDEs
.idea/
.vscode/
*.swp
*.swo
*~

# OS
.DS_Store
Thumbs.db

# Logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Environment variables
.env
.env.local
.env.*.local
EOF

# Verify file created
cat .gitignore
```

**Verification:**
- [ ] Directory structure created
- [ ] Git initialized
- [ ] .gitignore created with all exclusions

---

## Task 1.2: Set Up Spring Boot Backend

### Step 1.2.1: Navigate to backend directory
```bash
cd backend
pwd
```

**Expected Output:**
```
/Users/nanis/dev/Gauntlet/Invoice_AI/backend
```

### Step 1.2.2: Create pom.xml with all dependencies
```bash
cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.invoiceme</groupId>
    <artifactId>invoiceme-api</artifactId>
    <version>1.0.0</version>
    <name>InvoiceMe API</name>
    <description>AI-Assisted ERP Invoicing System</description>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

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

        <!-- Flyway Migration -->
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

        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
EOF

# Verify file created
ls -la pom.xml
```

**Expected Output:**
```
-rw-r--r--  1 nanis  staff  3456 Nov  8 14:30 pom.xml
```

### Step 1.2.3: Create Maven wrapper (for portability)
```bash
# Download Maven wrapper
mvn wrapper:wrapper

# Verify wrapper created
ls -la mvnw
```

**Expected Output:**
```
-rwxr-xr-x  1 nanis  staff  10070 Nov  8 14:31 mvnw
```

### Step 1.2.4: Create main application class
```bash
# Create package directories
mkdir -p src/main/java/com/invoiceme

# Create main application class
cat > src/main/java/com/invoiceme/InvoiceMeApplication.java << 'EOF'
package com.invoiceme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InvoiceMeApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvoiceMeApplication.class, args);
    }
}
EOF

# Verify file created
cat src/main/java/com/invoiceme/InvoiceMeApplication.java
```

### Step 1.2.5: Create application.properties
```bash
# Create resources directory if it doesn't exist
mkdir -p src/main/resources

cat > src/main/resources/application.properties << 'EOF'
# Application Name
spring.application.name=invoiceme-api

# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/invoiceme}
spring.datasource.username=${DATABASE_USERNAME:postgres}
spring.datasource.password=${DATABASE_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# OpenAI Configuration
openai.api.key=${OPENAI_API_KEY}
openai.api.model=${OPENAI_MODEL:gpt-4o-mini}

# Scheduling
scheduling.overdue-check.cron=0 0 0 * * *

# Security
jwt.secret=${JWT_SECRET:change-me-in-production-use-strong-secret}
jwt.expiration=86400000

# Logging
logging.level.com.invoiceme=INFO
logging.level.org.springframework.web=INFO
EOF

# Verify
cat src/main/resources/application.properties
```

### Step 1.2.6: Test build (will fail without database, but validates setup)
```bash
./mvnw clean compile
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  XX.XXX s
```

**Verification:**
- [ ] pom.xml created with all dependencies
- [ ] Maven wrapper created
- [ ] Main application class created
- [ ] application.properties configured
- [ ] Project compiles successfully

---

## Task 1.3: Set Up Next.js Frontend

### Step 1.3.1: Navigate to frontend directory
```bash
cd ../frontend
pwd
```

**Expected Output:**
```
/Users/nanis/dev/Gauntlet/Invoice_AI/frontend
```

### Step 1.3.2: Initialize Next.js project with TypeScript
```bash
# Create package.json
cat > package.json << 'EOF'
{
  "name": "invoiceme-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "next": "^14.0.3",
    "axios": "^1.6.2",
    "react-hook-form": "^7.48.2",
    "zod": "^3.22.4",
    "@hookform/resolvers": "^3.3.2",
    "date-fns": "^2.30.0",
    "zustand": "^4.4.7"
  },
  "devDependencies": {
    "typescript": "^5.3.2",
    "@types/node": "^20.10.0",
    "@types/react": "^18.2.39",
    "@types/react-dom": "^18.2.17",
    "tailwindcss": "^3.3.5",
    "postcss": "^8.4.31",
    "autoprefixer": "^10.4.16",
    "eslint": "^8.54.0",
    "eslint-config-next": "^14.0.3"
  }
}
EOF

# Verify
cat package.json
```

### Step 1.3.3: Install dependencies
```bash
npm install
```

**Expected Output:**
```
added XXX packages in XXs
```

### Step 1.3.4: Create TypeScript configuration
```bash
cat > tsconfig.json << 'EOF'
{
  "compilerOptions": {
    "target": "ES2020",
    "lib": ["dom", "dom.iterable", "esnext"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "incremental": true,
    "plugins": [
      {
        "name": "next"
      }
    ],
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
  "exclude": ["node_modules"]
}
EOF

cat tsconfig.json
```

### Step 1.3.5: Create Next.js configuration
```bash
cat > next.config.js << 'EOF'
/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  env: {
    API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
  },
}

module.exports = nextConfig
EOF

cat next.config.js
```

### Step 1.3.6: Initialize Tailwind CSS
```bash
cat > tailwind.config.js << 'EOF'
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
EOF

cat > postcss.config.js << 'EOF'
module.exports = {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
EOF

# Verify both files
ls -la *.config.js
```

### Step 1.3.7: Create global styles with Tailwind
```bash
mkdir -p src/app

cat > src/app/globals.css << 'EOF'
@tailwind base;
@tailwind components;
@tailwind utilities;

:root {
  --foreground-rgb: 0, 0, 0;
  --background-rgb: 255, 255, 255;
}

@media (prefers-color-scheme: dark) {
  :root {
    --foreground-rgb: 255, 255, 255;
    --background-rgb: 0, 0, 0;
  }
}

body {
  color: rgb(var(--foreground-rgb));
  background: rgb(var(--background-rgb));
}
EOF

cat src/app/globals.css
```

### Step 1.3.8: Create root layout
```bash
cat > src/app/layout.tsx << 'EOF'
import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'InvoiceMe - AI-Assisted Invoicing',
  description: 'Modern ERP invoicing system with AI features',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
EOF

cat src/app/layout.tsx
```

### Step 1.3.9: Create home page
```bash
cat > src/app/page.tsx << 'EOF'
export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <div className="text-center">
        <h1 className="text-4xl font-bold mb-4">InvoiceMe</h1>
        <p className="text-xl text-gray-600">
          AI-Assisted Invoicing System
        </p>
        <p className="mt-4 text-sm text-gray-500">
          Setup Complete - Ready for Development
        </p>
      </div>
    </main>
  )
}
EOF

cat src/app/page.tsx
```

### Step 1.3.10: Create API client utility
```bash
mkdir -p src/lib

cat > src/lib/api.ts << 'EOF'
import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export const apiClient = {
  async get(endpoint: string) {
    const response = await axios.get(`${API_BASE_URL}${endpoint}`);
    return response.data;
  },

  async post(endpoint: string, data: any) {
    const response = await axios.post(`${API_BASE_URL}${endpoint}`, data);
    return response.data;
  },

  async put(endpoint: string, data: any) {
    const response = await axios.put(`${API_BASE_URL}${endpoint}`, data);
    return response.data;
  },

  async delete(endpoint: string) {
    const response = await axios.delete(`${API_BASE_URL}${endpoint}`);
    return response.data;
  },
};
EOF

cat src/lib/api.ts
```

### Step 1.3.11: Create TypeScript type definitions
```bash
mkdir -p src/types

cat > src/types/customer.ts << 'EOF'
export interface CustomerDto {
  id: string;
  businessName: string;
  contactName: string;
  email: string;
  phone?: string;
  billingAddress: AddressDto;
  shippingAddress?: AddressDto;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AddressDto {
  street: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
}
EOF

cat > src/types/invoice.ts << 'EOF'
export interface InvoiceDto {
  id: string;
  invoiceNumber: string;
  customerId: string;
  customerName: string;
  issueDate: string;
  dueDate: string;
  status: 'DRAFT' | 'SENT' | 'PAID' | 'CANCELLED';
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  amountPaid: number;
  balanceRemaining: number;
  allowsPartialPayment: boolean;
  paymentLink?: string;
  notes?: string;
  terms?: string;
  lineItems: LineItemDto[];
}

export interface LineItemDto {
  id: string;
  description: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}
EOF

cat > src/types/payment.ts << 'EOF'
export interface PaymentDto {
  id: string;
  invoiceId: string;
  invoiceNumber: string;
  paymentAmount: number;
  paymentDate: string;
  paymentMethod: 'CREDIT_CARD' | 'BANK_TRANSFER' | 'CHECK' | 'CASH' | 'OTHER';
  transactionReference?: string;
  notes?: string;
  createdAt: string;
}
EOF

# Verify all type files
ls -la src/types/
```

### Step 1.3.12: Test frontend dev server
```bash
npm run dev
```

**Expected Output:**
```
- ready started server on 0.0.0.0:3000, url: http://localhost:3000
- event compiled client and server successfully
```

**Manual Verification:**
1. Open browser to http://localhost:3000
2. You should see "InvoiceMe" heading
3. Press Ctrl+C to stop server

**Verification:**
- [ ] npm install completed successfully
- [ ] TypeScript configured
- [ ] Tailwind CSS configured
- [ ] Root layout and page created
- [ ] API client utility created
- [ ] Type definitions created
- [ ] Dev server runs without errors

---

## Task 1.4: Configure PostgreSQL Database

### Step 1.4.1: Check if PostgreSQL is installed
```bash
psql --version
```

**Expected Output:**
```
psql (PostgreSQL) 15.x
```

**If not installed (macOS):**
```bash
brew install postgresql@15
brew services start postgresql@15
```

### Step 1.4.2: Create invoiceme database
```bash
# Connect to PostgreSQL
psql postgres

# In psql, run:
CREATE DATABASE invoiceme;

# Verify database created
\l

# You should see 'invoiceme' in the list

# Exit psql
\q
```

**Alternative single command:**
```bash
createdb invoiceme

# Verify
psql -l | grep invoiceme
```

**Expected Output:**
```
 invoiceme | nanis | UTF8 | ...
```

### Step 1.4.3: Test database connection
```bash
psql -d invoiceme -c "SELECT version();"
```

**Expected Output:**
```
PostgreSQL 15.x on ...
```

### Step 1.4.4: Create Flyway migration directory (already done, verify)
```bash
cd ../backend
ls -la src/main/resources/db/migration/
```

**Expected Output:**
```
drwxr-xr-x  db/migration/
```

### Step 1.4.5: Create .gitkeep file to preserve empty migration directory
```bash
touch src/main/resources/db/migration/.gitkeep
```

**Verification:**
- [ ] PostgreSQL installed and running
- [ ] Database 'invoiceme' created
- [ ] Can connect to database
- [ ] Migration directory exists

---

## Task 1.5: Set Up Environment Variables

### Step 1.5.1: Create backend .env.example
```bash
cd backend

cat > .env.example << 'EOF'
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/invoiceme
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# OpenAI Configuration
OPENAI_API_KEY=sk-your-key-here
OPENAI_MODEL=gpt-4o-mini

# Security
JWT_SECRET=change-me-in-production-use-strong-secret-min-256-bits
EOF

cat .env.example
```

### Step 1.5.2: Create actual backend .env (not committed)
```bash
cp .env.example .env

# Edit with your actual values
# For now, leave OpenAI key as placeholder (will add later)
cat .env
```

### Step 1.5.3: Create frontend .env.example
```bash
cd ../frontend

cat > .env.example << 'EOF'
# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080/api
EOF

cat .env.example
```

### Step 1.5.4: Create actual frontend .env.local
```bash
cp .env.example .env.local
cat .env.local
```

### Step 1.5.5: Verify .gitignore excludes .env files
```bash
cd ..
grep -E "\.env$|\.env\.local" .gitignore
```

**Expected Output:**
```
.env
.env.local
.env.*.local
```

**Verification:**
- [ ] .env.example files created for both backend and frontend
- [ ] Actual .env files created
- [ ] .gitignore excludes .env files
- [ ] Environment variables documented

---

## Task 1.6: Create Basic Security Configuration

### Step 1.6.1: Create config package
```bash
cd backend
mkdir -p src/main/java/com/invoiceme/config
```

### Step 1.6.2: Create SecurityConfig.java
```bash
cat > src/main/java/com/invoiceme/config/SecurityConfig.java << 'EOF'
package com.invoiceme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for demo (enable in production)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/payments/link/**").permitAll() // Public payment pages
                .anyRequest().authenticated()
            )
            .httpBasic();

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("demo")
            .password("{noop}password") // {noop} = no password encoding for demo
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
EOF

cat src/main/java/com/invoiceme/config/SecurityConfig.java
```

### Step 1.6.3: Build and verify security config compiles
```bash
./mvnw clean compile
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
```

**Verification:**
- [ ] SecurityConfig.java created
- [ ] Demo credentials configured (demo/password)
- [ ] CORS enabled for frontend
- [ ] Public payment endpoints allowed
- [ ] Code compiles successfully

---

## Task 1.7: Create README Files

### Step 1.7.1: Create main project README
```bash
cd ..

cat > README.md << 'EOF'
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

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
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
   # Edit .env with your values
   ./mvnw spring-boot:run
   ```

3. **Frontend**
   ```bash
   cd frontend
   cp .env.example .env.local
   npm install
   npm run dev
   ```

4. **Access**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Demo Credentials: demo/password

## Documentation

See [Docs/PRD/](Docs/PRD/) for complete implementation guides.

## License

MIT
EOF

cat README.md
```

### Step 1.7.2: Create backend README
```bash
cat > backend/README.md << 'EOF'
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
EOF

cat backend/README.md
```

### Step 1.7.3: Create frontend README
```bash
cat > frontend/README.md << 'EOF'
# InvoiceMe Frontend

Next.js 14 frontend with TypeScript and Tailwind CSS.

## Development

```bash
# Install dependencies
npm install

# Run dev server
npm run dev

# Build for production
npm run build

# Run production build
npm start

# Lint
npm run lint
```

## Access

Frontend: http://localhost:3000

## Structure

```
src/
├── app/              # Next.js App Router pages
├── components/       # React components
├── lib/              # Utilities (API client, etc.)
└── types/            # TypeScript type definitions
```

## Environment Variables

Copy `.env.example` to `.env.local` and configure:

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```
EOF

cat frontend/README.md
```

**Verification:**
- [ ] Main README created
- [ ] Backend README created
- [ ] Frontend README created
- [ ] All READMEs contain setup instructions

---

## Task 1.8: Final Verification & Git Commit

### Step 1.8.1: Test backend starts (without database migration yet)
```bash
cd backend

# This will fail trying to connect to database, but validates config
./mvnw spring-boot:run
```

**Expected:** Application starts, tries to connect to database, may fail on Flyway (that's OK for now)

Press Ctrl+C to stop

### Step 1.8.2: Test frontend starts
```bash
cd ../frontend
npm run dev
```

**Expected:**
```
- ready started server on 0.0.0.0:3000
```

Open http://localhost:3000 in browser - should see InvoiceMe homepage

Press Ctrl+C to stop

### Step 1.8.3: Create initial Git commit
```bash
cd ..

# Add all files
git add .

# Check status
git status

# Create initial commit
git commit -m "Phase 1: Project setup complete

- Spring Boot backend configured
- Next.js frontend configured
- PostgreSQL database created
- Environment variables set up
- Basic security configured
- README documentation added"

# Verify commit
git log --oneline
```

**Expected Output:**
```
abc1234 Phase 1: Project setup complete
```

### Step 1.8.4: Verify complete directory structure
```bash
tree -L 3 -I 'node_modules|target' .
```

**Expected Structure:**
```
.
├── Docs/
│   └── PRD/
├── README.md
├── backend/
│   ├── .env
│   ├── .env.example
│   ├── README.md
│   ├── mvnw
│   ├── pom.xml
│   └── src/
│       ├── main/
│       └── test/
├── database/
├── docs/
└── frontend/
    ├── .env.example
    ├── .env.local
    ├── README.md
    ├── next.config.js
    ├── package.json
    ├── src/
    │   ├── app/
    │   ├── lib/
    │   └── types/
    └── tailwind.config.js
```

---

## Phase 1 Completion Checklist

### Backend ✅
- [x] Spring Boot project created
- [x] pom.xml with all dependencies
- [x] Main application class created
- [x] application.properties configured
- [x] Security config implemented
- [x] Project compiles successfully

### Frontend ✅
- [x] Next.js project initialized
- [x] TypeScript configured
- [x] Tailwind CSS configured
- [x] API client utility created
- [x] Type definitions created
- [x] Dev server runs successfully

### Database ✅
- [x] PostgreSQL installed
- [x] Database 'invoiceme' created
- [x] Can connect to database
- [x] Flyway migration directory ready

### Environment ✅
- [x] .env.example files created
- [x] Actual .env files created
- [x] .gitignore excludes sensitive files
- [x] Environment variables documented

### Documentation ✅
- [x] Main README created
- [x] Backend README created
- [x] Frontend README created
- [x] Setup instructions documented

### Git ✅
- [x] Repository initialized (user will handle git)
- [x] .gitignore configured
- [ ] Initial commit created (user will handle git)

---

## Next Steps

✅ **Phase 1 Complete!**

Proceed to **Phase 2: Domain Model & Database Schema**
- File: `Phase-02-Tasks.md`
- Estimated time: 4-6 hours
- Creates database schema and domain entities

---

## Troubleshooting

### Backend won't compile
```bash
# Check Java version
java -version  # Should be 17+

# Clean and rebuild
./mvnw clean install -U
```

### Frontend won't start
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Database connection fails
```bash
# Check PostgreSQL is running
brew services list | grep postgresql

# Restart if needed
brew services restart postgresql@15
```

### Port already in use
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

---

**Phase 1 Complete! Total Time: ~4-6 hours**
