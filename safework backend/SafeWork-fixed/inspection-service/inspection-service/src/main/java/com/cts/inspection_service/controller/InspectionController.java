package com.cts.inspection_service.controller;

import com.cts.inspection_service.Dto.InspectionRequestDTO;
import com.cts.inspection_service.Dto.InspectionResponseDTO;
import com.cts.inspection_service.Service.IInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inspections")
public class InspectionController {

    @Autowired
    private IInspectionService service;

    @PostMapping("/createInspection")
    public ResponseEntity<InspectionResponseDTO> create(
            @RequestBody InspectionRequestDTO dto,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(service.createInspection(dto, requesterId, role));
    }

    @GetMapping("/getallInspection")
    public ResponseEntity<List<InspectionResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllInspections());
    }

    @GetMapping("/getbyInspectionId/{id}")
    public ResponseEntity<InspectionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getInspectionById(id));
    }

    @DeleteMapping("/deleteInspection/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        service.deleteInspection(id, requesterId, role);
        return ResponseEntity.ok("Deleted Successfully");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<InspectionResponseDTO> updateInspection(
            @PathVariable Long id,
            @RequestBody InspectionRequestDTO requestDTO,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(service.updateInspection(id, requestDTO, requesterId, role));
    }
}