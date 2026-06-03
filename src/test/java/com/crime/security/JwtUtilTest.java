package com.crime.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails mockUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "smart-crime-secret-key-2024-very-long-secure-key-for-hs256-algorithm");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L);

        mockUser = new User("officer@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_OFFICER")));
    }

    @Test
    @DisplayName("Should generate valid access token")
    void generateAccessToken_shouldReturnNonNullToken() {
        String token = jwtUtil.generateAccessToken(mockUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should extract correct username from token")
    void extractUsername_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateAccessToken(mockUser);
        String email = jwtUtil.extractUsername(token);
        assertEquals("officer@test.com", email);
    }

    @Test
    @DisplayName("Should extract correct role from token")
    void extractRole_shouldReturnCorrectRole() {
        String token = jwtUtil.generateAccessToken(mockUser);
        String role  = jwtUtil.extractRole(token);
        assertEquals("ROLE_OFFICER", role);
    }

    @Test
    @DisplayName("Token should not be expired immediately after creation")
    void isExpired_shouldReturnFalse_forFreshToken() {
        String token = jwtUtil.generateAccessToken(mockUser);
        assertFalse(jwtUtil.isExpired(token));
    }

    @Test
    @DisplayName("Should validate token successfully")
    void validateToken_shouldReturnTrue_forValidToken() {
        String token = jwtUtil.generateAccessToken(mockUser);
        assertTrue(jwtUtil.validateToken(token, mockUser));
    }

    @Test
    @DisplayName("Should reject token for different user")
    void validateToken_shouldReturnFalse_forDifferentUser() {
        String token = jwtUtil.generateAccessToken(mockUser);
        UserDetails otherUser = new User("other@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_CITIZEN")));
        assertFalse(jwtUtil.validateToken(token, otherUser));
    }

    @Test
    @DisplayName("Should generate different refresh token")
    void generateRefreshToken_shouldReturnToken() {
        String access  = jwtUtil.generateAccessToken(mockUser);
        String refresh = jwtUtil.generateRefreshToken(mockUser);
        assertNotNull(refresh);
        assertNotEquals(access, refresh);
    }

    @Test
    @DisplayName("Should return configured expiration value")
    void getExpiration_shouldReturnConfiguredValue() {
        assertEquals(86400000L, jwtUtil.getExpiration());
    }

    @Test
    @DisplayName("Access token should have correct subject")
    void accessToken_shouldHaveCorrectSubject() {
        String token = jwtUtil.generateAccessToken(mockUser);
        assertEquals(mockUser.getUsername(), jwtUtil.extractUsername(token));
    }
}