package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.view.AuditLogView;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {

    List<AuditLogView> listLatest(int limit);

    List<AuditLogView> listFiltered(String actionType, Long operatorId, LocalDateTime createdFrom, LocalDateTime createdTo, int limit);
}