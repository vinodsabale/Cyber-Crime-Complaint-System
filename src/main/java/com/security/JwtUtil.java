package com.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
 
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
 
@Component
public class JwtUtil {
 
    @Value("${app.jwt.secret}")
    private String secret;
 
    @Value("${app.jwt.expiration}")
    private long expiration;
 
    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;
 
    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
 
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        return buildToken(claims, userDetails.getUsername(), expiration);
    }
 
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails.getUsername(), refreshExpiration);
    }
 
    private String buildToken(Map<String, Object> claims, String subject, long exp) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + exp))
                .signWith(key())
                .compact();
    }
 
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
 
    public String extractUsername(String token) {
        return claims(token).getSubject();
    }
 
    public String extractRole(String token) {
        return claims(token).get("role", String.class);
    }
 
    public boolean isExpired(String token) {
        return claims(token).getExpiration().before(new Date());
    }
 
    public long getExpiration() {
        return expiration;
    }
 
    private Claims claims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
 
 