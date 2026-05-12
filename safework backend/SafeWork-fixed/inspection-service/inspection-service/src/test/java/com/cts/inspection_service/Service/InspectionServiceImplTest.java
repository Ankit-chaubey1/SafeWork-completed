package com.cts.inspection_service.Service;

import com.cts.inspection_service.Dto.InspectionRequestDTO;
import com.cts.inspection_service.Dto.InspectionResponseDTO;
import com.cts.inspection_service.Entity.Inspection;
import com.cts.inspection_service.FleignClient.UserClient;
import com.cts.inspection_service.FleignClient.UserPublicDto;
import com.cts.inspection_service.Repository.InspectionRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InspectionServiceImplTest {

    @Mock
    private InspectionRepository repository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private InspectionServiceImpl service;

    private final Long VALID_REQUESTER_ID = 10L;
    private final String VALID_ROLE = "ADMIN";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateInspection_Success() {
        // Mocking Input
        InspectionRequestDTO requestDTO = new InspectionRequestDTO();
        requestDTO.setOfficerId(1L);
        requestDTO.setInspectionLocation("Mumbai Site");

        // Mocking Feign Response
        UserPublicDto mockUser = new UserPublicDto();
        mockUser.setUserName("Sudhanshu");
        mockUser.setUserEmail("sudhanshu@cts.com");

        // Mocking Repository Save
        Inspection savedEntity = new Inspection();
        savedEntity.setInspectionId(101L);
        savedEntity.setOfficerName("Sudhanshu");

        when(userClient.getUserById(1L)).thenReturn(mockUser);
        when(repository.save(any(Inspection.class))).thenReturn(savedEntity);

        // Execute - Passing requesterId and role
        InspectionResponseDTO result = service.createInspection(requestDTO, VALID_REQUESTER_ID, VALID_ROLE);

        // Verify
        assertNotNull(result);
        assertEquals(101L, result.getInspectionId());
        verify(userClient, times(1)).getUserById(1L);
        verify(repository, times(1)).save(any(Inspection.class));
    }

    @Test
    void testCreateInspection_UserNotFound_ThrowsException() {
        InspectionRequestDTO requestDTO = new InspectionRequestDTO();
        requestDTO.setOfficerId(999L);

        // Simulate FeignException.NotFound instead of returning null
        // because the new implementation uses try-catch feign blocks
        when(userClient.getUserById(999L)).thenThrow(mock(FeignException.NotFound.class));

        // Assert Exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createInspection(requestDTO, VALID_REQUESTER_ID, VALID_ROLE);
        });

        assertEquals("Validation Failed: Officer does not exist.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateInspection_UnauthorizedRole_ThrowsException() {
        InspectionRequestDTO requestDTO = new InspectionRequestDTO();
        String invalidRole = "USER"; // A role not allowed by validateAuthorizedRole()

        // Assert Access Denied Exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createInspection(requestDTO, VALID_REQUESTER_ID, invalidRole);
        });

        assertEquals("Access Denied: Unauthorized role.", exception.getMessage());
    }

    @Test
    void testDeleteInspection_Success() {
        Long inspectionId = 101L;
        when(repository.existsById(inspectionId)).thenReturn(true);

        // Execute
        assertDoesNotThrow(() -> service.deleteInspection(inspectionId, VALID_REQUESTER_ID, VALID_ROLE));

        // Verify
        verify(repository, times(1)).deleteById(inspectionId);
    }

    @Test
    void testDeleteInspection_AccessDenied() {
        Long inspectionId = 101L;
        String unauthorizedRole = "EMPLOYEE";

        // Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.deleteInspection(inspectionId, VALID_REQUESTER_ID, unauthorizedRole);
        });

        assertEquals("Access Denied: Insufficient permissions.", exception.getMessage());
        verify(repository, never()).deleteById(anyLong());
    }
}