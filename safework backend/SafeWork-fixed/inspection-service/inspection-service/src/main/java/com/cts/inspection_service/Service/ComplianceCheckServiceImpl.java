//package com.cts.inspection_service.Service;
//
//import com.cts.inspection_service.Dto.ComplianceRequestDTO;
//import com.cts.inspection_service.Dto.ComplianceResponseDTO;
//import com.cts.inspection_service.Entity.ComplianceCheck;
//import com.cts.inspection_service.Repository.ComplianceCheckRepository;
//import com.cts.inspection_service.Repository.InspectionRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import jakarta.transaction.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//public class ComplianceCheckServiceImpl implements IComplianceCheckService {
//
//    @Autowired
//    private ComplianceCheckRepository repository;
//
//    @Autowired
//    private InspectionRepository inspectionRepository;
//
//    @Override
//    @Transactional
//    public ComplianceResponseDTO createCheck(ComplianceRequestDTO dto, Long requesterId, String role) {
//        if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied.");
//        }
//
//        if (!inspectionRepository.existsById(dto.getInspectionId())) {
//            throw new RuntimeException("Inspection ID " + dto.getInspectionId() + " not found.");
//        }
//
//        ComplianceCheck entity = new ComplianceCheck();
//        BeanUtils.copyProperties(dto, entity);
//        return mapToDTO(repository.save(entity));
//    }
//
//    @Override
//    public List<ComplianceResponseDTO> getChecksByInspectionId(Long inspectionId) {
//        return repository.findByInspectionId(inspectionId).stream().map(this::mapToDTO).collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ComplianceResponseDTO> getAllChecks() {
//        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public ComplianceResponseDTO updateCheck(Long checkId, ComplianceRequestDTO details, Long requesterId, String role) {
//        if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied.");
//        }
//
//        ComplianceCheck existing = repository.findById(checkId)
//                .orElseThrow(() -> new RuntimeException("Check not found"));
//
//        BeanUtils.copyProperties(details, existing, "checkId");
//        return mapToDTO(repository.save(existing));
//    }
//
//    @Override
//    @Transactional
//    public void deleteCheck(Long checkId, Long requesterId, String role) {
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Only Admin can delete compliance checks.");
//        }
//        if(!repository.existsById(checkId)) throw new RuntimeException("Check not found");
//        repository.deleteById(checkId);
//    }
//
//    private ComplianceResponseDTO mapToDTO(ComplianceCheck entity) {
//        ComplianceResponseDTO dto = new ComplianceResponseDTO();
//        BeanUtils.copyProperties(entity, dto);
//        return dto;
//    }
//}

package com.cts.inspection_service.Service;

import com.cts.inspection_service.Dto.ComplianceRequestDTO;
import com.cts.inspection_service.Dto.ComplianceResponseDTO;
import com.cts.inspection_service.Entity.ComplianceCheck;
import com.cts.inspection_service.Repository.ComplianceCheckRepository;
import com.cts.inspection_service.Repository.InspectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ComplianceCheckServiceImpl implements IComplianceCheckService {

    @Autowired
    private ComplianceCheckRepository repository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Override
    @Transactional
    public ComplianceResponseDTO createCheck(ComplianceRequestDTO dto, Long requesterId, String role) {
        log.info("Role received in Compliance Service: {}", role);

        // FIX: Added SAFETY_OFFICER to allow creation
        if (!"ADMIN".equalsIgnoreCase(role) &&
                !"OFFICER".equalsIgnoreCase(role) &&
                !"SAFETY_OFFICER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied.");
        }

        if (!inspectionRepository.existsById(dto.getInspectionId())) {
            throw new RuntimeException("Inspection ID " + dto.getInspectionId() + " not found.");
        }

        ComplianceCheck entity = new ComplianceCheck();
        BeanUtils.copyProperties(dto, entity);
        return mapToDTO(repository.save(entity));
    }

    @Override
    public List<ComplianceResponseDTO> getChecksByInspectionId(Long inspectionId) {
        return repository.findByInspectionId(inspectionId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ComplianceResponseDTO> getAllChecks() {
        return repository.findAll().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComplianceResponseDTO updateCheck(Long checkId, ComplianceRequestDTO details, Long requesterId, String role) {
        // FIX: Added SAFETY_OFFICER to allow updates
        if (!"ADMIN".equalsIgnoreCase(role) &&
                !"OFFICER".equalsIgnoreCase(role) &&
                !"SAFETY_OFFICER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied.");
        }

        ComplianceCheck existing = repository.findById(checkId)
                .orElseThrow(() -> new RuntimeException("Check not found"));

        BeanUtils.copyProperties(details, existing, "checkId");
        return mapToDTO(repository.save(existing));
    }

    @Override
    @Transactional
    public void deleteCheck(Long checkId, Long requesterId, String role) {
        // FIX: If you want Safety Officers to delete, add them here too.
        // Currently, only ADMIN is allowed as per your requirement.
        if (!"ADMIN".equalsIgnoreCase(role) && !"SAFETY_OFFICER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only Admin or Safety Officer can delete compliance checks.");
        }

        if(!repository.existsById(checkId)) {
            throw new RuntimeException("Check not found");
        }
        repository.deleteById(checkId);
    }

    private ComplianceResponseDTO mapToDTO(ComplianceCheck entity) {
        ComplianceResponseDTO dto = new ComplianceResponseDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}