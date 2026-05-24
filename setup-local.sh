#!/bin/bash
set -e

echo "==================================================="
echo "🚀 ShutterFlow Local Environment Bootstrapper"
echo "==================================================="

echo ""
echo "📦 [1/3] Spinning up local Docker infrastructure..."
docker-compose up -d

echo ""
echo "☕ [2/3] Building and starting Spring Boot Backend..."
cd backend
./gradlew bootRun

cd ..

echo ""
echo "🎨 [3/3] Setting up Frontend..."
echo "Run 'cd frontend && npm start' in a separate terminal to run the UI!"
echo ""
echo "==================================================="
echo "🎉 System is fully initialized and operational!"
echo "==================================================="
