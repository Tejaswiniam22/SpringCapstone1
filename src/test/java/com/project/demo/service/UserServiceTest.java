package com.project.demo.service;

import com.project.demo.model.User;
import com.project.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
class UserServiceTest {

    @Test
    void shouldFindUserByUsername() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService svc = new UserService(repo, encoder);

        User u = new User();
        u.setUsername("alice");
        u.setPassword("raw");
        when(repo.findByUsername("alice")).thenReturn(Optional.of(u));

        var loaded = svc.loadUserByUsername("alice");
        assertNotNull(loaded);
        assertEquals("alice", loaded.getUsername());
        verify(repo).findByUsername("alice");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService svc = new UserService(repo, encoder);

        when(repo.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> svc.loadUserByUsername("missing"));
    }

    @Test
    void shouldRegisterNewUser() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService svc = new UserService(repo, encoder);

        User u = new User();
        u.setUsername("newuser");
        u.setPassword("pw");

        when(repo.findByUsername("newuser")).thenReturn(Optional.empty());
        when(encoder.encode("pw")).thenReturn("encoded-pw");

        String msg = svc.register(u);
        assertEquals("User registered successfully!", msg);
        verify(repo).save(u);
        // password should have been encoded
        assertEquals("encoded-pw", u.getPassword());
    }

    @Test
    void shouldRejectDuplicateUserOnRegister() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService svc = new UserService(repo, encoder);

        User existing = new User();
        existing.setUsername("taken");
        when(repo.findByUsername("taken")).thenReturn(Optional.of(existing));

        User candidate = new User();
        candidate.setUsername("taken");
        candidate.setPassword("x");

        assertThrows(IllegalStateException.class, () -> svc.register(candidate));
    }
}
