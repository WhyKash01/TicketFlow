package com.ticketflow.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @GetMapping("/")
    public Map<String, Object> gatewayHome() {
        return Map.of(
                "service", "TicketFlow API Gateway",
                "status", "UP",
                "port", 8080,
                "endpoints", List.of(
                        "/api/catalog/** (Catalog Service)",
                        "/api/bookings/** (Booking Service)",
                        "/api/payments/** (Payment Service)",
                        "/api/notifications/** (Notification Service)",
                        "/actuator/health (Health Check)",
                        "/actuator/gateway/routes (Gateway Routing Table)"
                )
        );
    }
}
