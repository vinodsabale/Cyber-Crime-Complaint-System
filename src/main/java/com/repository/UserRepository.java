package com.repository;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.entity.User;
 
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
 
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
 
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<User> searchUsers(@Param("q") String query, Pageable pageable);
 
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.email = :email")
    void incrementFailedAttempts(@Param("email") String email);
 
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = NULL WHERE u.email = :email")
    void resetFailedAttempts(@Param("email") String email);

}
 