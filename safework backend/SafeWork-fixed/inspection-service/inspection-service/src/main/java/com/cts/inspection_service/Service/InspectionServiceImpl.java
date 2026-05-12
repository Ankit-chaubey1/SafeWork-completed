//package com.cts.inspection_service.Service;
//
//import com.cts.inspection_service.Dto.InspectionRequestDTO;
//import com.cts.inspection_service.Dto.InspectionResponseDTO;
//import com.cts.inspection_service.Entity.Inspection;
//import com.cts.inspection_service.FleignClient.UserClient;
//import com.cts.inspection_service.FleignClient.UserPublicDto;
//import com.cts.inspection_service.Repository.InspectionRepository;
//import feign.FeignException;
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
//public class InspectionServiceImpl implements IInspectionService {
//
//    @Autowired
//    private InspectionRepository repository;
//
//    @Autowired
//    private UserClient userClient;
//
//    @Override
//    @Transactional
//    public InspectionResponseDTO createInspection(InspectionRequestDTO requestDTO, Long requesterId, String role) {
//        log.info("User {} with role {} is attempting to create an inspection", requesterId, role);
//
//        // Updated Security Check to allow SAFETY_OFFICER
//        validateAuthorizedRole(role);
//
//        UserPublicDto user;
//        try {
//            // Ensure requestDTO.getOfficerId() is not null in your JSON request
//            user = userClient.getUserById(requestDTO.getOfficerId());
//        } catch (FeignException.NotFound e) {
//            throw new RuntimeException("Validation Failed: Officer with ID " + requestDTO.getOfficerId() + " does not exist.");
//        } catch (FeignException e) {
//            log.error("Feign communication error: {}", e.getMessage());
//            throw new RuntimeException("User Service is currently unavailable.");
//        }
//
//        Inspection inspection = new Inspection();
//        BeanUtils.copyProperties(requestDTO, inspection);
//
//        // Enrich the entity with data from User-Service
//        inspection.setOfficerName(user.getUserName());
//        inspection.setOfficerEmail(user.getUserEmail());
//        inspection.setOfficerId(user.getUserId());
//
//        Inspection savedInspection = repository.save(inspection);
//        log.info("Inspection created successfully with ID: {}", savedInspection.getInspectionId());
//
//        return mapToDTO(savedInspection);
//    }
//
//    @Override
//    public List<InspectionResponseDTO> getAllInspections() {
//        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
//    }
//
//    @Override
//    public InspectionResponseDTO getInspectionById(Long id) {
//        return repository.findById(id).map(this::mapToDTO)
//                .orElseThrow(() -> new RuntimeException("Inspection not found: " + id));
//    }
//
//    @Override
//    @Transactional
//    public InspectionResponseDTO updateInspection(Long id, InspectionRequestDTO details, Long requesterId, String role) {
//        validateAuthorizedRole(role);
//
//        Inspection existing = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Inspection not found"));
//
//        BeanUtils.copyProperties(details, existing, "inspectionId");
//        return mapToDTO(repository.save(existing));
//    }
//
//    @Override
//    @Transactional
//    public void deleteInspection(Long id, Long requesterId, String role) {
//        log.info("Delete request for inspection: {} by user: {}", id, requesterId);
//
//        if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied: Insufficient permissions.");
//        }
//
//        if (!repository.existsById(id)) {
//            throw new RuntimeException("Inspection not found");
//        }
//
//        repository.deleteById(id);
//    }
//
//    private void validateAuthorizedRole(String role) {
//        if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied: Unauthorized role.");
//        }
//    }
//
//    private InspectionResponseDTO mapToDTO(Inspection inspection) {
//        InspectionResponseDTO dto = new InspectionResponseDTO();
//        BeanUtils.copyProperties(inspection, dto);
//        return dto;
//    }
//}

package com.cts.inspection_service.Service;

import com.cts.inspection_service.Dto.InspectionRequestDTO;
import com.cts.inspection_service.Dto.InspectionResponseDTO;
import com.cts.inspection_service.Entity.Inspection;
import com.cts.inspection_service.FleignClient.UserClient;
import com.cts.inspection_service.FleignClient.UserPublicDto;
import com.cts.inspection_service.Repository.InspectionRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InspectionServiceImpl implements IInspectionService {

    @Autowired
    private InspectionRepository repository;

    @Autowired
    private UserClient userClient;

    @Override
    @Transactional
    public InspectionResponseDTO createInspection(InspectionRequestDTO requestDTO, Long requesterId, String role) {
        log.info("User {} with role {} is attempting to create an inspection", requesterId, role);

        // Security Check
        validateAuthorizedRole(role);

        UserPublicDto user;
        try {
            user = userClient.getUserById(requestDTO.getOfficerId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Validation Failed: Officer does not exist.");
        } catch (FeignException e) {
            log.error("Feign error: {}", e.getMessage());
            throw new RuntimeException("User Service unavailable.");
        }

        Inspection inspection = new Inspection();
        BeanUtils.copyProperties(requestDTO, inspection);

        // Enrich data from User-Service
        inspection.setOfficerName(user.getUserName());
        inspection.setOfficerEmail(user.getUserEmail());
        inspection.setOfficerId(user.getUserId());

        return mapToDTO(repository.save(inspection));
    }

    @Override
    public List<InspectionResponseDTO> getAllInspections() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InspectionResponseDTO getInspectionById(Long id) {
        return repository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Inspection not found: " + id));
    }

    @Override
    @Transactional
    public InspectionResponseDTO updateInspection(Long id, InspectionRequestDTO details, Long requesterId,
            String role) {
        validateAuthorizedRole(role);

        Inspection existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inspection not found"));

        BeanUtils.copyProperties(details, existing, "inspectionId");
        return mapToDTO(repository.save(existing));
    }

    @Override
    @Transactional
    public void deleteInspection(Long id, Long requesterId, String role) {
        log.info("Delete request for inspection: {} by user: {}", id, requesterId);

        // Updated to allow SAFETY_OFFICER for deletion
        if (role == null || (!"ADMIN".equalsIgnoreCase(role.trim()) &&
                !"OFFICER".equalsIgnoreCase(role.trim()) &&
                !"SAFETY_OFFICER".equalsIgnoreCase(role.trim()))) {
            throw new RuntimeException("Access Denied: Insufficient permissions.");
        }

        if (!repository.existsById(id)) {
            throw new RuntimeException("Inspection not found");
        }

        repository.deleteById(id);
    }

    /**
     * Helper method to validate roles.
     * Includes SAFETY_OFFICER to match the current JWT configuration.
     */
    private void validateAuthorizedRole(String role) {
        if (role == null || (!"ADMIN".equalsIgnoreCase(role.trim()) &&
                !"OFFICER".equalsIgnoreCase(role.trim()) &&
                !"SAFETY_OFFICER".equalsIgnoreCase(role.trim()))) {

            log.error("Unauthorized role detected: [{}]", role);
            throw new RuntimeException("Access Denied: Unauthorized role.");
        }
    }

    private InspectionResponseDTO mapToDTO(Inspection inspection) {
        InspectionResponseDTO dto = new InspectionResponseDTO();
        BeanUtils.copyProperties(inspection, dto);
        return dto;
    }
}