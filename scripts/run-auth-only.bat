@echo off
echo ========================================================
echo   TicketFlow - Starting Auth Service + API Gateway
echo ========================================================

cd /d "%~dp0..\docker"
docker compose up -d

cd /d "%~dp0..\backend"

echo Starting Auth & User Service on Port 8085...
start "TicketFlow - Auth Service [8085]" cmd /k "mvn spring-boot:run -pl auth-service"

timeout /t 3 /nobreak >nul

echo Starting API Gateway on Port 8080...
start "TicketFlow - API Gateway [8080]" cmd /k "mvn spring-boot:run -pl api-gateway"

echo.
echo Auth Service (8085) and Gateway (8080) started!
echo Test endpoint: http://localhost:8080/api/auth/register
pause

