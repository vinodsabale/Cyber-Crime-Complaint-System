package com.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.entity.Wanted;

@Repository
public interface WantedRepository extends JpaRepository<Wanted, Long> {

    Page<Wanted> findByStatus(Wanted.WantedStatus status, Pageable pageable);

    List<Wanted> findByComplaintId(Long complaintId);

    @Query("SELECT w FROM Wanted w WHERE " +
           "LOWER(w.name) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(w.alias) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(w.lastKnownLocation) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Wanted> search(@Param("q") String query, Pageable pageable);

    @Query("SELECT w FROM Wanted w LEFT JOIN FETCH w.complaint")
    List<Wanted> findAllWithComplaintList();   // ← rename केले

    Long countByStatus(Wanted.WantedStatus status);

    @Query(value = "SELECT w FROM Wanted w LEFT JOIN FETCH w.complaint",
           countQuery = "SELECT COUNT(w) FROM Wanted w")
    Page<Wanted> findAllWithComplaint(Pageable pageable);

    @Query(value = "SELECT w FROM Wanted w LEFT JOIN FETCH w.complaint WHERE w.status = :status",
           countQuery = "SELECT COUNT(w) FROM Wanted w WHERE w.status = :status")
    Page<Wanted> findByStatusWithComplaint(@Param("status") Wanted.WantedStatus status, Pageable pageable);
}