package com.scau.dormrepair.web.controller;

import com.scau.dormrepair.common.ApiResponse;
import com.scau.dormrepair.service.WorkOrderService;
import com.scau.dormrepair.web.dto.SubmitRepairFeedbackCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价反馈控制器。
 */
@RestController
@RequestMapping("/api/v1/repair-feedbacks")
@Tag(name = "Repair Feedbacks", description = "评价反馈接口")
public class RepairFeedbackController {

    /**
     * 评价业务入口。
     */
    private final WorkOrderService workOrderService;

    public RepairFeedbackController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    /**
     * 提交评价。
     * @param command 评价数据
     * @return 空响应
     */
    @PostMapping
    @Operation(summary = "提交报修评价")
    public ApiResponse<Void> create(@Valid @RequestBody SubmitRepairFeedbackCommand command) {
        workOrderService.submitFeedback(command);
        return ApiResponse.created("评价已提交", null);
    }
}
