package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.DormRoom;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 宿舍房间目录 Mapper。
 */
public interface DormRoomMapper {

    List<DormRoom> selectByBuildingId(@Param("buildingId") Long buildingId);

    List<DormRoom> selectActiveByBuildingId(@Param("buildingId") Long buildingId);

    DormRoom selectById(@Param("id") Long id);

    DormRoom selectByBuildingAndRoomNo(@Param("buildingId") Long buildingId, @Param("roomNo") String roomNo);

    int insert(DormRoom dormRoom);

    int update(DormRoom dormRoom);

    int countBoundRepairRequests(@Param("roomId") Long roomId);

    int deleteById(@Param("id") Long id);
}