package com.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.entity.Officer;
 
@Repository
public interface OfficerRepository extends JpaRepository<Officer, Long> {
 
    Optional<Officer> findByBadgeNumber(String badge);
    boolean existsByBadgeNumber(String badge);
    boolean existsByEmail(String email);
 
    @Query("SELECT o FROM Officer o WHERE o.status = 'ACTIVE' AND o.currentCaseLoad < o.maxCaseLoad ORDER BY o.currentCaseLoad ASC")
    List<Officer> findAvailable();
 
    @Query("SELECT o FROM Officer o WHERE " +
           "LOWER(o.fullName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(o.badgeNumber) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Officer> searchOfficers(@Param("q") String query, Pageable pageable);
 
    @Query("SELECT COUNT(o) FROM Officer o WHERE o.status = 'ACTIVE'")
    Long countActive();
}
 
 