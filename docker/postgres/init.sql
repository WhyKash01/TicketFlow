-- Initialize separate databases for microservice isolation
CREATE DATABASE ticketflow_auth;
CREATE DATABASE ticketflow_catalog;
CREATE DATABASE ticketflow_booking;
CREATE DATABASE ticketflow_payment;
CREATE DATABASE ticketflow_notification;

GRANT ALL PRIVILEGES ON DATABASE ticketflow_auth TO ticketflow_user;
GRANT ALL PRIVILEGES ON DATABASE ticketflow_catalog TO ticketflow_user;
GRANT ALL PRIVILEGES ON DATABASE ticketflow_booking TO ticketflow_user;
GRANT ALL PRIVILEGES ON DATABASE ticketflow_payment TO ticketflow_user;
GRANT ALL PRIVILEGES ON DATABASE ticketflow_notification TO ticketflow_user;

