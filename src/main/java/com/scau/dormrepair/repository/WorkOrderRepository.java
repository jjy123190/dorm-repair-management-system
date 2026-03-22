package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.WorkOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByRepairRequestId(Long repairRequestId);
}
