package com.project.demo.controller;

import com.project.demo.dto.request.LoginRequest;
import com.project.demo.dto.request.RegisterRequest;
import com.project.demo.dto.response.ApiResponse;
import com.project.demo.dto.response.AuthResponse;
import com.project.demo.service.AuthService;
import com.project.demo.service.LoginAuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController - lightweight, mocking all dependencies.
 */
class AuthControllerTest {

    @Test
    void shouldReturnAuthResponseWhenLoginIsSuccessful() {
        AuthService authService = mock(AuthService.class);
        LoginAuditService loginAuditService = mock(LoginAuditService.class);
        AuthController controller = new AuthController(authService, loginAuditService);

        LoginRequest request = new LoginRequest("user1", "pass1");
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        AuthResponse authResponse = new AuthResponse("access-token-xyz", "refresh-token-abc");
        when(authService.login(any())).thenReturn(authResponse);

        // call
        var resp = controller.login(request, httpRequest, httpResponse);

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCodeValue());
        ApiResponse<AuthResponse> body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertNotNull(body.getData());
        assertEquals("access-token-xyz", body.getData().getAccessToken());

        verify(authService, times(1)).login(any());
        // controller should attempt to store refresh cookie via response.addCookie(...)
        verify(httpResponse, atLeastOnce()).addCookie(any());
        // login audit should be invoked
        verify(loginAuditService, times(1)).saveLoginLog(any());
    }

    @Test
    void shouldThrowWhenRegisterFails() {
        AuthService authService = mock(AuthService.class);
        LoginAuditService loginAuditService = mock(LoginAuditService.class);
        AuthController controller = new AuthController(authService, loginAuditService);

        RegisterRequest bad = new RegisterRequest("", "", null);
        when(authService.register(any())).thenThrow(new IllegalArgumentException("Invalid data"));

        // Because we're calling controller method directly (no Spring advice), exception propagates.
        assertThrows(IllegalArgumentException.class, () -> controller.register(bad));
        verify(authService).register(any());
    }
}
