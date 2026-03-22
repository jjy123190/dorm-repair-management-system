package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.web.dto.CreateRepairRequestCommand;
import com.scau.dormrepair.web.dto.RepairRequestDetailResponse;
import com.scau.dormrepair.web.dto.RepairRequestSummaryResponse;
import org.springframework.data.domain.Page;

/**
 * 报修单服务接口。
 */
public interface RepairRequestService {

    /**
     * 学生提交报修申请。
     */
    RepairRequestDetailResponse create(CreateRepairRequestCommand command);

    /**
     * 查询报修单详情。
     */
    RepairRequestDetailResponse getById(Long id);

    /**
     * 分页查询报修单。
     */
    Page<RepairRequestSummaryResponse> page(RepairRequestStatus status, int page, int size);
}
