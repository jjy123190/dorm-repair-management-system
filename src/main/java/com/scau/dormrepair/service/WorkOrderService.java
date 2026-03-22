package com.scau.dormrepair.service;

import com.scau.dormrepair.web.dto.AssignWorkOrderCommand;
import com.scau.dormrepair.web.dto.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.web.dto.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.web.dto.WorkOrderResponse;

public interface WorkOrderService {

    WorkOrderResponse assign(AssignWorkOrderCommand command);

    WorkOrderResponse updateStatus(Long workOrderId, UpdateWorkOrderStatusCommand command);

    void submitFeedback(SubmitRepairFeedbackCommand command);
}
