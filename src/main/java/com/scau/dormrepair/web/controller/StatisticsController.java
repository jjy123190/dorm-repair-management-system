package com.scau.dormrepair.web.controller;

import com.scau.dormrepair.common.ApiResponse;
import com.scau.dormrepair.service.StatisticsService;
import com.scau.dormrepair.web.dto.MonthlyStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报表控制器。
 */
@RestController
@RequestMapping("/api/v1/reports/repair-requests")
@Tag(name = "Reports", description = "报表接口")
public class StatisticsController {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 报表业务入口。
     */
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 查询报修月度汇总。
     * @param month 月份，格式 yyyy-MM
     * @return 月度汇总结果
     */
    @GetMapping("/monthly-summary")
    @Operation(summary = "查询报修月度汇总")
    public ApiResponse<MonthlyStatisticsResponse> monthlySummary(@RequestParam(required = false) String month) {
        YearMonth yearMonth = month == null || month.isBlank() ? null : YearMonth.parse(month, MONTH_FORMATTER);
        return ApiResponse.ok(statisticsService.monthlySummary(yearMonth));
    }
}
