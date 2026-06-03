package com.dto.request;
import jakarta.validation.constraints.Email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {
 
    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100)
    private String fullName;
 
    @NotBlank
    @Email(message = "Invalid email")
    private String email;
 
    @NotBlank
    @Size(min = 8, message = "Password min 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must have uppercase, lowercase, digit and special character"
    )
    private String password;
 
    @NotBlank(message = "Confirm password required")
    private String confirmPassword;
 
    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String phone;
 
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String aadharNumber;
}
 
