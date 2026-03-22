package com.scau.dormrepair.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 宿舍房间实体。
 */
@Entity
@Table(name = "dorm_rooms")
public class DormRoom extends BaseTimeEntity {

    /**
     * 主键 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 校区名称，例如启林、泰山区。
     */
    @Column(nullable = false, length = 64)
    private String campusName;

    /**
     * 楼栋号，例如 1 栋、A 栋。
     */
    @Column(nullable = false, length = 32)
    private String buildingNo;

    /**
     * 房间号，例如 203、A305。
     */
    @Column(nullable = false, length = 32)
    private String roomNo;

    /**
     * 楼层号。
     */
    @Column(nullable = false)
    private Integer floorNo;

    /**
     * 房间床位数。
     */
    @Column(nullable = false)
    private Integer bedCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public String getBuildingNo() {
        return buildingNo;
    }

    public void setBuildingNo(String buildingNo) {
        this.buildingNo = buildingNo;
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
}
