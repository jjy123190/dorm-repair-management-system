package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.RepairFeedback;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepairFeedbackRepository extends JpaRepository<RepairFeedback, Long> {

    boolean existsByRepairRequestId(Long repairRequestId);

    @Query("""
            select avg(r.rating)
            from RepairFeedback r
            where r.createdAt >= :start and r.createdAt < :end
            """)
    Double findAverageRatingBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
