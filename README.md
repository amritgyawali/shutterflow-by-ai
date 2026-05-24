# ShutterFlow 📸

ShutterFlow is a highly sophisticated, production-grade SaaS tailored for photographers and studio owners. It is built in a modern **monorepo** layout.

## 🏗️ Repository Architecture

- `/backend` — **Spring Boot 3.2.x** REST API, powered by MySQL, AWS S3, Stripe, and SendGrid.
- `/frontend` — **Angular 17+** client portal, administrative dashboard, and responsive billing tools.
- `/mobile` — **React Native (Expo)** helper client for photographers on the field (core services).

## 🚀 Getting Started Locally

Every developer should be able to run this project in under 5 minutes using the automated local bootstrapper.

### Prerequisites

You need the following installed:
- **Docker & Docker Compose**
- **Java JDK 17 or 21**
- **Node.js 18+ & npm**

### Run Stack

1. **Clone the Repo**
2. **Start Services**
   Run the bootstrapper script:
   - On Windows: `./setup-local.bat`
   - On Mac/Linux: `./setup-local.sh`

This spins up the MySQL container, executes migrations, builds the Spring Boot API, and runs the Angular development server.
