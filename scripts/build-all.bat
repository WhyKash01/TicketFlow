@echo off
echo ========================================================
echo   TicketFlow - Building all Spring Boot Microservices
echo ========================================================
cd ..\backend
mvn clean install -DskipTests
pause

