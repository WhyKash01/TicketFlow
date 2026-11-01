package com.ticketflow.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
@RestController
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @GetMapping("/")
    public Map<String, Object> gatewayStatus() {
        return Map.of(
                "application", "TicketFlow API Gateway",
                "status", "ONLINE",
                "port", 8080,
                "type", "Basic Spring MVC Reverse Proxy Gateway",
                "routes", Map.of(
                        "/api/catalog/**", "http://localhost:8081 (Catalog Service)",
                        "/api/bookings/**", "http://localhost:8082 (Booking Service)",
                        "/api/payments/**", "http://localhost:8083 (Payment Service)",
                        "/api/notifications/**", "http://localhost:8084 (Notification Service)"
                ),
                "health", "/actuator/health"
        );
    }
}
