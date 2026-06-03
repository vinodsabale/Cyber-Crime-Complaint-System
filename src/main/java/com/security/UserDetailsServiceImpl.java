package com.security;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.entity.User;
import com.repository.UserRepository;

import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
 
    private final UserRepository userRepository;
 
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        com.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        boolean accountNonLocked = true;

        if (user.getLockedUntil() != null &&
            user.getLockedUntil().isAfter(LocalDateTime.now())) {

            accountNonLocked = false;
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getStatus() == com.entity.User.UserStatus.ACTIVE,
                true,
                true,
                accountNonLocked,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
 