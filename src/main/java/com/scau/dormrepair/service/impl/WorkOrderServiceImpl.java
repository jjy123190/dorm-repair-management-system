package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.BusinessNumberGenerator;
import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.command.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.entity.WorkOrder;
import com.scau.dormrepair.domain.entity.WorkOrderRecord;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.ActiveWorkOrderView;
import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import com.scau.dormrepair.mapper.RepairRequestMapper;
import com.scau.dormrepair.mapper.WorkOrderMapper;
import com.scau.dormrepair.mapper.WorkOrderRecordMapper;
import com.scau.dormrepair.service.WorkOrderService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单服务实现。
 */
public class WorkOrderServiceImpl implements WorkOrderService {

    private final MyBatisExecutor myBatisExecutor;

    public WorkOrderServiceImpl(MyBatisExecutor myBatisExecutor) {
        this.myBatisExecutor = myBatisExecutor;
    }

    @Override
    public Long assign(AssignWorkOrderCommand command) {
        if (command.repairRequestId() == null) {
            throw new BusinessException("报修单 ID 不能为空");
        }
        if (command.adminId() == null || command.workerId() == null) {
            throw new BusinessException("派单管理员和维修人员不能为空");
        }
        if (command.priority() == null) {
            throw new BusinessException("工单优先级不能为空");
        }

        return myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            WorkOrderMapper workOrderMapper = session.getMapper(WorkOrderMapper.class);
            WorkOrderRecordMapper workOrderRecordMapper = session.getMapper(WorkOrderRecordMapper.class);

            RepairRequest repairRequest = repairRequestMapper.selectById(command.repairRequestId());
            if (repairRequest == null) {
                throw new ResourceNotFoundException("未找到报修单，ID=" + command.repairRequestId());
            }
            if (workOrderMapper.selectByRepairRequestId(command.repairRequestId()) != null) {
                throw new BusinessException("该报修单已经生成工单");
            }
            if (repairRequest.getStatus() == RepairRequestStatus.COMPLETED
                    || repairRequest.getStatus() == RepairRequestStatus.CANCELLED
                    || repairRequest.getStatus() == RepairRequestStatus.REJECTED) {
                throw new BusinessException("当前报修状态不允许再派单");
            }

            WorkOrder workOrder = new WorkOrder();
            workOrder.setWorkOrderNo(BusinessNumberGenerator.nextWorkOrderNo());
            workOrder.setRepairRequestId(command.repairRequestId());
            workOrder.setAdminId(command.adminId());
            workOrder.setWorkerId(command.workerId());
            workOrder.setPriority(command.priority());
            workOrder.setStatus(WorkOrderStatus.ASSIGNED);
            workOrder.setAssignmentNote(command.assignmentNote());
            workOrder.setAssignedAt(LocalDateTime.now());
            workOrderMapper.insert(workOrder);

            repairRequestMapper.updateAssignmentInfo(
                    command.repairRequestId(),
                    RepairRequestStatus.ASSIGNED,
                    command.adminId(),
                    command.workerId()
            );

            WorkOrderRecord workOrderRecord = new WorkOrderRecord();
            workOrderRecord.setWorkOrderId(workOrder.getId());
            workOrderRecord.setOperatorId(command.adminId());
            workOrderRecord.setStatus(WorkOrderStatus.ASSIGNED);
            workOrderRecord.setRecordNote(command.assignmentNote());
            workOrderRecordMapper.insert(workOrderRecord);
            return workOrder.getId();
        });
    }

    @Override
    public void updateStatus(UpdateWorkOrderStatusCommand command) {
        if (command.workOrderId() == null || command.operatorId() == null || command.status() == null) {
            throw new BusinessException("工单状态更新参数不完整");
        }

        myBatisExecutor.executeWrite(session -> {
            WorkOrderMapper workOrderMapper = session.getMapper(WorkOrderMapper.class);
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            WorkOrderRecordMapper workOrderRecordMapper = session.getMapper(WorkOrderRecordMapper.class);

            WorkOrder workOrder = workOrderMapper.selectById(command.workOrderId());
            if (workOrder == null) {
                throw new ResourceNotFoundException("未找到工单，ID=" + command.workOrderId());
            }

            LocalDateTime acceptedAt = workOrder.getAcceptedAt();
            LocalDateTime completedAt = workOrder.getCompletedAt();

            if (command.status() == WorkOrderStatus.ACCEPTED && acceptedAt == null) {
                acceptedAt = LocalDateTime.now();
            }
            if (isFinishedStatus(command.status())) {
                completedAt = LocalDateTime.now();
            }

            workOrderMapper.updateStatus(workOrder.getId(), command.status(), acceptedAt, completedAt);
            repairRequestMapper.updateStatus(
                    workOrder.getRepairRequestId(),
                    mapRequestStatus(command.status()),
                    command.status() == WorkOrderStatus.COMPLETED ? completedAt : null
            );

            WorkOrderRecord workOrderRecord = new WorkOrderRecord();
            workOrderRecord.setWorkOrderId(workOrder.getId());
            workOrderRecord.setOperatorId(command.operatorId());
            workOrderRecord.setStatus(command.status());
            workOrderRecord.setRecordNote(command.recordNote());
            workOrderRecordMapper.insert(workOrderRecord);
            return null;
        });
    }

    @Override
    public List<ActiveWorkOrderView> listActiveWorkOrders(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return myBatisExecutor.executeRead(
                session -> session.getMapper(WorkOrderMapper.class).selectActiveWorkOrders(safeLimit)
        );
    }

    @Override
    public List<ActiveWorkOrderView> listWorkerActiveWorkOrders(Long workerId, int limit) {
        if (workerId == null) {
            throw new BusinessException("维修员ID不能为空");
        }

        // 维修员工作台应该只看到派给自己的在途工单，不应该把别人的处理单混进来。
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return myBatisExecutor.executeRead(
                session -> session.getMapper(WorkOrderMapper.class).selectWorkerActiveWorkOrders(workerId, safeLimit)
        );
    }

    private boolean isFinishedStatus(WorkOrderStatus workOrderStatus) {
        return workOrderStatus == WorkOrderStatus.COMPLETED
                || workOrderStatus == WorkOrderStatus.CANCELLED
                || workOrderStatus == WorkOrderStatus.REJECTED;
    }

    /**
     * 工单状态和报修状态需要同步，但两边的枚举语义不完全一样，这里集中做映射。
     */
    private RepairRequestStatus mapRequestStatus(WorkOrderStatus workOrderStatus) {
        return switch (workOrderStatus) {
            case ASSIGNED -> RepairRequestStatus.ASSIGNED;
            case ACCEPTED, IN_PROGRESS, WAITING_PARTS -> RepairRequestStatus.IN_PROGRESS;
            case COMPLETED -> RepairRequestStatus.COMPLETED;
            case REJECTED -> RepairRequestStatus.REJECTED;
            case CANCELLED -> RepairRequestStatus.CANCELLED;
        };
    }
}
