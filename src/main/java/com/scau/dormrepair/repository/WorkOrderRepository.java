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

    /**
     * 按报修单 ID 查询工单。
     * 一条报修单最多对应一张工单。
     */
    Optional<WorkOrder> findByRepairRequestId(Long repairRequestId);

    /**
     * 按状态分页查询工单。
     */
    Page<WorkOrder> findAllByStatus(WorkOrderStatus status, Pageable pageable);

    /**
     * 按维修人员分页查询工单。
     */
    Page<WorkOrder> findAllByWorkerId(Long workerId, Pageable pageable);

    /**
     * 按状态 + 维修人员分页查询工单。
     */
    Page<WorkOrder> findAllByStatusAndWorkerId(WorkOrderStatus status, Long workerId, Pageable pageable);
}
