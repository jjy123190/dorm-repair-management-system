package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.command.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.domain.view.ActiveWorkOrderView;
import com.scau.dormrepair.domain.view.WorkOrderDetailView;
import java.util.List;

public interface WorkOrderService {

    Long assign(AssignWorkOrderCommand command);

    void updateStatus(UpdateWorkOrderStatusCommand command);

    List<ActiveWorkOrderView> listActiveWorkOrders(int limit);

    List<ActiveWorkOrderView> listWorkerActiveWorkOrders(Long workerId, int limit);

    List<WorkOrderDetailView> listAdminTrackedWorkOrders(int limit);

    List<WorkOrderDetailView> listWorkerTrackedWorkOrders(Long workerId, int limit);

    WorkOrderDetailView getAdminWorkOrderDetail(Long workOrderId);

    WorkOrderDetailView getWorkerWorkOrderDetail(Long workerId, Long workOrderId);
}
