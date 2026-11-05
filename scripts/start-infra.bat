@echo off
echo ========================================================
echo   TicketFlow - Starting Infrastructure (Postgres, Redis, RabbitMQ)
echo ========================================================
cd ..\docker
docker compose up -d
echo.
echo Infrastructure containers started!
echo - PostgreSQL: localhost:5432
echo - pgAdmin 4 Web GUI: http://localhost:5050 (admin@ticketflow.com / admin)
echo - Redis: localhost:6379
echo - RabbitMQ AMQP: localhost:5672
echo - RabbitMQ Management UI: http://localhost:15672 (guest/guest)
pause

