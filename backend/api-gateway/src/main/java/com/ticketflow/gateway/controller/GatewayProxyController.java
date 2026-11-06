package com.ticketflow.gateway.controller;

import com.ticketflow.gateway.security.JwtValidator;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api")
public class GatewayProxyController {

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyController.class);
    private final RestTemplate restTemplate;
    private final JwtValidator jwtValidator;

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

    public enum AccessRequirement {
        PUBLIC,
        USER,
        ADMIN_ONLY
    }

    public GatewayProxyController(RestTemplate restTemplate, JwtValidator jwtValidator) {
        this.restTemplate = restTemplate;
        this.jwtValidator = jwtValidator;
    }

    @RequestMapping(value = "/**")
    public ResponseEntity<?> proxyRequest(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request) {

        // Complete CORS preflight requests directly at the gateway
        if (HttpMethod.OPTIONS.equals(method)) {
            return ResponseEntity.ok().build();
        }

        String requestUri = request.getRequestURI(); // e.g. /api/catalog/movies
        String queryString = request.getQueryString();

        // 1. Resolve destination microservice URL
        String targetBaseUrl = resolveTargetServiceUrl(requestUri);
        if (targetBaseUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "success", false,
                            "status", 404,
                            "error", "Not Found",
                            "message", "No downstream microservice configured for path: " + requestUri,
                            "path", requestUri
                    ));
        }

        // 2. Validate JWT if provided
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        Optional<Claims> claimsOpt = Optional.empty();
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            claimsOpt = jwtValidator.validateAndExtractClaims(token);
        }

        // 3. Evaluate Access Requirement (Public, Authenticated User, or Admin Only)
        AccessRequirement requirement = determineAccessRequirement(requestUri, method);

        if (requirement == AccessRequirement.USER && claimsOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "success", false,
                            "status", 401,
                            "error", "Unauthorized",
                            "message", "Authentication required. Please provide a valid Bearer token.",
                            "path", requestUri
                    ));
        }

        if (requirement == AccessRequirement.ADMIN_ONLY) {
            if (claimsOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of(
                                "success", false,
                                "status", 401,
                                "error", "Unauthorized",
                                "message", "Admin authentication required.",
                                "path", requestUri
                        ));
            }

            Claims claims = claimsOpt.get();
            String role = claims.get("role", String.class);
            if (role == null || (!role.equalsIgnoreCase("ROLE_ADMIN") && !role.equalsIgnoreCase("ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of(
                                "success", false,
                                "status", 403,
                                "error", "Forbidden",
                                "message", "Access denied: This endpoint requires ROLE_ADMIN privileges.",
                                "path", requestUri
                        ));
            }
        }

        // 4. Copy HTTP headers and SANITIZE incoming headers to prevent header spoofing
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String lower = headerName.toLowerCase();

            // Hop-by-hop headers to strip
            if (lower.equals("host") ||
                lower.equals("content-length") ||
                lower.equals("transfer-encoding") ||
                lower.equals("connection")) {
                continue;
            }

            // ANTI-SPOOFING: Strip any client-supplied identity headers
            if (lower.startsWith("x-user-") || lower.startsWith("x-auth-") || lower.startsWith("x-internal-")) {
                continue;
            }

            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        }

        // 5. Inject trusted, verified identity headers if authenticated
        claimsOpt.ifPresent(claims -> {
            headers.set("X-User-Id", claims.getSubject());
            if (claims.get("role") != null) {
                headers.set("X-User-Role", claims.get("role", String.class));
            }
            if (claims.get("email") != null) {
                headers.set("X-User-Email", claims.get("email", String.class));
            }
        });

        String targetUrl = targetBaseUrl + requestUri;
        if (queryString != null && !queryString.isBlank()) {
            targetUrl += "?" + queryString;
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        // 6. Forward request downstream
        try {
            log.info("Gateway proxying [{}] {} -> {}", method, requestUri, targetUrl);
            return restTemplate.exchange(URI.create(targetUrl), method, entity, byte[].class);
        } catch (HttpStatusCodeException ex) {
            log.warn("Downstream service responded with {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsByteArray());
        } catch (ResourceAccessException ex) {
            log.warn("Downstream service unreachable at {}: {}", targetUrl, ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "success", false,
                            "status", 503,
                            "error", "Service Unavailable",
                            "message", "Target microservice is offline at " + targetBaseUrl + ". Please ensure service is running.",
                            "path", requestUri
                    ));
        } catch (Exception ex) {
            log.error("Gateway error proxying request to {}: {}", targetUrl, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "success", false,
                            "status", 500,
                            "error", "Internal Gateway Error",
                            "message", "Gateway error: " + ex.getMessage(),
                            "path", requestUri
                    ));
        }
    }

    /**
     * Role-Based Access Control (RBAC) security policy evaluator.
     */
    public AccessRequirement determineAccessRequirement(String path, HttpMethod method) {
        // Public Auth routes
        if (path.startsWith("/api/auth/register") ||
            path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/refresh") ||
            path.startsWith("/api/auth/logout")) {
            return AccessRequirement.PUBLIC;
        }

        // Public Catalog read routes (Browsing movies, shows, theatres, screen seats layout)
        if (path.startsWith("/api/catalog")) {
            if (HttpMethod.GET.equals(method)) {
                return AccessRequirement.PUBLIC;
            } else {
                // Mutations (POST, PUT, DELETE) on catalog require ROLE_ADMIN
                return AccessRequirement.ADMIN_ONLY;
            }
        }

        // Public Booking read & calculation routes
        if ((path.matches("^/api/bookings/shows/\\d+/seats$") && HttpMethod.GET.equals(method)) ||
            path.startsWith("/api/bookings/auto-allocate")) {
            return AccessRequirement.PUBLIC;
        }

        // Admin Booking verification route (QR gate scanner)
        if (path.startsWith("/api/bookings/verify-ticket")) {
            return AccessRequirement.ADMIN_ONLY;
        }

        // Admin Payment routes (Compensating refund triggers & refund inspection)
        if (path.startsWith("/api/payments/refund") || path.matches("^/api/payments/.+/refunds$")) {
            return AccessRequirement.ADMIN_ONLY;
        }

        // All other booking, payment, notification, and /me operations require an authenticated USER
        return AccessRequirement.USER;
    }

    private String resolveTargetServiceUrl(String path) {
        if (path.startsWith("/api/auth")) {
            return authUrl;
        } else if (path.startsWith("/api/catalog")) {
            return catalogUrl;
        } else if (path.startsWith("/api/bookings")) {
            return bookingUrl;
        } else if (path.startsWith("/api/payments")) {
            return paymentUrl;
        } else if (path.startsWith("/api/notifications")) {
            return notificationUrl;
        }
        return null;
    }
}
