package com.cts.inspection_service.Service;

import com.cts.inspection_service.Dto.ComplianceRequestDTO;
import com.cts.inspection_service.Dto.ComplianceResponseDTO;
import java.util.List;

public interface IComplianceCheckService {
    // Added requesterId and role for security validation
    ComplianceResponseDTO createCheck(ComplianceRequestDTO dto, Long requesterId, String role);

    List<ComplianceResponseDTO> getChecksByInspectionId(Long inspectionId);

    List<ComplianceResponseDTO> getAllChecks();

    // Added requesterId and role for security validation
    ComplianceResponseDTO updateCheck(Long checkId, ComplianceRequestDTO details, Long requesterId, String role);

    // Added requesterId and role for security validation
    void deleteCheck(Long checkId, Long requesterId, String role);
}