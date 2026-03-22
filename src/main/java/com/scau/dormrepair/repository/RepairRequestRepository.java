package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {

    Page<RepairRequest> findAllByStatus(RepairRequestStatus status, Pageable pageable);

    long countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(LocalDateTime start, LocalDateTime end);

    long countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThanAndStatus(
            LocalDateTime start,
            LocalDateTime end,
            RepairRequestStatus status
    );
}
