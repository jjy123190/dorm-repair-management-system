package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.domain.view.AuditLogView;
import com.scau.dormrepair.mapper.AuditLogMapper;
import com.scau.dormrepair.service.AuditLogService;
import java.time.LocalDateTime;
import java.util.List;

public class AuditLogServiceImpl implements AuditLogService {

    private final MyBatisExecutor myBatisExecutor;

    public AuditLogServiceImpl(MyBatisExecutor myBatisExecutor) {
        this.myBatisExecutor = myBatisExecutor;
    }

    @Override
    public List<AuditLogView> listLatest(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return myBatisExecutor.executeRead(session -> session.getMapper(AuditLogMapper.class).selectLatest(safeLimit));
    }

    @Override
    public List<AuditLogView> listFiltered(String actionType, Long operatorId, LocalDateTime createdFrom, LocalDateTime createdTo, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        String normalizedActionType = actionType == null || actionType.isBlank() ? null : actionType.trim();
        return myBatisExecutor.executeRead(session -> session.getMapper(AuditLogMapper.class)
                .selectFiltered(normalizedActionType, operatorId, createdFrom, createdTo, safeLimit));
    }
}