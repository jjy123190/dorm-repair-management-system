package com.scau.dormrepair.repository;

import com.scau.dormrepair.domain.entity.RepairRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairRecordRepository extends JpaRepository<RepairRecord, Long> {
}
