package com.cts.compliance_audit_service.AuditTest;

import com.cts.compliance_audit_service.dto.UserPublicDTO;
import com.cts.compliance_audit_service.entity.Audit;
import com.cts.compliance_audit_service.enums.AuditScope;
import com.cts.compliance_audit_service.enums.AuditStatus;
import com.cts.compliance_audit_service.exception.AuditNotFoundException;
import com.cts.compliance_audit_service.exception.NoAuditFoundException;
import com.cts.compliance_audit_service.exception.ResourceNotFoundException;
import com.cts.compliance_audit_service.externalService.UserClient;
import com.cts.compliance_audit_service.projection.AuditByIdProjection;
import com.cts.compliance_audit_service.repository.AuditRepository;
import com.cts.compliance_audit_service.service.AuditServiceImpl;
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
class AuditServiceImplTest {

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AuditServiceImpl auditService;

    private Audit sampleAudit;
    private UserPublicDTO sampleUser;

    @BeforeEach
    void setUp() {
        sampleAudit = new Audit();
        sampleAudit.setAuditId(1L);
        sampleAudit.setOfficerId(101L);
        sampleAudit.setOfficerName("Old Officer");
        sampleAudit.setAuditScope(AuditScope.FULL_SITE);
        sampleAudit.setAuditStatus(AuditStatus.Open);
        sampleAudit.setAuditDate(LocalDate.now());
        sampleAudit.setAuditFinding("Initial Finding");

        sampleUser = new UserPublicDTO();
        sampleUser.setUserId(101L);
        sampleUser.setUserName("John Doe");
    }

    @Test
    @DisplayName("Should create audit successfully using userId from header")
    void createAudit_Success() {
        when(userClient.getUserById(101L)).thenReturn(sampleUser);

        auditService.createAudit(sampleAudit, 101L);

        assertThat(sampleAudit.getOfficerId()).isEqualTo(101L);
        assertThat(sampleAudit.getOfficerName()).isEqualTo("John Doe");
        verify(userClient, times(1)).getUserById(101L);
        verify(auditRepository, times(1)).save(sampleAudit);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when userId is null")
    void createAudit_NullUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> auditService.createAudit(sampleAudit, null));

