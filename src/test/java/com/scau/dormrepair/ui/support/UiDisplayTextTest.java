package com.scau.dormrepair.ui.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import org.junit.jupiter.api.Test;

class UiDisplayTextTest {

    @Test
    void shouldRenderFaultCategoriesInChinese() {
        assertEquals("电路电器", UiDisplayText.faultCategory(FaultCategory.ELECTRICITY));
        assertEquals("水管龙头", UiDisplayText.faultCategory(FaultCategory.WATER_PIPE));
        assertEquals("公共区域", UiDisplayText.faultCategory(FaultCategory.PUBLIC_AREA));
        assertEquals("其他问题", UiDisplayText.faultCategory(FaultCategory.OTHER));
    }

    @Test
    void shouldRenderRepairRequestStatusesInChinese() {
        assertEquals("已提交", UiDisplayText.repairRequestStatus(RepairRequestStatus.SUBMITTED));
        assertEquals("已受理", UiDisplayText.repairRequestStatus(RepairRequestStatus.ASSIGNED));
        assertEquals("处理中", UiDisplayText.repairRequestStatus(RepairRequestStatus.IN_PROGRESS));
        assertEquals("已处理", UiDisplayText.repairRequestStatus(RepairRequestStatus.COMPLETED));
        assertEquals("已驳回", UiDisplayText.repairRequestStatus(RepairRequestStatus.REJECTED));
        assertEquals("已取消", UiDisplayText.repairRequestStatus(RepairRequestStatus.CANCELLED));
    }

    @Test
    void shouldRenderWorkOrderPrioritiesAndStatusesInChinese() {
        assertEquals("低", UiDisplayText.workOrderPriority(WorkOrderPriority.LOW));
        assertEquals("普通", UiDisplayText.workOrderPriority(WorkOrderPriority.NORMAL));
        assertEquals("高", UiDisplayText.workOrderPriority(WorkOrderPriority.HIGH));
        assertEquals("紧急", UiDisplayText.workOrderPriority(WorkOrderPriority.URGENT));

        assertEquals("已派单", UiDisplayText.workOrderStatus(WorkOrderStatus.ASSIGNED));
        assertEquals("已受理", UiDisplayText.workOrderStatus(WorkOrderStatus.ACCEPTED));
        assertEquals("处理中", UiDisplayText.workOrderStatus(WorkOrderStatus.IN_PROGRESS));
        assertEquals("待配件", UiDisplayText.workOrderStatus(WorkOrderStatus.WAITING_PARTS));
        assertEquals("已处理", UiDisplayText.workOrderStatus(WorkOrderStatus.COMPLETED));
        assertEquals("已驳回", UiDisplayText.workOrderStatus(WorkOrderStatus.REJECTED));
        assertEquals("已取消", UiDisplayText.workOrderStatus(WorkOrderStatus.CANCELLED));
    }
}