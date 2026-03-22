package com.scau.dormrepair.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveDormRoomCommand(
        @NotBlank(message = "校区名称不能为空")
        @Size(max = 64, message = "校区名称长度不能超过64")
        String campusName,
        @NotBlank(message = "楼栋号不能为空")
        @Size(max = 32, message = "楼栋号长度不能超过32")
        String buildingNo,
        @NotBlank(message = "房间号不能为空")
        @Size(max = 32, message = "房间号长度不能超过32")
        String roomNo,
        @NotNull(message = "楼层不能为空")
        @Min(value = 1, message = "楼层最小为1")
        Integer floorNo,
        @NotNull(message = "床位数不能为空")
        @Min(value = 1, message = "床位数最小为1")
        Integer bedCount
) {
}
