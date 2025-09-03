package com.project.demo.service;

import com.project.demo.dto.request.LoginRequest;
import com.project.demo.dto.request.RegisterRequest;
import com.project.demo.dto.response.AuthResponse;
import com.project.demo.model.RefreshToken;
import com.project.demo.model.User;
import com.project.demo.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserService userService, JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }

    public String register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        //user.setEmail(request.getEmail());

        return userService.register(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            String accessToken = jwtUtil.generateToken(authentication.getName());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authentication.getName());

            return new AuthResponse(accessToken, refreshToken.getToken());
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(rt -> {
                    String username = rt.getUsername();
                    refreshTokenService.deleteByToken(refreshToken);

                    String newAccessToken = jwtUtil.generateToken(username);
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(username);

                    return new AuthResponse(newAccessToken, newRefreshToken.getToken());
                })
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.deleteByToken(refreshToken);
        }
    }
}
