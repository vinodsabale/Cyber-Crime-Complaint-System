package com.crime.app.service;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.dto.request.SuspectRequest;
import com.dto.response.SuspectResponse;
import com.entity.Suspect;
import com.exception.ResourceNotFoundException;
import com.repository.SuspectRepository;
import com.service.impl.SuspectServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuspectService Tests")
class SuspectServiceTest {

    @Mock SuspectRepository suspectRepo;
    @InjectMocks SuspectServiceImpl suspectService;

    private Suspect testSuspect;

    @BeforeEach
    void setUp() {
        testSuspect = Suspect.builder()
                .id(1L).suspectCode("SUSP-000001")
                .fullName("John Doe").alias("JD")
                .gender(Suspect.Gender.MALE)
                .status(Suspect.SuspectStatus.WANTED)
                .dangerLevel(Suspect.DangerLevel.HIGH)
                .phoneNumbers("9876543210").emailAddresses("fraud@fake.com")
                .bankAccounts("1234567890").upiIds("fraud@upi")
                .totalCases(3).arrested(false)
                .complaints(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should return suspect by ID")
    void getById_shouldReturnSuspect() {
        when(suspectRepo.findById(1L)).thenReturn(Optional.of(testSuspect));
        SuspectResponse result = suspectService.getById(1L);
        assertNotNull(result);
        assertEquals("SUSP-000001", result.getSuspectCode());
        assertEquals("John Doe", result.getFullName());
    }

    @Test
    @DisplayName("Should throw exception when suspect not found")
    void getById_shouldThrow_whenNotFound() {
        when(suspectRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> suspectService.getById(99L));
    }

    @Test
    @DisplayName("Should add suspect with generated code")
    void add_shouldCreateSuspectWithCode() {
        SuspectRequest req = SuspectRequest.builder()
                .fullName("Jane Doe").status(Suspect.SuspectStatus.WANTED)
                .dangerLevel(Suspect.DangerLevel.MODERATE).build();

        when(suspectRepo.count()).thenReturn(0L);
        when(suspectRepo.save(any(Suspect.class))).thenReturn(testSuspect);

        SuspectResponse result = suspectService.add(req);
        assertNotNull(result);
        verify(suspectRepo, times(1)).save(any(Suspect.class));
    }

    @Test
    @DisplayName("Should return wanted suspects sorted by danger level")
    void getWanted_shouldReturnWantedSuspects() {
        Suspect mostWanted = Suspect.builder().id(2L).suspectCode("SUSP-000002")
                .fullName("Most Wanted").status(Suspect.SuspectStatus.WANTED)
                .dangerLevel(Suspect.DangerLevel.MOST_WANTED)
                .complaints(new ArrayList<>()).build();

        when(suspectRepo.findWanted()).thenReturn(List.of(testSuspect, mostWanted));
        var result = suspectService.getWanted();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should delete suspect when exists")
    void delete_shouldDelete_whenExists() {
        when(suspectRepo.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> suspectService.delete(1L));
        verify(suspectRepo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent suspect")
    void delete_shouldThrow_whenNotFound() {
        when(suspectRepo.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> suspectService.delete(99L));
    }

    @Test
    @DisplayName("Should mark suspect as arrested")
    void arrest_shouldUpdateArrestedStatus() {
        when(suspectRepo.findById(1L)).thenReturn(Optional.of(testSuspect));
        when(suspectRepo.save(any())).thenReturn(testSuspect);

        ArgumentCaptor<Suspect> captor = ArgumentCaptor.forClass(Suspect.class);
        suspectService.arrest(1L, "Arrested at Mumbai airport");
        verify(suspectRepo).save(captor.capture());

        assertTrue(captor.getValue().isArrested());
        assertEquals(Suspect.SuspectStatus.ARRESTED, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getArrestDate());
    }

    @Test
    @DisplayName("Should search suspects by digital footprint")
    void searchBySuspectDetails_shouldReturnMatches() {
        when(suspectRepo.findBySuspectDetails("9876543210", "fraud@fake.com", "fraud@upi", "1234567890"))
                .thenReturn(List.of(testSuspect));

        var result = suspectService.searchBySuspectDetails("9876543210", "fraud@fake.com", "fraud@upi", "1234567890");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
    }

    @Test
    @DisplayName("Should return paginated suspects")
    void getAll_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Suspect> page = new PageImpl<>(List.of(testSuspect), pageable, 1);
        when(suspectRepo.findAll(pageable)).thenReturn(page);

        var result = suspectService.getAll(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}