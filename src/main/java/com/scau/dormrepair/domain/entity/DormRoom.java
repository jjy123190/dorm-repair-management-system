package com.scau.dormrepair.domain.entity;

/**
 * 宿舍房间实体。
 * 桌面端后续主要通过房间 ID 绑定报修单，因此这里保留 buildingId 作为外键。
 */
public class DormRoom extends BaseTimeEntity {

    private Long id;
    private Long buildingId;
    private String roomNo;
    private Integer floorNo;
    private Integer bedCount;
    private String roomStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public Integer getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(Integer floorNo) {
        this.floorNo = floorNo;
    }

    public Integer getBedCount() {
        return bedCount;
    }

    public void setBedCount(Integer bedCount) {
        this.bedCount = bedCount;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }
}
