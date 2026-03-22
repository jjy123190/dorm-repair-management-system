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

    /**
     * 判断某条报修单是否已经提交过评价。
     * @param repairRequestId 报修单 ID
     * @return 已评价返回 true，否则返回 false
     */
    boolean existsByRepairRequestId(Long repairRequestId);

    /**
     * 统计一个时间区间内的平均评分。
     * @param start 统计开始时间
     * @param end 统计结束时间
     * @return 平均分，如果没有数据会返回 null
     */
    @Query("""
            select avg(r.rating)
            from RepairFeedback r
            where r.createdAt >= :start and r.createdAt < :end
            """)
    Double findAverageRatingBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
