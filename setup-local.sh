#!/bin/bash
set -e

echo "==================================================="
echo "🚀 ShutterFlow Local Environment Bootstrapper"
echo "==================================================="
echo ""
echo "This script will set up the entire ShutterFlow stack locally."
echo ""

# Check prerequisites
command -v docker >/dev/null 2>&1 || { echo "❌ Docker is required but not installed. Please install Docker first."; exit 1; }
command -v java >/dev/null 2>&1 || { echo "❌ Java JDK 21 is required but not installed. Please install JDK 21 first."; exit 1; }
command -v node >/dev/null 2>&1 || { echo "❌ Node.js is required but not installed. Please install Node.js 18+ first."; exit 1; }

echo "✅ All prerequisites detected."
echo ""

# Step 1: Start Docker containers
echo "📦 [1/5] Spinning up local Docker infrastructure..."
docker-compose up -d

echo ""
echo "⏳ Waiting for containers to become healthy..."
# Wait for MySQL to be ready
MAX_RETRIES=30
RETRY_COUNT=0
until docker exec shutterflow-mysql mysqladmin ping -h localhost -uroot -proot_shutterflow_pass --silent 2>/dev/null; do
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
        echo "❌ MySQL failed to start within timeout. Check Docker logs."
        exit 1
    fi
    echo "   Waiting for MySQL... ($RETRY_COUNT/$MAX_RETRIES)"
    sleep 2
done
echo "✅ MySQL is ready."

# Wait for Redis
RETRY_COUNT=0
until docker exec shutterflow-redis redis-cli ping 2>/dev/null | grep -q PONG; do
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
        echo "❌ Redis failed to start within timeout. Check Docker logs."
        exit 1
    fi
    sleep 1
done
echo "✅ Redis is ready."

# Wait for LocalStack
RETRY_COUNT=0
until curl -sf http://localhost:4566/_localstack/health >/dev/null 2>&1; do
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
        echo "❌ LocalStack failed to start within timeout. Check Docker logs."
        exit 1
    fi
    sleep 1
done
echo "✅ LocalStack is ready."

echo ""
echo "📦 All Docker containers are healthy!"
echo ""

# Step 2: Build Backend
echo "☕ [2/5] Compiling Spring Boot Backend..."
cd backend
./gradlew compileJava --no-daemon -q
echo "✅ Backend compiled successfully."
echo ""

# Step 3: Run Backend Tests
echo "🧪 [3/5] Running Backend Test Suite..."
./gradlew test --no-daemon -q
echo "✅ All backend tests passed."
echo ""
cd ..

# Step 4: Install Frontend Dependencies
echo "🎨 [4/5] Installing Frontend npm dependencies..."
cd frontend
if ! npm install --silent 2>/dev/null; then
    echo "   Retrying npm install without --silent..."
    npm install
fi
echo "✅ Frontend dependencies installed."
echo ""
cd ..

# Step 5: Start Backend
echo "🚀 [5/5] Starting Spring Boot Backend Server..."
echo "   Backend API will be available at: http://localhost:8080"
echo "   RedisInsight GUI available at: http://localhost:8001"
echo "   LocalStack S3 endpoint at: http://localhost:4566"
echo ""
echo "==================================================="
echo "🎉 System is fully initialized and operational!"
echo "==================================================="
echo ""
echo "📋 Next Steps:"
echo "   • Backend is starting on port 8080..."
echo "   • To start Frontend: cd frontend && npm start"
echo "   • Frontend will be available at: http://localhost:4200"
echo ""

cd backend
./gradlew bootRun --no-daemon
