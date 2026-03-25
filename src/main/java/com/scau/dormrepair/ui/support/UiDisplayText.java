package com.scau.dormrepair.ui.support;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;

/**
 * 枚举在数据库和业务层保留英文码，界面展示统一在这里转中文。
 */
public final class UiDisplayText {

    private UiDisplayText() {
    }

    public static String faultCategory(FaultCategory faultCategory) {
        if (faultCategory == null) {
            return "";
        }
        return switch (faultCategory) {
            case ELECTRICITY -> "电路电器";
            case WATER_PIPE -> "水管水龙头";
            case DOOR_WINDOW -> "门窗锁具";
            case NETWORK -> "网络故障";
            case FURNITURE -> "家具设施";
            case PUBLIC_AREA -> "公共区域";
            case OTHER -> "其他问题";
        };
    }

    public static String repairRequestStatus(RepairRequestStatus repairRequestStatus) {
        if (repairRequestStatus == null) {
            return "";
        }
        return switch (repairRequestStatus) {
            case SUBMITTED -> "已提交";
            case ASSIGNED -> "已派单";
            case IN_PROGRESS -> "处理中";
            case COMPLETED -> "已完成";
            case REJECTED -> "已驳回";
            case CANCELLED -> "已取消";
        };
    }

    public static String workOrderPriority(WorkOrderPriority workOrderPriority) {
        if (workOrderPriority == null) {
            return "";
        }
        return switch (workOrderPriority) {
            case LOW -> "低";
            case NORMAL -> "普通";
            case HIGH -> "高";
            case URGENT -> "紧急";
        };
    }

    public static String workOrderStatus(WorkOrderStatus workOrderStatus) {
        if (workOrderStatus == null) {
            return "";
        }
        return switch (workOrderStatus) {
            case ASSIGNED -> "已派单";
            case ACCEPTED -> "已接单";
            case IN_PROGRESS -> "处理中";
            case WAITING_PARTS -> "待配件";
            case COMPLETED -> "已完成";
            case REJECTED -> "已驳回";
            case CANCELLED -> "已取消";
        };
    }
}
