package com.ticketflow.gateway.controller;

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

    @Value("${services.catalog:http://localhost:8081}")
    private String catalogUrl;

    @Value("${services.booking:http://localhost:8082}")
    private String bookingUrl;

    @Value("${services.payment:http://localhost:8083}")
    private String paymentUrl;

    @Value("${services.notification:http://localhost:8084}")
    private String notificationUrl;

    public GatewayProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/**")
    public ResponseEntity<?> proxyRequest(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request) {

        String requestUri = request.getRequestURI(); // e.g. /api/catalog/movies
        String queryString = request.getQueryString();

        // Determine destination service base URL
        String targetBaseUrl = resolveTargetServiceUrl(requestUri);
        if (targetBaseUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", "No service route configured for path: " + requestUri
                    ));
        }

        String targetUrl = targetBaseUrl + requestUri;
        if (queryString != null && !queryString.isBlank()) {
            targetUrl += "?" + queryString;
        }

        // Copy incoming headers
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase("host") && !headerName.equalsIgnoreCase("content-length")) {
                headers.put(headerName, Collections.list(request.getHeaders(headerName)));
            }
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            log.info("Gateway forwarding [{}] {} -> {}", method, requestUri, targetUrl);
            return restTemplate.exchange(URI.create(targetUrl), method, entity, byte[].class);
        } catch (HttpStatusCodeException ex) {
            log.warn("Downstream service returned {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
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

    private String resolveTargetServiceUrl(String path) {
        if (path.startsWith("/api/catalog")) {
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
