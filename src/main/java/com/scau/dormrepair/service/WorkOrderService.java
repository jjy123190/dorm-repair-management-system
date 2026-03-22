package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.web.dto.AssignWorkOrderCommand;
import com.scau.dormrepair.web.dto.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.web.dto.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.web.dto.WorkOrderResponse;
import org.springframework.data.domain.Page;

/**
 * 工单服务接口。
 */
public interface WorkOrderService {

    /**
     * 管理员创建并派发工单。
     * @param command 派单数据
     * @return 创建后的工单信息
     */
    WorkOrderResponse assign(AssignWorkOrderCommand command);

    /**
     * 查询工单详情。
     * @param id 工单 ID
     * @return 工单详情
     */
    WorkOrderResponse getById(Long id);

    /**
     * 分页查询工单。
     * @param status 可选的工单状态
     * @param workerId 可选的维修人员 ID
     * @param page 页码
     * @param size 每页条数
     * @return 工单分页结果
     */
    Page<WorkOrderResponse> page(WorkOrderStatus status, Long workerId, int page, int size);

    /**
     * 更新工单状态。
     * @param workOrderId 工单 ID
     * @param command 状态更新数据
     * @return 更新后的工单信息
     */
    WorkOrderResponse updateStatus(Long workOrderId, UpdateWorkOrderStatusCommand command);

    /**
     * 提交学生评价。
     * @param command 评价数据
     */
    void submitFeedback(SubmitRepairFeedbackCommand command);
}
