package com.project.demo.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil.
 * We create JwtUtil directly with a test secret (must be >= 32 bytes for HS256).
 */
class JwtUtilTest {

    @Test
    void shouldGenerateAndValidateToken() {
        String secret = "mysecretkeythatislongenoughtobeatleast32chars!";
        long expMs = 60 * 60 * 1000L;
        JwtUtil jwtUtil = new JwtUtil(secret, expMs);

        String token = jwtUtil.generateToken("tester");
        assertNotNull(token);
        assertTrue(jwtUtil.isValid(token));
        assertEquals("tester", jwtUtil.extractUsername(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        String secret = "mysecretkeythatislongenoughtobeatleast32chars!";
        JwtUtil jwtUtil = new JwtUtil(secret, 1000L);
        assertFalse(jwtUtil.isValid("this.is.not.a.jwt"));
    }
}
