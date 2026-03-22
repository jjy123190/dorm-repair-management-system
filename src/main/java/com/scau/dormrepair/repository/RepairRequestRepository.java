package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 报修单数据访问接口。
 */
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {

    /**
     * 按状态分页查询报修单。
     * @param status 报修状态
     * @param pageable 分页参数
     * @return 报修单分页结果
     */
    Page<RepairRequest> findAllByStatus(RepairRequestStatus status, Pageable pageable);

    /**
     * 统计某个时间区间内的报修单总数。
     */
    long countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(LocalDateTime start, LocalDateTime end);

    /**
     * 统计某个时间区间内指定状态的报修单数量。
     */
    long countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThanAndStatus(
            LocalDateTime start,
            LocalDateTime end,
            RepairRequestStatus status
    );
}
