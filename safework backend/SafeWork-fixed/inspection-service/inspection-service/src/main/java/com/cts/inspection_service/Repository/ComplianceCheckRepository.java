
package com.cts.inspection_service.Repository;


import com.cts.inspection_service.Entity.ComplianceCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplianceCheckRepository extends JpaRepository<ComplianceCheck, Long> {
    List<ComplianceCheck> findByInspectionId(Long inspectionId);
}
