package com.util;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.repository.ComplaintRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TrackingNumberGenerator {

   private final ComplaintRepository repo;

   public synchronized String generate() {
       String year = String.valueOf(LocalDate.now().getYear());
       long next = repo.count() + 1;
       return String.format("CRM-%s-%08d", year, next);
   }
}