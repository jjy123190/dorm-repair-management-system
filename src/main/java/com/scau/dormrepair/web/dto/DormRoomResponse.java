package com.scau.dormrepair.web.dto;

public record DormRoomResponse(
        Long id,
        String campusName,
        String buildingNo,
        String roomNo,
        Integer floorNo,
        Integer bedCount
) {
}
