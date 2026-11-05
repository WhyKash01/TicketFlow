@echo off
echo ========================================================
echo   TicketFlow - PostgreSQL Shell (ticketflow_auth)
echo ========================================================
echo Connecting to ticketflow_auth database...
echo Useful commands:
echo   \dt                - List all tables
echo   \d users           - Show structure of users table
echo   SELECT * FROM users;
echo   \q                 - Quit shell
echo ========================================================
echo.
docker exec -it ticketflow-postgres psql -U ticketflow_user -d ticketflow_auth

