package com.repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.entity.Complaint;
import com.entity.User;
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
 
    Optional<Complaint> findByTrackingNumber(String trackingNumber);
    boolean existsByTrackingNumber(String trackingNumber);
    Page<Complaint> findByComplainant(User user, Pageable pageable);
    Page<Complaint> findByStatus(Complaint.ComplaintStatus status, Pageable pageable);
    Page<Complaint> findByPriority(Complaint.Priority priority, Pageable pageable);
    Page<Complaint> findByAssignedOfficerId(Long officerId, Pageable pageable);
 
    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:fromDate IS NULL OR c.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR c.createdAt <= :toDate)")
    Page<Complaint> findWithFilters(
        @Param("status") Complaint.ComplaintStatus status,
        @Param("category") Complaint.CrimeCategory category,
        @Param("priority") Complaint.Priority priority,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable);
 
    @Query("SELECT c FROM Complaint c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.trackingNumber) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.incidentLocation) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.suspectEmail) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.suspectPhone) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Complaint> searchComplaints(@Param("q") String query, Pageable pageable);
 
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = :status")
    Long countByStatus(@Param("status") Complaint.ComplaintStatus status);
 
    @Query("SELECT COUNT(c) FROM Complaint c WHERE DATE(c.createdAt) = CURRENT_DATE")
    Long countToday();
 
    @Query("SELECT COUNT(c) FROM Complaint c WHERE MONTH(c.createdAt) = MONTH(CURRENT_DATE) AND YEAR(c.createdAt) = YEAR(CURRENT_DATE)")
    Long countThisMonth();
 
    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countGroupByCategory();
 
    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countGroupByStatus();
 
    @Query("SELECT MONTH(c.createdAt), COUNT(c) FROM Complaint c WHERE YEAR(c.createdAt) = YEAR(CURRENT_DATE) GROUP BY MONTH(c.createdAt)")
    List<Object[]> countByMonth();
 
    @Query("SELECT COALESCE(SUM(c.financialLoss),0) FROM Complaint c WHERE c.financialLoss IS NOT NULL")
    Double sumFinancialLoss();
 
    @Query("SELECT c FROM Complaint c WHERE c.assignedOfficer IS NULL AND c.status = 'SUBMITTED'")
    List<Complaint> findUnassigned();
 
    @Query("SELECT c FROM Complaint c WHERE c.priority = 'CRITICAL' AND c.status NOT IN ('RESOLVED','CLOSED','REJECTED')")
    List<Complaint> findCriticalOpen();
 
    @Modifying
    @Query("UPDATE Complaint c SET c.status = :status, c.updatedAt = :now WHERE c.id = :id")
    void updateStatus(@Param("id") Long id,
                      @Param("status") Complaint.ComplaintStatus status,
                      @Param("now") LocalDateTime now);
}
 