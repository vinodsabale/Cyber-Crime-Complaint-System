package com.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.entity.Officer;

public interface OfficerService {

    List<Officer> getAvailableOfficers();

    Page<Officer> getAll(Pageable pageable);

    Officer getById(Long id);

    Officer save(Officer officer);

    void delete(Long id);
}