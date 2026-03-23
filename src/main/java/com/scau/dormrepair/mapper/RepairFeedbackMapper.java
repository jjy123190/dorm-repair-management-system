package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.RepairFeedback;

/**
 * 评价反馈 Mapper。
 */
public interface RepairFeedbackMapper {

    RepairFeedback selectByRepairRequestId(Long repairRequestId);

    int insert(RepairFeedback repairFeedback);
}
