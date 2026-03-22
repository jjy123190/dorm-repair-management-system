package com.scau.dormrepair.web.controller;

import com.scau.dormrepair.common.ApiResponse;
import com.scau.dormrepair.service.StatisticsService;
import com.scau.dormrepair.web.dto.MonthlyStatisticsResponse;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/monthly")
    public ApiResponse<MonthlyStatisticsResponse> monthlySummary(@RequestParam(required = false) String month) {
        YearMonth yearMonth = month == null || month.isBlank() ? null : YearMonth.parse(month, MONTH_FORMATTER);
        return ApiResponse.ok(statisticsService.monthlySummary(yearMonth));
    }
}
