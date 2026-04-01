package com.scau.dormrepair.common;

import com.scau.dormrepair.domain.entity.AuditLog;
import com.scau.dormrepair.mapper.AuditLogMapper;

/**
 * Shared helper for writing compact audit logs.
 */
public final class AuditLogSupport {

    private AuditLogSupport() {
    }

    public static void append(
            AuditLogMapper mapper,
            Long operatorId,
            String actionType,
            String targetType,
            String targetId,
            String targetLabel,
            String oldValue,
            String newValue
    ) {
        if (mapper == null || actionType == null || actionType.isBlank() || targetType == null || targetType.isBlank()) {
            return;
        }
        AuditLog auditLog = new AuditLog();
        auditLog.setOperatorId(operatorId);
        auditLog.setActionType(actionType);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(trimToNull(targetId));
        auditLog.setTargetLabel(trimToNull(targetLabel));
        auditLog.setOldValue(trimToNull(oldValue));
        auditLog.setNewValue(trimToNull(newValue));
        mapper.insert(auditLog);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}