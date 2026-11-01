package com.ticketflow.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", "TicketFlow API Gateway");
        response.put("status", "ONLINE");
        response.put("port", 8080);
        response.put("type", "Basic Spring Boot MVC Gateway");

        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("/api/catalog/**", "http://localhost:8081 (Catalog Service)");
        routes.put("/api/bookings/**", "http://localhost:8082 (Booking Service)");
        routes.put("/api/payments/**", "http://localhost:8083 (Payment Service)");
        routes.put("/api/notifications/**", "http://localhost:8084 (Notification Service)");
        response.put("routes", routes);

        response.put("health", "/actuator/health");
        return response;
    }
}

