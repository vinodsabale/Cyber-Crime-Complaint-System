package com.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.entity.ComplaintStatusHistory;

@Repository
public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, Long> {
   List<ComplaintStatusHistory> findByComplaintIdOrderByChangedAtAsc(Long complaintId);
}