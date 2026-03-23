package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.command.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.domain.view.ActiveWorkOrderView;
import java.util.List;

/**
 * 工单服务。
 */
public interface WorkOrderService {

    Long assign(AssignWorkOrderCommand command);

    void updateStatus(UpdateWorkOrderStatusCommand command);

    List<ActiveWorkOrderView> listActiveWorkOrders(int limit);
}
