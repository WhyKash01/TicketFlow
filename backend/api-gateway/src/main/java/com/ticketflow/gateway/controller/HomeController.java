package com.ticketflow.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @Value("${services.auth:http://localhost:8085}")
    private String authUrl;

    @Value("${services.catalog:http://localhost:8081}")
    private String catalogUrl;

    @Value("${services.booking:http://localhost:8082}")
    private String bookingUrl;

    @Value("${services.payment:http://localhost:8083}")
    private String paymentUrl;

    @Value("${services.notification:http://localhost:8084}")
    private String notificationUrl;

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", "TicketFlow API Gateway");
        response.put("status", "ONLINE");
        response.put("port", 8080);
        response.put("type", "Spring Boot Reverse Proxy & Security Gateway");

        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("/api/auth/**", authUrl + " (Auth & User Service)");
        routes.put("/api/catalog/**", catalogUrl + " (Catalog Service)");
        routes.put("/api/bookings/**", bookingUrl + " (Booking Service)");
        routes.put("/api/payments/**", paymentUrl + " (Payment Service)");
        routes.put("/api/notifications/**", notificationUrl + " (Notification Service)");
        response.put("routes", routes);

        response.put("health", "/actuator/health");
        return response;
    }
}
