package com.service.impl;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dto.request.SuspectRequest;
import com.dto.response.SuspectResponse;
import com.entity.Suspect;
import com.exception.ResourceNotFoundException;
import com.repository.SuspectRepository;
import com.service.SuspectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
 
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SuspectServiceImpl implements SuspectService {
 
    private final SuspectRepository suspectRepo;
 
    @Override
    @CacheEvict(value = "suspects", allEntries = true)
    public SuspectResponse add(SuspectRequest req) {
        long count = suspectRepo.count() + 1;
        Suspect suspect = Suspect.builder()
                .suspectCode(String.format("SUSP-%06d", count))
                .fullName(req.getFullName())
                .alias(req.getAlias())
                .dateOfBirth(req.getDateOfBirth())
                .gender(req.getGender())
                .nationality(req.getNationality() != null ? req.getNationality() : "Indian")
                .aadharNumber(req.getAadharNumber())
                .phone(req.getPhone())
                .email(req.getEmail())
                .lastKnownAddress(req.getLastKnownAddress())
                .physicalDescription(req.getPhysicalDescription())
                .status(req.getStatus())
                .dangerLevel(req.getDangerLevel())
                .modusOperandi(req.getModusOperandi())
                .bankAccounts(req.getBankAccounts())
                .ipAddresses(req.getIpAddresses())
                .emailAddresses(req.getEmailAddresses())
                .phoneNumbers(req.getPhoneNumbers())
                .websites(req.getWebsites())
                .upiIds(req.getUpiIds())
                .notes(req.getNotes())
                .totalCases(0)
                .arrested(false)
                .build();
        return toResponse(suspectRepo.save(suspect));
    }
 
    @Override
    @Cacheable(value = "suspects", key = "#id")
    @Transactional(readOnly = true)
    public SuspectResponse getById(Long id) {
        return toResponse(find(id));
    }
 
    @Override
    @Transactional(readOnly = true)
    public SuspectResponse getByCode(String code) {
        return toResponse(suspectRepo.findBySuspectCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Suspect not found: " + code)));
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<SuspectResponse> getAll(Pageable pageable) {
        return suspectRepo.findAll(pageable).map(this::toResponse);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<SuspectResponse> search(String query, Pageable pageable) {
        return suspectRepo.searchSuspects(query, pageable).map(this::toResponse);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<SuspectResponse> filter(Suspect.SuspectStatus status,
                                         Suspect.DangerLevel level,
                                         Pageable pageable) {
        return suspectRepo.findWithFilters(status, level, pageable).map(this::toResponse);
    }
 
    @Override
    @CacheEvict(value = "suspects", key = "#id")
    public SuspectResponse update(Long id, SuspectRequest req) {
        Suspect s = find(id);
        s.setFullName(req.getFullName());
        s.setAlias(req.getAlias());
        s.setDateOfBirth(req.getDateOfBirth());
        s.setGender(req.getGender());
        s.setPhone(req.getPhone());
        s.setEmail(req.getEmail());
        s.setLastKnownAddress(req.getLastKnownAddress());
        s.setPhysicalDescription(req.getPhysicalDescription());
        s.setStatus(req.getStatus());
        s.setDangerLevel(req.getDangerLevel());
        s.setModusOperandi(req.getModusOperandi());
        s.setBankAccounts(req.getBankAccounts());
        s.setIpAddresses(req.getIpAddresses());
        s.setEmailAddresses(req.getEmailAddresses());
        s.setPhoneNumbers(req.getPhoneNumbers());
        s.setWebsites(req.getWebsites());
        s.setUpiIds(req.getUpiIds());
        s.setNotes(req.getNotes());
        return toResponse(suspectRepo.save(s));
    }
 
    @Override
    @CacheEvict(value = "suspects", key = "#id")
    public SuspectResponse arrest(Long id, String details) {
        Suspect s = find(id);
        s.setArrested(true);
        s.setArrestDate(LocalDate.now());
        s.setArrestDetails(details);
        s.setStatus(Suspect.SuspectStatus.ARRESTED);
        return toResponse(suspectRepo.save(s));
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<SuspectResponse> getWanted() {
        return suspectRepo.findWanted().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<SuspectResponse> getMostWanted() {
        return suspectRepo.findWanted().stream()
                .filter(s -> s.getDangerLevel() == Suspect.DangerLevel.MOST_WANTED
                          || s.getDangerLevel() == Suspect.DangerLevel.VERY_HIGH)
                .sorted(Comparator.comparing(Suspect::getDangerLevel).reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<SuspectResponse> searchBySuspectDetails(String phone, String email,
                                                         String upiId, String bank) {
        return suspectRepo.findBySuspectDetails(phone, email, upiId, bank).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @CacheEvict(value = "suspects", allEntries = true)
    public void delete(Long id) {
        if (!suspectRepo.existsById(id))
            throw new ResourceNotFoundException("Suspect not found");
        suspectRepo.deleteById(id);
    }
 
    private Suspect find(Long id) {
        return suspectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suspect not found: " + id));
    }
 
    private SuspectResponse toResponse(Suspect s) {
        return SuspectResponse.builder()
                .id(s.getId())
                .suspectCode(s.getSuspectCode())
                .fullName(s.getFullName())
                .alias(s.getAlias())
                .dateOfBirth(s.getDateOfBirth())
                .gender(s.getGender())
                .nationality(s.getNationality())
                .phone(s.getPhone())
                .email(s.getEmail())
                .lastKnownAddress(s.getLastKnownAddress())
                .physicalDescription(s.getPhysicalDescription())
                .photoUrl(s.getPhotoUrl())
                .status(s.getStatus())
                .dangerLevel(s.getDangerLevel())
                .modusOperandi(s.getModusOperandi())
                .bankAccounts(s.getBankAccounts())
                .ipAddresses(s.getIpAddresses())
                .emailAddresses(s.getEmailAddresses())
                .phoneNumbers(s.getPhoneNumbers())
                .websites(s.getWebsites())
                .upiIds(s.getUpiIds())
                .totalCases(s.getTotalCases())
                .arrested(s.isArrested())
                .arrestDate(s.getArrestDate())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .linkedComplaintsCount(s.getComplaints() != null ? s.getComplaints().size() : 0)
                .build();
    }
}
 
 