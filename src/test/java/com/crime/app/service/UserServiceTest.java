package com.crime.app.service;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dto.request.RegisterRequest;
import com.entity.User;
import com.exception.DuplicateResourceException;
import com.exception.ResourceNotFoundException;
import com.repository.UserRepository;
import com.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock UserRepository  userRepo;
    @Mock PasswordEncoder encoder;
    @InjectMocks UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).fullName("Test Citizen")
                .email("test@test.com").phone("9876543210")
                .password("encodedPassword")
                .role(User.Role.CITIZEN)
                .status(User.UserStatus.ACTIVE).build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void register_shouldCreateUser_whenValidRequest() {
        RegisterRequest req = RegisterRequest.builder()
                .fullName("New User").email("new@test.com")
                .phone("9999999999").password("Pass@1234")
                .confirmPassword("Pass@1234").build();

        when(userRepo.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepo.existsByPhone("9999999999")).thenReturn(false);
        when(encoder.encode("Pass@1234")).thenReturn("encodedPass");
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        User result = userService.register(req);
        assertNotNull(result);
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email exists")
    void register_shouldThrow_whenEmailExists() {
        RegisterRequest req = RegisterRequest.builder()
                .fullName("Test").email("test@test.com")
                .phone("9999999999").password("Pass@1234")
                .confirmPassword("Pass@1234").build();

        when(userRepo.existsByEmail("test@test.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> userService.register(req));
        verify(userRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when passwords do not match")
    void register_shouldThrow_whenPasswordsMismatch() {
        RegisterRequest req = RegisterRequest.builder()
                .fullName("Test").email("new@test.com")
                .phone("9999999999").password("Pass@1234")
                .confirmPassword("Different@123").build();

        when(userRepo.existsByEmail(any())).thenReturn(false);
        when(userRepo.existsByPhone(any())).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test
    @DisplayName("Should return user by ID when found")
    void getById_shouldReturnUser() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        User result = userService.getById(1L);
        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void getById_shouldThrow_whenNotFound() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getById(99L));
    }

    @Test
    @DisplayName("Should change password when old password is correct")
    void changePassword_shouldUpdate_whenOldPasswordCorrect() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(encoder.matches("OldPass@1", "encodedPassword")).thenReturn(true);
        when(encoder.encode("NewPass@1")).thenReturn("newEncoded");
        when(userRepo.save(any())).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.changePassword(1L, "OldPass@1", "NewPass@1"));
        verify(userRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw when old password is incorrect")
    void changePassword_shouldThrow_whenOldPasswordWrong() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(encoder.matches("WrongPass", "encodedPassword")).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(1L, "WrongPass", "NewPass@1"));
    }

    @Test
    @DisplayName("Should delete user when exists")
    void delete_shouldDelete_whenExists() {
        when(userRepo.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> userService.delete(1L));
        verify(userRepo, times(1)).deleteById(1L);
    }
}