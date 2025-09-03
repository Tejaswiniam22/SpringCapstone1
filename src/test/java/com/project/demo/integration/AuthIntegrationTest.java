package com.project.demo.integration;

import com.project.demo.dto.request.LoginRequest;
import com.project.demo.dto.request.RegisterRequest;
import com.project.demo.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests: start the app context on a random port and use TestRestTemplate
 * These tests will touch the file-backed repositories (data/users.json & data/refresh_tokens.json).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() {
        LoginRequest bad = new LoginRequest("nonexistent_" + UUID.randomUUID(), "nope");
        ResponseEntity<String> resp = restTemplate.postForEntity("/auth/login", bad, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void shouldRegisterNewUserSuccessfully() {
        String uname = "integration_" + UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest(uname, "password123", null);

        ResponseEntity<ApiResponse> resp =
                restTemplate.postForEntity("/auth/register", req, ApiResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isSuccess());
    }
}
