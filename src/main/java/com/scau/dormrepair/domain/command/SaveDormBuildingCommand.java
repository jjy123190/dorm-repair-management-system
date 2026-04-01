package com.scau.dormrepair.domain.command;

/**
 * 保存宿舍楼栋目录时使用的命令对象。
 */
public record SaveDormBuildingCommand(
        Long id,
        String campusName,
        String buildingNo,
        String buildingName
) {
}