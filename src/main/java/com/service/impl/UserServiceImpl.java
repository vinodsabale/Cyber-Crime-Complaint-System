package com.service.impl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dto.request.RegisterRequest;
import com.entity.User;
import com.exception.DuplicateResourceException;
import com.exception.ResourceNotFoundException;
import com.repository.UserRepository;
import com.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
 
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
 
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
 
    @Override
    public User register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("Email already registered: " + req.getEmail());
        if (userRepo.existsByPhone(req.getPhone()))
            throw new DuplicateResourceException("Phone already registered: " + req.getPhone());
        if (!req.getPassword().equals(req.getConfirmPassword()))
            throw new IllegalArgumentException("Passwords do not match");
 
        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .address(req.getAddress())
                .city(req.getCity())
                .state(req.getState())
                .pincode(req.getPincode())
                .aadharNumber(req.getAadharNumber())
                .role(User.Role.CITIZEN)
                .status(User.UserStatus.ACTIVE)
                .build();
 
        log.info("New user registered: {}", req.getEmail());
        return userRepo.save(user);
    }
 
    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
 
    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<User> getAll(Pageable pageable) {
        return userRepo.findAll(pageable);
    }
 
    @Override
    public User updateProfile(Long id, User details) {
        User u = getById(id);
        u.setFullName(details.getFullName());
        u.setPhone(details.getPhone());
        u.setAddress(details.getAddress());
        u.setCity(details.getCity());
        u.setState(details.getState());
        u.setPincode(details.getPincode());
        return userRepo.save(u);
    }
 
    @Override
    public void changePassword(Long id, String oldPwd, String newPwd) {
        User u = getById(id);
        if (!encoder.matches(oldPwd, u.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect");
        u.setPassword(encoder.encode(newPwd));
        userRepo.save(u);
    }
 
    @Override
    public void delete(Long id) {
        if (!userRepo.existsById(id))
            throw new ResourceNotFoundException("User not found");
        userRepo.deleteById(id);
    }
 
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }
}