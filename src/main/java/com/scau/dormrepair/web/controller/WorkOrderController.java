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

/**
 * 工单控制器。
 */
@RestController
@RequestMapping("/api/v1/work-orders")
@Tag(name = "Work Orders", description = "工单管理接口")
public class WorkOrderController {

    /**
     * 工单业务入口。
     */
    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    /**
     * 创建并派发工单。
     * @param command 派单数据
     * @return 创建后的工单信息
     */
    @PostMapping
    @Operation(summary = "创建并派发工单")
    public ApiResponse<WorkOrderResponse> assign(@Valid @RequestBody AssignWorkOrderCommand command) {
        return ApiResponse.created("工单已创建并派发", workOrderService.assign(command));
    }

    /**
     * 查询工单详情。
     * @param id 工单 ID
     * @return 工单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询工单详情")
    public ApiResponse<WorkOrderResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(workOrderService.getById(id));
    }

    /**
     * 分页查询工单。
     * @param status 工单状态
     * @param workerId 维修人员 ID
     * @param page 页码
     * @param size 每页条数
     * @return 工单分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询工单")
    public ApiResponse<PageResponse<WorkOrderResponse>> page(
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false) Long workerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 查询结果统一转换成项目固定的分页结构，方便前端复用。
        Page<WorkOrderResponse> pageData = workOrderService.page(status, workerId, page, size);
        return ApiResponse.ok(PageResponse.from(pageData));
    }

    /**
     * 更新工单状态。
     * @param id 工单 ID
     * @param command 状态更新数据
     * @return 更新后的工单信息
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新工单状态")
    public ApiResponse<WorkOrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkOrderStatusCommand command
    ) {
        return ApiResponse.ok(workOrderService.updateStatus(id, command));
    }
}
