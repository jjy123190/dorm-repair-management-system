package com.scau.dormrepair.web.dto;

/**
 * 宿舍房间响应体。
 */
public record DormRoomResponse(
        Long id,
        String campusName,
        String buildingNo,
        String roomNo,
        Integer floorNo,
        Integer bedCount
) {
}
