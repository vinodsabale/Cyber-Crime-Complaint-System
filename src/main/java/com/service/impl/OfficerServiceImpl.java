package com.service.impl;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.entity.Officer;
import com.exception.ResourceNotFoundException;
import com.repository.OfficerRepository;
import com.service.OfficerService;

import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
@Transactional
public class OfficerServiceImpl implements OfficerService {
 
    private final OfficerRepository officerRepo;
 
    @Override
    @Transactional(readOnly = true)
    public List<Officer> getAvailableOfficers() {
        return officerRepo.findAvailable();
    }
 
    @Override
    @Transactional(readOnly = true)
    public Officer getById(Long id) {
        return officerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found: " + id));
    }
 
    @Override
    public Officer save(Officer officer) {
        return officerRepo.save(officer);
    }
    @Override
    public Page<Officer> getAll(Pageable pageable) {
        return officerRepo.findAll(pageable);
    }

    @Override
    public void delete(Long id) {
        officerRepo.deleteById(id);
    }
}
 
 