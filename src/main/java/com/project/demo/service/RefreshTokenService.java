package com.project.demo.service;

import com.project.demo.exception.RefreshTokenExpiredException;
import com.project.demo.exception.ResourceNotFoundException;
import com.project.demo.model.RefreshToken;
import com.project.demo.repository.RefreshTokenRepository;
import com.project.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final long refreshTokenExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            @Value("${jwt.refresh-token.expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(String username) {
        // make sure the user exists — if not, throw ResourceNotFoundException
        userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpirationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.deleteByToken(token.getToken());
            throw new RefreshTokenExpiredException("Refresh token was expired. Please sign in again.");
        }
        return token;
    }

    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
