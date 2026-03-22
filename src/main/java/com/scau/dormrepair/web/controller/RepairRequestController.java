package com.scau.dormrepair.web.controller;

import com.scau.dormrepair.common.ApiResponse;
import com.scau.dormrepair.common.PageResponse;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.service.RepairRequestService;
import com.scau.dormrepair.web.dto.CreateRepairRequestCommand;
import com.scau.dormrepair.web.dto.RepairRequestDetailResponse;
import com.scau.dormrepair.web.dto.RepairRequestSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报修申请控制器。
 */
@RestController
@RequestMapping("/api/v1/repair-requests")
@Tag(name = "Repair Requests", description = "报修申请接口")
public class RepairRequestController {

    /**
     * 报修申请业务入口。
     */
    private final RepairRequestService repairRequestService;

    public RepairRequestController(RepairRequestService repairRequestService) {
        this.repairRequestService = repairRequestService;
    }

    /**
     * 提交报修申请。
     * @param command 报修申请数据
     * @return 新建后的报修单详情
     */
    @PostMapping
    @Operation(summary = "提交报修申请")
    public ApiResponse<RepairRequestDetailResponse> create(@Valid @RequestBody CreateRepairRequestCommand command) {
        return ApiResponse.created("报修申请已提交", repairRequestService.create(command));
    }

    /**
     * 查询报修单详情。
     * @param id 报修单 ID
     * @return 报修单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询报修详情")
    public ApiResponse<RepairRequestDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(repairRequestService.getById(id));
    }

    /**
     * 分页查询报修单。
     * @param status 可选的状态筛选
     * @param page 页码
     * @param size 每页条数
     * @return 报修单分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询报修单")
    public ApiResponse<PageResponse<RepairRequestSummaryResponse>> page(
            @RequestParam(required = false) RepairRequestStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 统一把 Spring Page 转成项目自己的分页返回结构。
        Page<RepairRequestSummaryResponse> pageData = repairRequestService.page(status, page, size);
        return ApiResponse.ok(PageResponse.from(pageData));
    }
}
