package com.scau.dormrepair.web.controller;

import com.scau.dormrepair.common.ApiResponse;
import com.scau.dormrepair.common.PageResponse;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.service.WorkOrderService;
import com.scau.dormrepair.web.dto.AssignWorkOrderCommand;
import com.scau.dormrepair.web.dto.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.web.dto.WorkOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/work-orders")
@Tag(name = "Work Orders", description = "工单管理接口")
/**
 * 工单控制器。
 */
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    @Operation(summary = "创建并派发工单")
    public ApiResponse<WorkOrderResponse> assign(@Valid @RequestBody AssignWorkOrderCommand command) {
        return ApiResponse.created("工单已创建并派发", workOrderService.assign(command));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询工单详情")
    public ApiResponse<WorkOrderResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(workOrderService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询工单")
    public ApiResponse<PageResponse<WorkOrderResponse>> page(
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false) Long workerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<WorkOrderResponse> pageData = workOrderService.page(status, workerId, page, size);
        return ApiResponse.ok(PageResponse.from(pageData));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新工单状态")
    public ApiResponse<WorkOrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkOrderStatusCommand command
    ) {
        return ApiResponse.ok(workOrderService.updateStatus(id, command));
    }
}