        verify(userClient, never()).getUserById(any());
        verify(auditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when UserClient fails during createAudit")
    void createAudit_UserNotFound() {
        when(userClient.getUserById(101L)).thenThrow(new RuntimeException("User service down"));

        assertThrows(ResourceNotFoundException.class,
                () -> auditService.createAudit(sampleAudit, 101L));

        verify(auditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return all audits when database is not empty")
    void getAllAudits_Success() {
        when(auditRepository.findAll()).thenReturn(List.of(sampleAudit));

        List<Audit> result = auditService.getAllAudits();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuditId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw NoAuditFoundException when no audits exist")
    void getAllAudits_Empty() {
        when(auditRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(NoAuditFoundException.class, () -> auditService.getAllAudits());
    }

    @Test
    @DisplayName("Should return audit by id when present")
    void getAuditById_Success() {
        AuditByIdProjection projection = mock(AuditByIdProjection.class);
        when(auditRepository.findProjectedByAuditId(1L)).thenReturn(Optional.of(projection));

        Optional<AuditByIdProjection> result = auditService.getAuditById(1L);

        assertThat(result).isPresent();
        verify(auditRepository, times(1)).findProjectedByAuditId(1L);
    }

    @Test
    @DisplayName("Should throw AuditNotFoundException when audit id not found")
    void getAuditById_NotFound() {
        when(auditRepository.findProjectedByAuditId(99L)).thenReturn(Optional.empty());

        assertThrows(AuditNotFoundException.class,
                () -> auditService.getAuditById(99L));
    }

    @Test
    @DisplayName("Should update audit without changing officer when officerId is same or null")
    void updateAudit_Success_WithoutOfficerChange() {
        Audit updatedInfo = new Audit();
        updatedInfo.setAuditScope(AuditScope.FULL_SITE);
        updatedInfo.setAuditFinding("Updated Finding");
        updatedInfo.setAuditDate(LocalDate.now());
        updatedInfo.setAuditStatus(AuditStatus.Closed);
        updatedInfo.setOfficerId(101L); // same as existing

        when(auditRepository.findById(1L)).thenReturn(Optional.of(sampleAudit));
        when(auditRepository.save(any(Audit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Audit result = auditService.updateAudit(1L, updatedInfo);

        assertThat(result.getAuditFinding()).isEqualTo("Updated Finding");
        assertThat(result.getAuditStatus()).isEqualTo(AuditStatus.Closed);
        assertThat(result.getOfficerId()).isEqualTo(101L);
        assertThat(result.getOfficerName()).isEqualTo("Old Officer");

        verify(userClient, never()).getUserById(any());
        verify(auditRepository, times(1)).save(any(Audit.class));
    }

    @Test
    @DisplayName("Should update audit and officer details when officerId changes")
    void updateAudit_WithNewOfficer() {
        Audit updatedInfo = new Audit();
        updatedInfo.setOfficerId(102L);
        updatedInfo.setAuditScope(AuditScope.FULL_SITE);
        updatedInfo.setAuditFinding("Updated Finding");
        updatedInfo.setAuditDate(LocalDate.now());
        updatedInfo.setAuditStatus(AuditStatus.Open);

        UserPublicDTO newUser = new UserPublicDTO();
        newUser.setUserId(102L);
        newUser.setUserName("Jane Smith");

        when(auditRepository.findById(1L)).thenReturn(Optional.of(sampleAudit));
        when(userClient.getUserById(102L)).thenReturn(newUser);
        when(auditRepository.save(any(Audit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Audit result = auditService.updateAudit(1L, updatedInfo);

        assertThat(result.getOfficerId()).isEqualTo(102L);
        assertThat(result.getOfficerName()).isEqualTo("Jane Smith");
        verify(userClient, times(1)).getUserById(102L);
        verify(auditRepository, times(1)).save(any(Audit.class));
    }

    @Test
    @DisplayName("Should throw AuditNotFoundException when updating non-existing audit")
    void updateAudit_NotFound() {
        Audit updatedInfo = new Audit();
        updatedInfo.setAuditScope(AuditScope.FULL_SITE);
        updatedInfo.setAuditFinding("Updated Finding");
        updatedInfo.setAuditDate(LocalDate.now());
        updatedInfo.setAuditStatus(AuditStatus.Open);

        when(auditRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AuditNotFoundException.class,
                () -> auditService.updateAudit(99L, updatedInfo));
    }

    @Test
    @DisplayName("Should delete audit when id exists")
    void deleteAudit_Success() {
        when(auditRepository.existsById(1L)).thenReturn(true);

        auditService.deleteAudit(1L);

        verify(auditRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw AuditNotFoundException when deleting non-existent audit")
    void deleteAudit_NotFound() {
        when(auditRepository.existsById(99L)).thenReturn(false);

        assertThrows(AuditNotFoundException.class,
                () -> auditService.deleteAudit(99L));
    }

    @Test
    @DisplayName("Should return audits by status")
    void findByAuditStatus_Success() {
        when(auditRepository.findByAuditStatus(AuditStatus.Open)).thenReturn(List.of(sampleAudit));

        List<Audit> result = auditService.findByAuditStatus(AuditStatus.Open);

        assertThat(result).hasSize(1);
        verify(auditRepository, times(1)).findByAuditStatus(AuditStatus.Open);
    }

    @Test
    @DisplayName("Should throw NoAuditFoundException when no audits found by status")
    void findByAuditStatus_Empty() {
        when(auditRepository.findByAuditStatus(AuditStatus.Open)).thenReturn(Collections.emptyList());

        assertThrows(NoAuditFoundException.class,
                () -> auditService.findByAuditStatus(AuditStatus.Open));
    }

    @Test
    @DisplayName("Should return audits by scope")
    void findByAuditScope_Success() {
        when(auditRepository.findByAuditScope(AuditScope.FULL_SITE)).thenReturn(List.of(sampleAudit));

        List<Audit> result = auditService.findByAuditScope(AuditScope.FULL_SITE);

        assertThat(result).hasSize(1);
        verify(auditRepository, times(1)).findByAuditScope(AuditScope.FULL_SITE);
    }

    @Test
    @DisplayName("Should throw NoAuditFoundException when no audits found by scope")
    void findByAuditScope_Empty() {
        when(auditRepository.findByAuditScope(AuditScope.FULL_SITE)).thenReturn(Collections.emptyList());

        assertThrows(NoAuditFoundException.class,
                () -> auditService.findByAuditScope(AuditScope.FULL_SITE));
    }

    @Test
    @DisplayName("Should return audits by officerId")
    void findAuditByOfficerId_Success() {
        when(auditRepository.findByOfficerId(101L)).thenReturn(List.of(sampleAudit));

        List<Audit> result = auditService.findAuditByOfficerId(101L);

        assertThat(result).hasSize(1);
        verify(auditRepository, times(1)).findByOfficerId(101L);
    }

    @Test
    @DisplayName("Should throw NoAuditFoundException when no audits found for officerId")
    void findAuditByOfficerId_Empty() {
        when(auditRepository.findByOfficerId(101L)).thenReturn(Collections.emptyList());

        assertThrows(NoAuditFoundException.class,
                () -> auditService.findAuditByOfficerId(101L));
    }
}
