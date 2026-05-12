package com.cts.inspection_service.Repository;


import com.cts.inspection_service.Entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
}