package com.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.entity.User;
import com.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j

public class DataSeeder {
 
    @Bean
    @Profile("!test")
    public CommandLineRunner initApp(UserRepository userRepo,
                                     PasswordEncoder encoder) {
        return args -> {
            // Check if any ADMIN exists
        	boolean adminExists = userRepo.findAll().stream()
        	        .anyMatch(u -> u.getRole() == User.Role.ADMIN);
 
            if (!adminExists) {
                log.info("=================================================");
                log.info("No ADMIN found. Creating default admin account...");
                log.info("Please change password after first login!");
 
                // Read from environment variables or system properties
                String adminEmail    = System.getenv("ADMIN_EMAIL")    != null
                        ? System.getenv("ADMIN_EMAIL")    : "admin@crime.gov.in";
                String adminPassword = System.getenv("ADMIN_PASSWORD") != null
                        ? System.getenv("ADMIN_PASSWORD") : "Admin@1234";
                String adminName     = System.getenv("ADMIN_NAME")     != null
                        ? System.getenv("ADMIN_NAME")     : "System Admin";
                String adminPhone    = System.getenv("ADMIN_PHONE")    != null
                        ? System.getenv("ADMIN_PHONE")    : "9000000000";
 
                userRepo.save(User.builder()
                        .fullName(adminName)
                        .email(adminEmail)
                        .password(encoder.encode(adminPassword))
                        .phone(adminPhone)
                        .role(User.Role.ADMIN)
                        .status(User.UserStatus.ACTIVE)
                        .emailVerified(true)
                        .build());
 
                log.info("Admin created → Email: {}", adminEmail);
                log.info("=================================================");
            } else {
                log.info("Admin already exists. Skipping seed.");
            }
 
            log.info("App started. Register via: http://localhost:8088/register");
            log.info("Login via:                http://localhost:8088/login");
            log.info("Swagger UI:               http://localhost:8088/swagger-ui.html");
        };
    }
}
 