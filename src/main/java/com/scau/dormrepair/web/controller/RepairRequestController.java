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

@RestController
@RequestMapping("/api/v1/repair-requests")
@Tag(name = "Repair Requests", description = "报修申请接口")
/**
 * 报修申请控制器。
 */
public class RepairRequestController {

    private final RepairRequestService repairRequestService;

    public RepairRequestController(RepairRequestService repairRequestService) {
        this.repairRequestService = repairRequestService;
    }

    @PostMapping
    @Operation(summary = "提交报修申请")
    public ApiResponse<RepairRequestDetailResponse> create(@Valid @RequestBody CreateRepairRequestCommand command) {
        return ApiResponse.created("报修申请已提交", repairRequestService.create(command));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询报修详情")
    public ApiResponse<RepairRequestDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(repairRequestService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询报修单")
    public ApiResponse<PageResponse<RepairRequestSummaryResponse>> page(
            @RequestParam(required = false) RepairRequestStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<RepairRequestSummaryResponse> pageData = repairRequestService.page(status, page, size);
        return ApiResponse.ok(PageResponse.from(pageData));
    }
}
