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

    public GatewayProxyController(RestTemplate restTemplate, JwtValidator jwtValidator) {
        this.restTemplate = restTemplate;
        this.jwtValidator = jwtValidator;
    }

    @RequestMapping(value = "/**")
    public ResponseEntity<?> proxyRequest(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request) {

        // Complete CORS preflight requests directly at gateway
        if (HttpMethod.OPTIONS.equals(method)) {
            return ResponseEntity.ok().build();
        }

        String requestUri = request.getRequestURI(); // e.g. /api/catalog/movies
        String queryString = request.getQueryString();

        // 1. Resolve destination microservice URL
        String targetBaseUrl = resolveTargetServiceUrl(requestUri);
        if (targetBaseUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", "No service route configured for path: " + requestUri
                    ));
        }

        // 2. Authentication Check for Protected Endpoints
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        Optional<Claims> claimsOpt = Optional.empty();
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            claimsOpt = jwtValidator.validateAndExtractClaims(token);
        }

        boolean requiresAuth = isProtectedRoute(requestUri, method);
        if (requiresAuth && claimsOpt.isEmpty()) {
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

        String targetUrl = targetBaseUrl + requestUri;
        if (queryString != null && !queryString.isBlank()) {
            targetUrl += "?" + queryString;
        }

        // 3. Copy headers and inject trusted user claims
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase("host") && 
                !headerName.equalsIgnoreCase("content-length") &&
                !headerName.equalsIgnoreCase("transfer-encoding") &&
                !headerName.equalsIgnoreCase("connection")) {
                headers.put(headerName, Collections.list(request.getHeaders(headerName)));
            }
        }

        // Inject claims downstream if authenticated
        claimsOpt.ifPresent(claims -> {
            headers.set("X-User-Id", claims.getSubject());
            if (claims.get("role") != null) {
                headers.set("X-User-Role", claims.get("role", String.class));
            }
            if (claims.get("email") != null) {
                headers.set("X-User-Email", claims.get("email", String.class));
            }
        });

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            log.info("Gateway forwarding [{}] {} -> {}", method, requestUri, targetUrl);
            return restTemplate.exchange(URI.create(targetUrl), method, entity, byte[].class);
        } catch (HttpStatusCodeException ex) {
            log.warn("Downstream service returned {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsByteArray());
        } catch (ResourceAccessException ex) {
            log.warn("Downstream service unreachable at {}: {}", targetUrl, ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "success", false,
                            "status", 503,
                            "error", "Service Unavailable",
                            "message", "The target microservice is currently offline at " + targetBaseUrl + ". Please start the service to handle requests.",
                            "path", requestUri
                    ));
        } catch (Exception ex) {
            log.error("Gateway error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Gateway error: " + ex.getMessage()
                    ));
        }
    }

    private boolean isProtectedRoute(String path, HttpMethod method) {
        // Public Auth routes
        if (path.startsWith("/api/auth/register") ||
            path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/refresh") ||
            path.startsWith("/api/auth/logout")) {
            return false;
        }

        // Public Catalog routes (Browsing movies, shows, theatres)
        if (path.startsWith("/api/catalog")) {
            return method != HttpMethod.GET; // Only mutations require auth, GET is public
        }

        // Browsing seat layout, auto-allocating, and temporary Redis seat locks are public
        if ((path.matches("^/api/bookings/shows/\\d+/seats$") && method == HttpMethod.GET) ||
            path.startsWith("/api/bookings/lock-seats") ||
            path.startsWith("/api/bookings/auto-allocate")) {
            return false;
        }

        // Final booking creation, payments, and /me require authentication
        if (path.startsWith("/api/bookings") || path.startsWith("/api/payments") || path.equals("/api/auth/me")) {
            return true;
        }

        return false;
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
