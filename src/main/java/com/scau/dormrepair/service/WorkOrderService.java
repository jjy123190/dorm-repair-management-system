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
     */
    WorkOrderResponse assign(AssignWorkOrderCommand command);

    /**
     * 查询工单详情。
     */
    WorkOrderResponse getById(Long id);

    /**
     * 分页查询工单。
     */
    Page<WorkOrderResponse> page(WorkOrderStatus status, Long workerId, int page, int size);

    /**
     * 更新工单状态。
     */
    WorkOrderResponse updateStatus(Long workOrderId, UpdateWorkOrderStatusCommand command);

    /**
     * 提交学生评价。
     */
    void submitFeedback(SubmitRepairFeedbackCommand command);
}
