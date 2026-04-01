package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.command.SaveDormBuildingCommand;
import com.scau.dormrepair.domain.command.SaveDormRoomCommand;
import com.scau.dormrepair.domain.entity.DormBuilding;
import com.scau.dormrepair.domain.entity.DormRoom;
import java.util.List;

/**
 * 宿舍目录服务，负责楼栋和房间基础数据维护。
 */
public interface DormCatalogService {

    List<String> listDormAreas();

    List<DormBuilding> listBuildingsByArea(String dormArea);

    List<DormBuilding> listAllBuildings();

    Long saveBuilding(SaveDormBuildingCommand command);

    void deleteBuilding(Long buildingId);

    List<DormRoom> listRoomsByBuilding(Long buildingId);

    List<DormRoom> listActiveRoomsByBuilding(Long buildingId);

    Long saveRoom(SaveDormRoomCommand command);

    void deleteRoom(Long roomId);
}