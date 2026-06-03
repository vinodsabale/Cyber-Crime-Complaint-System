package com.service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.dto.request.RegisterRequest;
import com.entity.User;
 
public interface UserService {
    User register(RegisterRequest req);
    User getById(Long id);
    User getByEmail(String email);
    Page<User> getAll(Pageable pageable);
    User updateProfile(Long id, User details);
    void changePassword(Long id, String oldPwd, String newPwd);
    void delete(Long id);
    boolean existsByEmail(String email);
 
}
 
 