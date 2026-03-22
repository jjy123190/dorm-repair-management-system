package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.repository.RepairFeedbackRepository;
import com.scau.dormrepair.repository.RepairRequestRepository;
import com.scau.dormrepair.service.StatisticsService;
import com.scau.dormrepair.web.dto.MonthlyStatisticsResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 报表统计服务实现。
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    /**
     * 报修单数据访问对象。
     */
    private final RepairRequestRepository repairRequestRepository;
    /**
     * 评价数据访问对象。
     */
    private final RepairFeedbackRepository repairFeedbackRepository;

    public StatisticsServiceImpl(
            RepairRequestRepository repairRequestRepository,
            RepairFeedbackRepository repairFeedbackRepository
    ) {
        this.repairRequestRepository = repairRequestRepository;
        this.repairFeedbackRepository = repairFeedbackRepository;
    }

    /**
     * 统计指定月份的报修汇总。
     * @param month 统计月份
     * @return 月度汇总结果
     */
    @Override
    @Transactional(readOnly = true)
    public MonthlyStatisticsResponse monthlySummary(YearMonth month) {
        // 统计区间采用 [start, end) 半开区间，避免月底边界重复计算。
        YearMonth targetMonth = month == null ? YearMonth.now() : month;
        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

        long total = repairRequestRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(start, end);
        long submitted = repairRequestRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThanAndStatus(
                start,
                end,
                RepairRequestStatus.SUBMITTED
        );
        long assigned = repairRequestRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThanAndStatus(
                start,
                end,
                RepairRequestStatus.ASSIGNED
        );
        long inProgress = repairRequestRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThanAndStatus(
                start,
                end,
                RepairRequestStatus.IN_PROGRESS
        );
        long completed = repairRequestRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThanAndStatus(
                start,
                end,
                RepairRequestStatus.COMPLETED
        );
        long rejected = repairRequestRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThanAndStatus(
                start,
                end,
                RepairRequestStatus.REJECTED
        );

        Double averageRating = repairFeedbackRepository.findAverageRatingBetween(start, end);
        BigDecimal rating = averageRating == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP);

        return new MonthlyStatisticsResponse(
                targetMonth.toString(),
                total,
                submitted,
                assigned,
                inProgress,
                completed,
                rejected,
                rating
        );
    }
}
