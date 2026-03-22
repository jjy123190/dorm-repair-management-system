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
     * @param command 报修申请数据
     * @return 新建后的报修单详情
     */
    RepairRequestDetailResponse create(CreateRepairRequestCommand command);

    /**
     * 查询报修单详情。
     * @param id 报修单 ID
     * @return 报修单详情
     */
    RepairRequestDetailResponse getById(Long id);

    /**
     * 分页查询报修单。
     * @param status 可选的报修状态筛选
     * @param page 页码
     * @param size 每页条数
     * @return 报修单分页结果
     */
    Page<RepairRequestSummaryResponse> page(RepairRequestStatus status, int page, int size);
}
