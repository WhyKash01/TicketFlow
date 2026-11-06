package com.ticketflow.gateway;

import com.ticketflow.gateway.controller.GatewayProxyController;
import com.ticketflow.gateway.security.JwtValidator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GatewayProxyController.class)
@Import(JwtValidator.class)
class GatewayProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private SecretKey key;

    @BeforeEach
    void setUp() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String generateToken(String userId, String role, String email) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Public Route: Browsing catalog movies succeeds without token")
    void testPublicRouteCatalogAllowedWithoutToken() throws Exception {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok("[]".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/api/catalog/movies"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected Route: Initiating booking without token returns 401 Unauthorized")
    void testBookingWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/bookings/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"showId\": 1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("Protected Route: Processing payment with invalid/malformed token returns 401")
    void testPaymentWithForgedTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/payments/process")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer forged-fake-token-xyz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 500}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("RBAC: Customer attempting to create a movie returns 403 Forbidden")
    void testCustomerCannotCreateMovie() throws Exception {
        String customerToken = generateToken("101", "ROLE_CUSTOMER", "customer@ticketflow.com");

        mockMvc.perform(post("/api/catalog/movies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Hacker Movie\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("RBAC: Admin user can create a movie (returns 200 forwarded)")
    void testAdminCanCreateMovie() throws Exception {
        String adminToken = generateToken("1", "ROLE_ADMIN", "admin@ticketflow.com");

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok("{\"id\": 1}".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(post("/api/catalog/movies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"New Blockbuster\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Security Anti-Spoofing: Gateway sanitizes client-supplied X-User-* headers and injects verified token claims")
    void testHeaderSanitizationAndInjection() throws Exception {
        String customerToken = generateToken("user_42", "ROLE_CUSTOMER", "genuine@ticketflow.com");

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok("{\"success\": true}".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(post("/api/bookings/initiate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        // Attempt malicious header spoofing
                        .header("X-User-Id", "spoofed_admin_id")
                        .header("X-User-Role", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"showId\": 10}"))
                .andExpect(status().isOk());

        ArgumentCaptor<HttpEntity<byte[]>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), entityCaptor.capture(), eq(byte[].class));

        HttpHeaders forwardedHeaders = entityCaptor.getValue().getHeaders();
        // Malicious spoofed role must NOT exist; verified token values must prevail
        assertThat(forwardedHeaders.getFirst("X-User-Id")).isEqualTo("user_42");
        assertThat(forwardedHeaders.getFirst("X-User-Role")).isEqualTo("ROLE_CUSTOMER");
        assertThat(forwardedHeaders.getFirst("X-User-Email")).isEqualTo("genuine@ticketflow.com");
    }

    @Test
    @DisplayName("Resilience: Downstream service offline returns clean 503 JSON")
    void testDownstreamServiceOfflineReturns503() throws Exception {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenThrow(new ResourceAccessException("Connection refused: connect"));

        mockMvc.perform(get("/api/catalog/movies"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(503)))
                .andExpect(jsonPath("$.error", is("Service Unavailable")));
    }
}

