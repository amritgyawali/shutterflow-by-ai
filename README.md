# ShutterFlow 📸

ShutterFlow is a highly sophisticated, production-grade SaaS tailored for photographers and studio owners. It is built in a modern **monorepo** layout with multi-tenant isolation, secure JWT authentication, and a fully containerized local development stack.

## 🏗️ Repository Architecture

```
shutterflow-by-ai/
├── backend/          # Spring Boot 3.3.5 REST API (Java 21)
│   ├── src/main/java/com/shutterflow/
│   │   ├── core/         # Domain-Driven modules (studio, user, client, pricing, booking, etc.)
│   │   └── infrastructure/  # AWS S3, Redis, SendGrid, Security
│   └── src/main/resources/
│       ├── db/migration/    # Flyway SQL migrations
│       ├── application.yml  # Base configuration
│       ├── application-dev.yml   # Local Docker dev profile
│       └── application-test.yml  # H2 in-memory test profile
├── frontend/         # Angular 17+ Standalone client (TypeScript)
├── mobile/           # React Native (Expo) mobile client
├── docker-compose.yml   # MySQL 8, Redis Stack, LocalStack S3
├── setup-local.sh       # One-click bootstrapper (Unix/Mac)
├── setup-local.bat      # One-click bootstrapper (Windows)
└── plans/               # Sprint planning documentation
```

### Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend Framework | Spring Boot | 3.3.5 |
| Language | Java (JDK) | 21 |
| Frontend | Angular (Standalone) | 17+ |
| Mobile | React Native (Expo) | Latest |
| Database | MySQL | 8.x |
| Cache | Redis Stack | Latest |
| Cloud Storage | AWS S3 (LocalStack locally) | — |
| Email | SendGrid | 4.10.1 |
| Migrations | Flyway | Core + MySQL |
| Auth | JWT (jjwt) | 0.12.5 |

## 🚀 Getting Started Locally

Every developer should be able to run this project in under 5 minutes using the automated local bootstrapper.

### Prerequisites

You need the following installed:
- **Docker & Docker Compose** (for MySQL, Redis, LocalStack containers)
- **Java JDK 21** (for the Spring Boot backend)
- **Node.js 18+ & npm** (for the Angular frontend)

### Run Stack

1. **Clone the Repo**
   ```bash
   git clone <repository-url>
   cd shutterflow-by-ai
   ```

2. **Start Services** using the one-click bootstrapper:
   - On Windows: `./setup-local.bat`
   - On Mac/Linux: `chmod +x setup-local.sh && ./setup-local.sh`

   This will:
   - Start Docker containers (MySQL, Redis, LocalStack)
   - Wait for containers to be healthy
   - Execute Flyway database migrations
   - Compile and test the Spring Boot backend
   - Install frontend npm dependencies
   - Start the backend API server on port `8080`

3. **Start the Frontend** (in a separate terminal):
   ```bash
   cd frontend
   npm start
   ```

### Available Services

| Service | URL | Description |
|---------|-----|-------------|
| Backend API | http://localhost:8080 | Spring Boot REST API |
| Frontend UI | http://localhost:4200 | Angular Development Server |
| RedisInsight | http://localhost:8001 | Redis visual GUI |
| LocalStack S3 | http://localhost:4566 | AWS S3 mock endpoint |
| MySQL | localhost:3306 | Database (user: `shutterflow_admin`) |

## 🧪 Development Principles

- **Multi-Tenancy**: All data is isolated by `studioId`. Every authenticated endpoint validates tenant access via `@PreAuthorize`.
- **Domain-Driven Design**: Business logic is organized into bounded contexts (`core/studio`, `core/booking`, `core/invoice`, etc.).
- **UUID Primary Keys**: All entities use `VARCHAR(36)` UUID strings as primary keys.
- **Audit Trails**: Entities use `@CreationTimestamp` and `@UpdateTimestamp` for automatic audit fields.
- **Profile Isolation**: Use `application-dev.yml` for local dev, `application-test.yml` for tests (H2 in-memory).

## 🔧 Useful Commands

```bash
# Backend
cd backend
./gradlew compileJava          # Compile only
./gradlew test --no-daemon     # Run all tests
./gradlew bootRun              # Start backend server

# Frontend
cd frontend
npm install                    # Install dependencies
npm start                      # Start dev server (port 4200)
npm run build                  # Production build

# Docker
docker-compose up -d           # Start all infrastructure
docker-compose down            # Stop all infrastructure
docker-compose ps              # Check container status
```
