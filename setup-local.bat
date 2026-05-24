@echo off
echo ===================================================
echo 🚀 ShutterFlow Local Environment Bootstrapper
echo ===================================================

echo.
echo 📦 [1/3] Spinning up local Docker infrastructure...
docker-compose up -d

if %ERRORLEVEL% neq 0 (
    echo ❌ Failed to start docker containers. Make sure Docker Desktop is running!
    exit /b %ERRORLEVEL%
)

echo.
echo ☕ [2/3] Building and starting Spring Boot Backend...
cd backend
call ./gradlew bootRun

if %ERRORLEVEL% neq 0 (
    echo ❌ Backend build failed!
    cd ..
    exit /b %ERRORLEVEL%
)

cd ..

echo.
echo 🎨 [3/3] Setting up Frontend...
echo Run "cd frontend && npm start" in a separate terminal to run the UI!
echo.
echo ===================================================
echo 🎉 System is fully initialized and operational!
echo ===================================================
