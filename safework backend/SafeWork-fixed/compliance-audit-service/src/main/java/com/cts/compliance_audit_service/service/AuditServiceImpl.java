package com.cts.compliance_audit_service.service;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AuditServiceImpl implements IAuditService {

    private final UserClient userClient;
    private final AuditRepository auditRepository;

    public AuditServiceImpl(UserClient userClient, AuditRepository auditRepository) {
        this.userClient = userClient;
        this.auditRepository = auditRepository;
    }

    @Override
    public void createAudit(Audit audit, Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException("UserId from header must not be null");
        }

        UserPublicDTO user;
        try {
            user = userClient.getUserById(userId);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        audit.setOfficerId(userId);
        audit.setOfficerName(user.getUserName());

        auditRepository.save(audit);
    }

    @Override
    public List<Audit> getAllAudits() {
        List<Audit> audits = auditRepository.findAll();
        if (audits.isEmpty()) {
            throw new NoAuditFoundException("No audits found");
        }
        return audits;
    }

    @Override
    public Optional<AuditByIdProjection> getAuditById(Long id) {
        Optional<AuditByIdProjection> audit = auditRepository.findProjectedByAuditId(id);
        if (audit.isEmpty()) {
            throw new AuditNotFoundException("Audit not found with id: " + id);
        }
        return audit;
    }

    @Override
    public Audit updateAudit(Long id, Audit updatedAudit) {
        return auditRepository.findById(id)
                .map(existing -> {

                    existing.setAuditScope(updatedAudit.getAuditScope());
                    existing.setAuditFinding(updatedAudit.getAuditFinding());
                    existing.setAuditDate(updatedAudit.getAuditDate());
                    existing.setAuditStatus(updatedAudit.getAuditStatus());

                    if (updatedAudit.getOfficerId() != null &&
                            !updatedAudit.getOfficerId().equals(existing.getOfficerId())) {

                        UserPublicDTO user = userClient.getUserById(updatedAudit.getOfficerId());

                        existing.setOfficerId(user.getUserId());
                        existing.setOfficerName(user.getUserName());
                    }

                    return auditRepository.save(existing);
                })
                .orElseThrow(() -> new AuditNotFoundException("Audit not found with id: " + id));
    }

    @Override
    public void deleteAudit(Long id) {
        if (!auditRepository.existsById(id)) {
            throw new AuditNotFoundException("Audit not found with id: " + id);
        }
        auditRepository.deleteById(id);
    }

    @Override
    public List<Audit> findByAuditStatus(AuditStatus auditStatus) {
        List<Audit> audits = auditRepository.findByAuditStatus(auditStatus);
        if (audits.isEmpty()) {
            throw new NoAuditFoundException("No audits found with status: " + auditStatus);
        }
        return audits;
    }

    @Override
    public List<Audit> findByAuditScope(AuditScope auditScope) {
        List<Audit> audits = auditRepository.findByAuditScope(auditScope);
        if (audits.isEmpty()) {
            throw new NoAuditFoundException("No audits found with scope: " + auditScope);
        }
        return audits;
    }

    @Override
    public List<Audit> findAuditByOfficerId(Long userId) {
        List<Audit> audits = auditRepository.findByOfficerId(userId);
        if (audits.isEmpty()) {
            throw new NoAuditFoundException("No audits found for officerId: " + userId);
        }
        return audits;
    }
}
