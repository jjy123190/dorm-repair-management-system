package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.AuditLog;
import com.scau.dormrepair.domain.view.AuditLogView;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuditLogMapper {

    int insert(AuditLog auditLog);

    List<AuditLogView> selectLatest(@Param("limit") int limit);

    List<AuditLogView> selectFiltered(
            @Param("actionType") String actionType,
            @Param("operatorId") Long operatorId,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            @Param("limit") int limit
    );
}