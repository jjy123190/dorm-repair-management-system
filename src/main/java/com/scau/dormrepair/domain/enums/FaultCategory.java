package com.scau.dormrepair.domain.enums;

/**
 * 故障类型枚举。
 * 前后端统一传英文码，界面展示中文由前端自己映射。
 */
public enum FaultCategory {
    ELECTRICITY,
    WATER_PIPE,
    DOOR_WINDOW,
    NETWORK,
    FURNITURE,
    PUBLIC_AREA,
    OTHER
}
