package com.scau.dormrepair.ui.support;
import com.scau.dormrepair.domain.enums.*;
public final class UiDisplayText {
private UiDisplayText(){}
public static String faultCategory(FaultCategory faultCategory){if(faultCategory==null)return "";return switch(faultCategory){case ELECTRICITY->"电路电器";case WATER_PIPE->"水管龙头";case DOOR_WINDOW->"门窗锁具";case NETWORK->"网络故障";case FURNITURE->"家具设施";case PUBLIC_AREA->"公共区域";case OTHER->"其他问题";};}
public static String repairRequestStatus(RepairRequestStatus repairRequestStatus){if(repairRequestStatus==null)return "";return switch(repairRequestStatus){case SUBMITTED->"已提交";case ASSIGNED->"已受理";case IN_PROGRESS->"处理中";case PENDING_CONFIRMATION->"待确认完成";case REWORK_IN_PROGRESS->"返修中";case COMPLETED->"已处理";case REJECTED->"已驳回";case CANCELLED->"已取消";};}
public static String workOrderPriority(WorkOrderPriority workOrderPriority){if(workOrderPriority==null)return "";return switch(workOrderPriority){case LOW->"低";case NORMAL->"普通";case HIGH->"高";case URGENT->"紧急";};}
public static String workOrderStatus(WorkOrderStatus workOrderStatus){if(workOrderStatus==null)return "";return switch(workOrderStatus){case ASSIGNED->"已派单";case ACCEPTED->"已受理";case IN_PROGRESS->"处理中";case WAITING_PARTS->"待配件";case WAITING_CONFIRMATION->"待学生确认";case COMPLETED->"已处理";case REJECTED->"已驳回";case CANCELLED->"已取消";};}
public static String timeoutLabel(TimeoutLevel timeoutLevel,String timeoutText){if(timeoutLevel==null||timeoutLevel==TimeoutLevel.NORMAL||timeoutText==null||timeoutText.isBlank())return "";return timeoutText;}
}