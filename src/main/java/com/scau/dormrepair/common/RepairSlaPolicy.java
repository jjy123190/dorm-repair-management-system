package com.scau.dormrepair.common;

import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.TimeoutLevel;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Shared lightweight SLA policy for demo-stage timeout warnings.
 */
public final class RepairSlaPolicy {

    private static final Duration PENDING_ASSIGN_WARNING = Duration.ofHours(6);
    private static final Duration PENDING_ASSIGN_OVERDUE = Duration.ofHours(12);
    private static final Duration ASSIGNED_WARNING = Duration.ofHours(2);
    private static final Duration ASSIGNED_OVERDUE = Duration.ofHours(6);
    private static final Duration PROCESSING_WARNING = Duration.ofHours(24);
    private static final Duration PROCESSING_OVERDUE = Duration.ofHours(48);

    private RepairSlaPolicy() {
    }

    public static TimeoutLevel resolveRequestLevel(
            RepairRequestStatus status,
            LocalDateTime submittedAt,
            LocalDateTime assignedAt,
            LocalDateTime acceptedAt
    ) {
        if (status == null) {
            return TimeoutLevel.NORMAL;
        }
        return switch (status) {
            case SUBMITTED -> resolveByAge(submittedAt, PENDING_ASSIGN_WARNING, PENDING_ASSIGN_OVERDUE);
            case ASSIGNED -> resolveByAge(assignedAt == null ? submittedAt : assignedAt, ASSIGNED_WARNING, ASSIGNED_OVERDUE);
            case IN_PROGRESS, REWORK_IN_PROGRESS -> resolveByAge(resolveProcessingStart(acceptedAt, assignedAt, submittedAt), PROCESSING_WARNING, PROCESSING_OVERDUE);
            case PENDING_CONFIRMATION, COMPLETED, REJECTED, CANCELLED -> TimeoutLevel.NORMAL;
        };
    }

    public static String resolveRequestLabel(
            RepairRequestStatus status,
            TimeoutLevel level
    ) {
        if (status == null || level == null || level == TimeoutLevel.NORMAL) {
            return null;
        }
        return switch (status) {
            case SUBMITTED -> level == TimeoutLevel.OVERDUE ? "待派单超时" : "待派单将超时";
            case ASSIGNED -> level == TimeoutLevel.OVERDUE ? "待接单超时" : "待接单将超时";
            case IN_PROGRESS, REWORK_IN_PROGRESS -> level == TimeoutLevel.OVERDUE ? "处理中超时" : "处理中将超时";
            default -> null;
        };
    }

    public static TimeoutLevel resolveWorkOrderLevel(
            WorkOrderStatus status,
            LocalDateTime assignedAt,
            LocalDateTime acceptedAt
    ) {
        if (status == null) {
            return TimeoutLevel.NORMAL;
        }
        return switch (status) {
            case ASSIGNED -> resolveByAge(assignedAt, ASSIGNED_WARNING, ASSIGNED_OVERDUE);
            case ACCEPTED, IN_PROGRESS, WAITING_PARTS -> resolveByAge(resolveProcessingStart(acceptedAt, assignedAt, assignedAt), PROCESSING_WARNING, PROCESSING_OVERDUE);
            case WAITING_CONFIRMATION, COMPLETED, REJECTED, CANCELLED -> TimeoutLevel.NORMAL;
        };
    }

    public static String resolveWorkOrderLabel(WorkOrderStatus status, TimeoutLevel level) {
        if (status == null || level == null || level == TimeoutLevel.NORMAL) {
            return null;
        }
        return switch (status) {
            case ASSIGNED -> level == TimeoutLevel.OVERDUE ? "待接单超时" : "待接单将超时";
            case ACCEPTED, IN_PROGRESS, WAITING_PARTS -> level == TimeoutLevel.OVERDUE ? "处理中超时" : "处理中将超时";
            default -> null;
        };
    }

    private static TimeoutLevel resolveByAge(LocalDateTime startAt, Duration warning, Duration overdue) {
        if (startAt == null) {
            return TimeoutLevel.NORMAL;
        }
        Duration elapsed = Duration.between(startAt, LocalDateTime.now());
        if (elapsed.compareTo(overdue) >= 0) {
            return TimeoutLevel.OVERDUE;
        }
        if (elapsed.compareTo(warning) >= 0) {
            return TimeoutLevel.WARNING;
        }
        return TimeoutLevel.NORMAL;
    }

    private static LocalDateTime resolveProcessingStart(
            LocalDateTime acceptedAt,
            LocalDateTime assignedAt,
            LocalDateTime submittedAt
    ) {
        if (acceptedAt != null) {
            return acceptedAt;
        }
        if (assignedAt != null) {
            return assignedAt;
        }
        return submittedAt;
    }
}