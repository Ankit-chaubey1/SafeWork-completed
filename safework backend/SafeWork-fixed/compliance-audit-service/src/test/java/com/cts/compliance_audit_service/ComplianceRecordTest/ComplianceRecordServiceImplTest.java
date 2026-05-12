package com.cts.compliance_audit_service.ComplianceRecordTest;

import com.cts.compliance_audit_service.entity.ComplianceRecord;
import com.cts.compliance_audit_service.enums.ComplianceEntityType;
import com.cts.compliance_audit_service.enums.ComplianceResult;
import com.cts.compliance_audit_service.exception.ResourceNotFoundException;
import com.cts.compliance_audit_service.repository.ComplianceRecordRepository;
import com.cts.compliance_audit_service.service.ComplianceRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceRecordServiceImplTest {

    @Mock
    private ComplianceRecordRepository complianceRecordRepository;

    @InjectMocks
    private ComplianceRecordServiceImpl complianceRecordService;

    private ComplianceRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = new ComplianceRecord();
        sampleRecord.setComplianceId(1L);
        sampleRecord.setEntityId(101L);
        sampleRecord.setEntityType(ComplianceEntityType.Hazard);
        sampleRecord.setComplianceResult(ComplianceResult.COMPLIANT);
        sampleRecord.setComplianceDate(LocalDate.now());
        sampleRecord.setComplianceNotes("All documents verified");
    }

    @Test
    @DisplayName("Should save a compliance record successfully")
    void createComplianceRecord_Success() {
        complianceRecordService.createComplianceRecord(sampleRecord);
        verify(complianceRecordRepository, times(1)).save(sampleRecord);
    }

    @Test
    @DisplayName("Should return all records when list is not empty")
    void getAllComplianceRecords_Success() {
        when(complianceRecordRepository.findAll()).thenReturn(List.of(sampleRecord));

        List<ComplianceRecord> result = complianceRecordService.getAllComplianceRecords();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no records exist")
    void getAllComplianceRecords_NotFound() {
        when(complianceRecordRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () ->
                complianceRecordService.getAllComplianceRecords()
        );
    }

    @Test
    @DisplayName("Should return record when valid ID is provided")
    void getComplianceRecordById_Success() {
        when(complianceRecordRepository.findById(1L)).thenReturn(Optional.of(sampleRecord));

        Optional<ComplianceRecord> result = complianceRecordService.getComplianceRecordById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getComplianceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should update existing record successfully")
    void updateComplianceRecord_Success() {
        ComplianceRecord updatedData = new ComplianceRecord();
        updatedData.setComplianceNotes("Updated Notes");

        when(complianceRecordRepository.findById(1L)).thenReturn(Optional.of(sampleRecord));

        complianceRecordService.updateComplianceRecord(1L, updatedData);

        verify(complianceRecordRepository).save(any(ComplianceRecord.class));
        assertThat(sampleRecord.getComplianceNotes()).isEqualTo("Updated Notes");
    }

    @Test
    @DisplayName("Should delete record when ID exists")
    void deleteComplianceRecord_Success() {
        when(complianceRecordRepository.existsById(1L)).thenReturn(true);

        complianceRecordService.deleteComplianceRecord(1L);

        verify(complianceRecordRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent ID")
    void deleteComplianceRecord_NotFound() {
        when(complianceRecordRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                complianceRecordService.deleteComplianceRecord(1L)
        );
        verify(complianceRecordRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should find records by Entity Type")
    void findByEntityType_Success() {
        when(complianceRecordRepository.findByEntityType(ComplianceEntityType.Hazard))
                .thenReturn(List.of(sampleRecord));

        List<ComplianceRecord> result = complianceRecordService.findByEntityType(ComplianceEntityType.Hazard);

        assertThat(result).isNotEmpty();
        verify(complianceRecordRepository).findByEntityType(ComplianceEntityType.Hazard);
    }
}