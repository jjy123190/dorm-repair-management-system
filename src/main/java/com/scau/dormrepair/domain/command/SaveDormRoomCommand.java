package com.scau.dormrepair.domain.command;

/**
 * 保存宿舍房间目录时使用的命令对象。
 */
public record SaveDormRoomCommand(
        Long id,
        Long buildingId,
        String roomNo,
        Integer floorNo,
        Integer bedCount,
        String roomStatus
) {
}