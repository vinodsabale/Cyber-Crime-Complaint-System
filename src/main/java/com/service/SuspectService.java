package com.service;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.dto.request.SuspectRequest;
import com.dto.response.SuspectResponse;
import com.entity.Suspect;
 
public interface SuspectService {
    SuspectResponse add(SuspectRequest req);
    SuspectResponse getById(Long id);
    SuspectResponse getByCode(String code);
    Page<SuspectResponse> getAll(Pageable pageable);
    Page<SuspectResponse> search(String query, Pageable pageable);
    Page<SuspectResponse> filter(Suspect.SuspectStatus status, Suspect.DangerLevel level, Pageable pageable);
    SuspectResponse update(Long id, SuspectRequest req);
    SuspectResponse arrest(Long id, String details);
    List<SuspectResponse> getWanted();
    List<SuspectResponse> getMostWanted();
    List<SuspectResponse> searchBySuspectDetails(String phone, String email, String upiId, String bank);
    void delete(Long id);
}
 