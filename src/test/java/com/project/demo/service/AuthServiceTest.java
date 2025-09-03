package com.project.demo.service;

import com.project.demo.dto.request.LoginRequest;
import com.project.demo.dto.request.RegisterRequest;
import com.project.demo.dto.response.AuthResponse;
import com.project.demo.model.RefreshToken;
import com.project.demo.model.User;
import com.project.demo.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService (JWT generation, register, login).
 */
class AuthServiceTest {

    @Test
    void shouldLoginSuccessfully() {
        UserService userSvc = mock(UserService.class);
        JwtUtil jwt = mock(JwtUtil.class);
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        RefreshTokenService rtService = mock(RefreshTokenService.class);

        AuthService auth = new AuthService(userSvc, jwt, authManager, rtService);

        LoginRequest req = new LoginRequest("bob", "pw");
        Authentication authObj = mock(Authentication.class);
        when(authObj.getName()).thenReturn("bob");
        when(authManager.authenticate(any())).thenReturn(authObj);

        when(jwt.generateToken("bob")).thenReturn("jwt-token");
        RefreshToken rt = new RefreshToken();
        rt.setToken("rt-1");
        when(rtService.createRefreshToken("bob")).thenReturn(rt);

        AuthResponse resp = auth.login(req);
        assertNotNull(resp);
        assertEquals("jwt-token", resp.getAccessToken());
        assertEquals("rt-1", resp.getRefreshToken());
    }

    @Test
    void shouldThrowOnBadCredentials() {
        UserService userSvc = mock(UserService.class);
        JwtUtil jwt = mock(JwtUtil.class);
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        RefreshTokenService rtService = mock(RefreshTokenService.class);

        AuthService auth = new AuthService(userSvc, jwt, authManager, rtService);

        LoginRequest req = new LoginRequest("bob", "wrong");

        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> auth.login(req));
    }

    @Test
    void shouldRegisterNewUserDelegatingToUserService() {
        UserService userSvc = mock(UserService.class);
        JwtUtil jwt = mock(JwtUtil.class);
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        RefreshTokenService rtService = mock(RefreshTokenService.class);

        AuthService auth = new AuthService(userSvc, jwt, authManager, rtService);

        RegisterRequest req = new RegisterRequest("newu", "password", null);
        when(userSvc.register(any())).thenReturn("User registered successfully!");

        String res = auth.register(req);
        assertEquals("User registered successfully!", res);
        verify(userSvc).register(any());
    }
}
