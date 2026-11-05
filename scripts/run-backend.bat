@echo off
echo ========================================================
echo   TicketFlow - Launching Backend Services
echo ========================================================

REM 1. Ensure Docker infra is up
echo Checking Docker containers...
cd /d "%~dp0..\docker"
docker compose up -d

REM 2. Launch each microservice in its own window
cd /d "%~dp0..\backend"

echo Starting Auth & User Service (Port 8085)...
start "TicketFlow - Auth Service [8085]" cmd /k "mvn spring-boot:run -pl auth-service"

timeout /t 3 /nobreak >nul

echo Starting API Gateway (Port 8080)...
start "TicketFlow - API Gateway [8080]" cmd /k "mvn spring-boot:run -pl api-gateway"

timeout /t 3 /nobreak >nul

echo Starting Catalog Service (Port 8081)...
start "TicketFlow - Catalog Service [8081]" cmd /k "mvn spring-boot:run -pl catalog-service"

echo Starting Booking Service (Port 8082)...
start "TicketFlow - Booking Service [8082]" cmd /k "mvn spring-boot:run -pl booking-service"

echo Starting Payment Service (Port 8083)...
start "TicketFlow - Payment Service [8083]" cmd /k "mvn spring-boot:run -pl payment-service"

echo Starting Notification Service (Port 8084)...
start "TicketFlow - Notification Service [8084]" cmd /k "mvn spring-boot:run -pl notification-service"

echo.
echo ========================================================
echo   All backend microservices launched in separate tabs!
echo   API Gateway available at: http://localhost:8080
echo ========================================================
pause

