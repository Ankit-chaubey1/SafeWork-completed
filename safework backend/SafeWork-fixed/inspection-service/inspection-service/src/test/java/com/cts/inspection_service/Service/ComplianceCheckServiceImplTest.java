package com.cts.inspection_service.Service;

import com.cts.inspection_service.Dto.ComplianceRequestDTO;
import com.cts.inspection_service.Repository.ComplianceCheckRepository;
import com.cts.inspection_service.Repository.InspectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceCheckServiceImplTest {

    @Mock
    private ComplianceCheckRepository repository;

    @Mock
    private InspectionRepository inspectionRepository;

    @InjectMocks
    private ComplianceCheckServiceImpl service;

    private final Long VALID_REQUESTER_ID = 50L;
    private final String ADMIN_ROLE = "ADMIN";
    private final String OFFICER_ROLE = "OFFICER";

    @Test
    void testCreateCheck_InvalidInspectionId_ThrowsException() {
        ComplianceRequestDTO dto = new ComplianceRequestDTO();
        dto.setInspectionId(7234L);

        when(inspectionRepository.existsById(7234L)).thenReturn(false);

        // Pass the dummy requesterId and role as per the new method signature
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.createCheck(dto, 1L, "ADMIN")
        );

        // Update this to match the Service implementation exactly
        assertTrue(ex.getMessage().contains("Inspection ID 7234 not found."));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreateCheck_UnauthorizedRole_ThrowsException() {
        ComplianceRequestDTO dto = new ComplianceRequestDTO();
        String unauthorizedRole = "EMPLOYEE";

        // Execute & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.createCheck(dto, VALID_REQUESTER_ID, unauthorizedRole)
        );

        assertEquals("Access Denied: You do not have permission to perform this action.", ex.getMessage());
    }

    @Test
    void testDeleteCheck_AdminSuccess() {
        Long checkId = 1L;
        when(repository.existsById(checkId)).thenReturn(true);

        // Execute
        assertDoesNotThrow(() -> service.deleteCheck(checkId, VALID_REQUESTER_ID, ADMIN_ROLE));

        // Verify
        verify(repository, times(1)).deleteById(checkId);
    }

    @Test
    void testDeleteCheck_OfficerDenied_ThrowsException() {
        Long checkId = 1L;

        // Execute & Assert - Officers cannot delete compliance checks, only Admins
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.deleteCheck(checkId, VALID_REQUESTER_ID, OFFICER_ROLE)
        );

        assertEquals("Only Admin can delete compliance checks.", ex.getMessage());
        verify(repository, never()).deleteById(anyLong());
    }
}