package com.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.entity.Suspect;
 
@Repository
public interface SuspectRepository extends JpaRepository<Suspect, Long> {
 
    Optional<Suspect> findBySuspectCode(String code);
    boolean existsBySuspectCode(String code);
    List<Suspect> findByStatus(Suspect.SuspectStatus status);
 
    @Query("SELECT s FROM Suspect s WHERE " +
           "LOWER(s.fullName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(s.alias) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "s.aadharNumber LIKE CONCAT('%',:q,'%') OR " +
           "s.phone LIKE CONCAT('%',:q,'%') OR " +
           "s.email LIKE CONCAT('%',:q,'%') OR " +
           "s.bankAccounts LIKE CONCAT('%',:q,'%') OR " +
           "s.ipAddresses LIKE CONCAT('%',:q,'%') OR " +
           "s.upiIds LIKE CONCAT('%',:q,'%') OR " +
           "s.phoneNumbers LIKE CONCAT('%',:q,'%')")
    Page<Suspect> searchSuspects(@Param("q") String query, Pageable pageable);
 
    @Query("SELECT s FROM Suspect s WHERE s.status = 'WANTED' ORDER BY s.dangerLevel DESC")
    List<Suspect> findWanted();
 
    @Query("SELECT s FROM Suspect s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:level IS NULL OR s.dangerLevel = :level)")
    Page<Suspect> findWithFilters(
        @Param("status") Suspect.SuspectStatus status,
        @Param("level") Suspect.DangerLevel level,
        Pageable pageable);
 
    @Query("SELECT s.dangerLevel, COUNT(s) FROM Suspect s GROUP BY s.dangerLevel")
    List<Object[]> countByDangerLevel();
 
    @Query("SELECT COUNT(s) FROM Suspect s WHERE s.status = :status")
    Long countByStatus(@Param("status") Suspect.SuspectStatus status);
 
    @Query("SELECT s FROM Suspect s WHERE " +
           "(:phone IS NOT NULL AND s.phoneNumbers LIKE CONCAT('%',:phone,'%')) OR " +
           "(:email IS NOT NULL AND s.emailAddresses LIKE CONCAT('%',:email,'%')) OR " +
           "(:upiId IS NOT NULL AND s.upiIds LIKE CONCAT('%',:upiId,'%')) OR " +
           "(:bank IS NOT NULL AND s.bankAccounts LIKE CONCAT('%',:bank,'%'))")
    List<Suspect> findBySuspectDetails(
        @Param("phone") String phone,
        @Param("email") String email,
        @Param("upiId") String upiId,
        @Param("bank") String bank);
}
 