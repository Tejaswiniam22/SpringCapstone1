package com.project.demo.service;

import com.project.demo.model.RefreshToken;
import com.project.demo.model.User;
import com.project.demo.repository.RefreshTokenRepository;
import com.project.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RefreshTokenService.
 */
class RefreshTokenServiceTest {

    @Test
    void shouldFindRefreshTokenById() {
        RefreshTokenRepository repo = mock(RefreshTokenRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        RefreshTokenService svc = new RefreshTokenService(repo, userRepo, 7L * 24 * 60 * 60 * 1000);

        RefreshToken rt = new RefreshToken();
        rt.setToken("abc123");
        when(repo.findByToken("abc123")).thenReturn(Optional.of(rt));

        Optional<RefreshToken> out = svc.findByToken("abc123");
        assertTrue(out.isPresent());
        assertEquals("abc123", out.get().getToken());
    }

    @Test
    void shouldCreateRefreshTokenAndSave() {
        RefreshTokenRepository repo = mock(RefreshTokenRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        RefreshTokenService svc = new RefreshTokenService(repo, userRepo, 1000L * 60 * 60 * 24);

        User u = new User();
        u.setUsername("joe");
        when(userRepo.findByUsername("joe")).thenReturn(Optional.of(u));

        RefreshToken created = svc.createRefreshToken("joe");
        assertNotNull(created);
        assertEquals("joe", created.getUsername());
        assertNotNull(created.getToken());
        assertTrue(created.getExpiryDate().isAfter(Instant.now()));

        verify(repo).save(any(RefreshToken.class));
    }

    @Test
    void shouldReturnEmptyWhenTokenNotFound() {
        RefreshTokenRepository repo = mock(RefreshTokenRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        RefreshTokenService svc = new RefreshTokenService(repo, userRepo, 1000L);

        when(repo.findByToken("nope")).thenReturn(Optional.empty());
        Optional<RefreshToken> out = svc.findByToken("nope");
        assertTrue(out.isEmpty());
    }
}
