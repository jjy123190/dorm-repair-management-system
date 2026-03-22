package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.RepairFeedback;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 评价反馈数据访问接口。
 */
public interface RepairFeedbackRepository extends JpaRepository<RepairFeedback, Long> {

    boolean existsByRepairRequestId(Long repairRequestId);

    @Query("""
            select avg(r.rating)
            from RepairFeedback r
            where r.createdAt >= :start and r.createdAt < :end
            """)
    /**
     * 统计一个时间区间内的平均评分。
     */
    Double findAverageRatingBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
