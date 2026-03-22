package com.scau.dormrepair.web.controller;

import com.scau.dormrepair.common.ApiResponse;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.service.RepairRequestService;
import com.scau.dormrepair.web.dto.CreateRepairRequestCommand;
import com.scau.dormrepair.web.dto.RepairRequestDetailResponse;
import com.scau.dormrepair.web.dto.RepairRequestSummaryResponse;
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
@RequestMapping("/api/repair-requests")
public class RepairRequestController {

    private final RepairRequestService repairRequestService;

    public RepairRequestController(RepairRequestService repairRequestService) {
        this.repairRequestService = repairRequestService;
    }

    @PostMapping
    public ApiResponse<RepairRequestDetailResponse> create(@Valid @RequestBody CreateRepairRequestCommand command) {
        return ApiResponse.created("报修申请已提交", repairRequestService.create(command));
    }

    @GetMapping("/{id}")
    public ApiResponse<RepairRequestDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(repairRequestService.getById(id));
    }

    @GetMapping
    public ApiResponse<Page<RepairRequestSummaryResponse>> page(
            @RequestParam(required = false) RepairRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(repairRequestService.page(status, page, size));
    }
}
