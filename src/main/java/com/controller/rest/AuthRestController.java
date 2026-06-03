package com.controller.rest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dto.request.LoginRequest;
import com.dto.request.RegisterRequest;
import com.dto.response.ApiResponse;
import com.dto.response.JwtResponse;
import com.entity.User;
import com.security.JwtUtil;
import com.security.UserDetailsServiceImpl;
import com.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
 
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth API", description = "Register, Login, Refresh Token")
public class AuthRestController {
 
    private final AuthenticationManager authManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
 
    @PostMapping("/register")
    @Operation(summary = "Register new citizen account")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User registered: " + user.getEmail(),
                        "Registration successful"));
    }
 
    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(ApiResponse.error("Account is locked. Try again later."));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Account is disabled"));
        }
 
        UserDetails ud = userDetailsService.loadUserByUsername(req.getEmail());
        String accessToken  = jwtUtil.generateAccessToken(ud);
        String refreshToken = jwtUtil.generateRefreshToken(ud);
        User user = userService.getByEmail(req.getEmail());
 
        JwtResponse jwt = JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
 
        log.info("User logged in: {}", req.getEmail());
        return ResponseEntity.ok(ApiResponse.success(jwt, "Login successful"));
    }
 
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@RequestParam String refreshToken) {
        try {
            String email = jwtUtil.extractUsername(refreshToken);
            UserDetails ud = userDetailsService.loadUserByUsername(email);
            if (!jwtUtil.isExpired(refreshToken)) {
                String newAccess = jwtUtil.generateAccessToken(ud);
                JwtResponse jwt = JwtResponse.builder()
                        .accessToken(newAccess)
                        .refreshToken(refreshToken)
                        .tokenType("Bearer")
                        .expiresIn(jwtUtil.getExpiration())
                        .email(email)
                        .build();
                return ResponseEntity.ok(ApiResponse.success(jwt, "Token refreshed"));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Refresh token expired"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid refresh token"));
        }
    }
 
    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public ResponseEntity<ApiResponse<User>> me(@AuthenticationPrincipal UserDetails ud) {
        User user = userService.getByEmail(ud.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
 
 