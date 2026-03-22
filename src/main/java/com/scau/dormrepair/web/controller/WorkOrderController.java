package com.scau.dormrepair.web.controller;

import com.scau.dormrepair.common.ApiResponse;
import com.scau.dormrepair.service.WorkOrderService;
import com.scau.dormrepair.web.dto.AssignWorkOrderCommand;
import com.scau.dormrepair.web.dto.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.web.dto.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.web.dto.WorkOrderResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping("/assign")
    public ApiResponse<WorkOrderResponse> assign(@Valid @RequestBody AssignWorkOrderCommand command) {
        return ApiResponse.created("工单已创建并派发", workOrderService.assign(command));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<WorkOrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkOrderStatusCommand command
    ) {
        return ApiResponse.ok(workOrderService.updateStatus(id, command));
    }

    @PostMapping("/feedback")
    public ApiResponse<Void> submitFeedback(@Valid @RequestBody SubmitRepairFeedbackCommand command) {
        workOrderService.submitFeedback(command);
        return ApiResponse.created("评价已提交", null);
    }
}
