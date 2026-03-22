package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.RepairRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 工单处理记录数据访问接口。
 */
public interface RepairRecordRepository extends JpaRepository<RepairRecord, Long> {
}
