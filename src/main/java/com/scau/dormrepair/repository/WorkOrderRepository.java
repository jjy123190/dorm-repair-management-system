package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.WorkOrder;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 工单数据访问接口。
 */
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByRepairRequestId(Long repairRequestId);

    Page<WorkOrder> findAllByStatus(WorkOrderStatus status, Pageable pageable);

    Page<WorkOrder> findAllByWorkerId(Long workerId, Pageable pageable);

    Page<WorkOrder> findAllByStatusAndWorkerId(WorkOrderStatus status, Long workerId, Pageable pageable);
}
