package com.crime.config;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.security.JwtUtil;
import com.security.UserDetailsServiceImpl;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("Security Configuration Tests")
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;
    @MockBean  JwtUtil jwtUtil;
    @MockBean  UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Public login page should be accessible without auth")
    void loginPage_shouldBePublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public register page should be accessible without auth")
    void registerPage_shouldBePublic() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Track complaint page should be accessible without auth")
    void trackPage_shouldBePublic() throws Exception {
        mockMvc.perform(get("/complaints/track"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Dashboard should redirect to login without auth")
    void dashboard_shouldRedirectToLogin_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("API endpoint should return 401 without JWT")
    void apiEndpoint_shouldReturn401_withoutJwt() throws Exception {
        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("JWT filter should authenticate valid token")
    void jwtFilter_shouldAuthenticate_withValidToken() throws Exception {
        UserDetails mockUser = new org.springframework.security.core.userdetails.User(
                "officer@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_OFFICER")));

        when(jwtUtil.extractUsername("valid-token")).thenReturn("officer@test.com");
        when(jwtUtil.validateToken("valid-token", mockUser)).thenReturn(true);
        when(userDetailsService.loadUserByUsername("officer@test.com")).thenReturn(mockUser);

        mockMvc.perform(get("/api/complaints")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }
}