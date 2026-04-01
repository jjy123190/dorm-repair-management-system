package com.scau.dormrepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.view.AuditLogView;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuditLogServiceImplTest extends UserAccountIntegrationSupport {

    @Test
    void shouldFilterAuditLogsByActionOperatorAndTimeRange() {
        Long adminId = userAccountService.createInternalAccount(
                uniqueUsername("audit_admin"),
                "admin123",
                "admin123",
                "audit_admin",
                "13855552091",
                UserRole.ADMIN
        );
        Long workerId = userAccountService.createInternalAccount(
                uniqueUsername("audit_worker"),
                "worker123",
                "worker123",
                "audit_worker",
                "13855552092",
                UserRole.WORKER
        );
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("audit_student"),
                "student123",
                "student123",
                "audit_student",
                "13855552093"
        );

        Long requestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "audit_student",
                "13855552093",
                null,
                "Taishan",
                "10-BLD",
                "601",
                FaultCategory.PUBLIC_AREA,
                "Need an audit record.",
                List.of()
        ));
        workOrderService.assign(new AssignWorkOrderCommand(
                requestId,
                adminId,
                workerId,
                WorkOrderPriority.NORMAL,
                "Generate dispatch audit."
        ));

        List<AuditLogView> byAction = auditLogService.listFiltered("WORK_ORDER_ASSIGN", null, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusMinutes(1), 20);
        List<AuditLogView> byOperator = auditLogService.listFiltered(null, adminId, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusMinutes(1), 20);

        assertTrue(byAction.stream().anyMatch(item -> "WORK_ORDER_ASSIGN".equals(item.getActionType())));
        assertTrue(byOperator.stream().anyMatch(item -> adminId.equals(item.getOperatorId())));
        assertEquals(byAction.get(0).getOperatorId(), byOperator.get(0).getOperatorId());
    }
}