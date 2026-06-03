package com.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class JwtResponse {
   private String accessToken;
   private String refreshToken;
   private String tokenType = "Bearer";
   private Long expiresIn;
   private String email;
   private String role;
   private String fullName;
}
