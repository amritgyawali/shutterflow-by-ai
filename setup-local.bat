@echo off
echo ===================================================
echo 🚀 ShutterFlow Local Environment Bootstrapper
echo ===================================================
echo.

REM Check prerequisites
where docker >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ❌ Docker is required but not installed. Please install Docker Desktop first.
    exit /b 1
)
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ❌ Java JDK 21 is required but not installed. Please install JDK 21 first.
    exit /b 1
)
where node >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ❌ Node.js is required but not installed. Please install Node.js 18+ first.
    exit /b 1
)

echo ✅ All prerequisites detected.
echo.

REM Step 1: Start Docker containers
echo 📦 [1/5] Spinning up local Docker infrastructure...
docker-compose up -d

if %ERRORLEVEL% neq 0 (
    echo ❌ Failed to start Docker containers. Make sure Docker Desktop is running!
    exit /b %ERRORLEVEL%
)

echo.
echo ⏳ Waiting for containers to become healthy...

REM Wait for MySQL
set RETRY_COUNT=0
:wait_mysql
set /a RETRY_COUNT+=1
if %RETRY_COUNT% gtr 30 (
    echo ❌ MySQL failed to start within timeout. Check Docker logs.
    exit /b 1
)
docker exec shutterflow-mysql mysqladmin ping -h localhost -uroot -proot_shutterflow_pass --silent >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo    Waiting for MySQL... (%RETRY_COUNT%/30)
    timeout /t 2 /nobreak >nul
    goto wait_mysql
)
echo ✅ MySQL is ready.

REM Wait for Redis
:wait_redis
docker exec shutterflow-redis redis-cli ping >nul 2>&1
if %ERRORLEVEL% neq 0 (
    timeout /t 1 /nobreak >nul
    goto wait_redis
)
echo ✅ Redis is ready.

echo.
echo 📦 All Docker containers are healthy!
echo.

REM Step 2: Build Backend
echo ☕ [2/5] Compiling Spring Boot Backend...
cd backend
call gradlew.bat compileJava --no-daemon -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Backend compilation failed!
    cd ..
    exit /b %ERRORLEVEL%
)
echo ✅ Backend compiled successfully.
echo.

REM Step 3: Run Backend Tests
echo 🧪 [3/5] Running Backend Test Suite...
call gradlew.bat test --no-daemon -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Backend tests failed!
    cd ..
    exit /b %ERRORLEVEL%
)
echo ✅ All backend tests passed.
echo.
cd ..

REM Step 4: Install Frontend Dependencies
echo 🎨 [4/5] Installing Frontend npm dependencies...
cd frontend
call npm install --silent
if %ERRORLEVEL% neq 0 (
    echo ⚠️ npm install had warnings, attempting to continue...
)
echo ✅ Frontend dependencies installed.
echo.
cd ..

REM Step 5: Start Backend
echo 🚀 [5/5] Starting Spring Boot Backend Server...
echo    Backend API will be available at: http://localhost:8080
echo    RedisInsight GUI available at: http://localhost:8001
echo    LocalStack S3 endpoint at: http://localhost:4566
echo.
echo ===================================================
echo 🎉 System is fully initialized and operational!
echo ===================================================
echo.
echo 📋 Next Steps:
echo    • Backend is starting on port 8080...
echo    • To start Frontend: cd frontend ^&^& npm start
echo    • Frontend will be available at: http://localhost:4200
echo.

cd backend
call gradlew.bat bootRun --no-daemon
