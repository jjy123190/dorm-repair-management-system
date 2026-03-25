package com.scau.dormrepair.domain.command;

import com.scau.dormrepair.domain.enums.FaultCategory;
import java.util.List;

/**
 * 学生提交报修时传入的命令对象。
 */
public record CreateRepairRequestCommand(
        Long studentId,
        String studentName,
        String contactPhone,
        Long dormRoomId,
        String dormAreaSnapshot,
        String buildingNoSnapshot,
        String roomNoSnapshot,
        FaultCategory faultCategory,
        String description,
        List<String> imageUrls
) {
}
