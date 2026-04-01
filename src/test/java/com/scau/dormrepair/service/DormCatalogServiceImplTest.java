package com.scau.dormrepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.command.SaveDormBuildingCommand;
import com.scau.dormrepair.domain.command.SaveDormRoomCommand;
import com.scau.dormrepair.domain.entity.DormBuilding;
import com.scau.dormrepair.domain.entity.DormRoom;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.Test;

class DormCatalogServiceImplTest extends UserAccountIntegrationSupport {

    @Test
    void shouldCreateAndUpdateBuildingAndRoom() {
        String campusName = uniqueUsername("area");
        String buildingNo = uniqueUsername("bld");

        Long buildingId = dormCatalogService.saveBuilding(new SaveDormBuildingCommand(
                null,
                campusName,
                buildingNo,
                campusName + " " + buildingNo
        ));
        List<DormBuilding> buildings = dormCatalogService.listAllBuildings();
        DormBuilding createdBuilding = buildings.stream()
                .filter(item -> buildingId.equals(item.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(campusName, createdBuilding.getCampusName());
        assertEquals(buildingNo, createdBuilding.getBuildingNo());

        Long roomId = dormCatalogService.saveRoom(new SaveDormRoomCommand(
                null,
                buildingId,
                uniqueRoomNo("room"),
                4,
                6,
                "ACTIVE"
        ));
        List<DormRoom> rooms = dormCatalogService.listRoomsByBuilding(buildingId);
        DormRoom createdRoom = rooms.stream()
                .filter(item -> roomId.equals(item.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(Integer.valueOf(6), createdRoom.getBedCount());
        assertEquals("ACTIVE", createdRoom.getRoomStatus());

        Long updatedRoomId = dormCatalogService.saveRoom(new SaveDormRoomCommand(
                roomId,
                buildingId,
                createdRoom.getRoomNo(),
                4,
                8,
                "INACTIVE"
        ));
        assertEquals(roomId, updatedRoomId);
        DormRoom updatedRoom = dormCatalogService.listRoomsByBuilding(buildingId).stream()
                .filter(item -> roomId.equals(item.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(Integer.valueOf(8), updatedRoom.getBedCount());
        assertEquals("INACTIVE", updatedRoom.getRoomStatus());
    }

    @Test
    void shouldOnlyExposeActiveRoomsForStudentSelection() {
        String campusName = uniqueUsername("area");
        String buildingNo = uniqueUsername("bld");
        Long buildingId = dormCatalogService.saveBuilding(new SaveDormBuildingCommand(
                null,
                campusName,
                buildingNo,
                campusName + " " + buildingNo
        ));
        Long activeRoomId = dormCatalogService.saveRoom(new SaveDormRoomCommand(
                null,
                buildingId,
                uniqueRoomNo("rooma"),
                3,
                4,
                "ACTIVE"
        ));
        dormCatalogService.saveRoom(new SaveDormRoomCommand(
                null,
                buildingId,
                uniqueRoomNo("roomi"),
                3,
                4,
                "INACTIVE"
        ));

        List<DormRoom> reportableRooms = dormCatalogService.listActiveRoomsByBuilding(buildingId);
        assertEquals(1, reportableRooms.size());
        assertEquals(activeRoomId, reportableRooms.get(0).getId());
        assertEquals("ACTIVE", reportableRooms.get(0).getRoomStatus());
    }

    @Test
    void shouldRejectDeletingBuildingWhenRoomsStillExist() {
        Long buildingId = dormCatalogService.saveBuilding(new SaveDormBuildingCommand(
                null,
                uniqueUsername("area"),
                uniqueUsername("bld"),
                "Test Building"
        ));
        dormCatalogService.saveRoom(new SaveDormRoomCommand(null, buildingId, uniqueRoomNo("room"), 3, 4, "ACTIVE"));

        BusinessException exception = assertThrows(BusinessException.class, () -> dormCatalogService.deleteBuilding(buildingId));
        assertTrue(exception.getMessage().contains("\u5bbf\u820d\u623f\u95f4"));
    }

    @Test
    void shouldRejectDeletingRoomWhenRepairRequestAlreadyUsesIt() {
        String campusName = uniqueUsername("area");
        String buildingNo = uniqueUsername("bld");
        Long buildingId = dormCatalogService.saveBuilding(new SaveDormBuildingCommand(null, campusName, buildingNo, campusName + " " + buildingNo));
        String roomNo = uniqueRoomNo("room");
        Long roomId = dormCatalogService.saveRoom(new SaveDormRoomCommand(null, buildingId, roomNo, 5, 6, "ACTIVE"));
        Long studentId = userAccountService.registerStudent(uniqueUsername("student"), "student123", "student123", "Dorm Tester", "13855553101");

        repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "Dorm Tester",
                "13855553101",
                roomId,
                campusName,
                buildingNo,
                roomNo,
                FaultCategory.ELECTRICITY,
                "Test repair request for bound room delete validation.",
                List.of()
        ));

        BusinessException exception = assertThrows(BusinessException.class, () -> dormCatalogService.deleteRoom(roomId));
        assertTrue(exception.getMessage().contains("\u5386\u53f2\u62a5\u4fee\u8bb0\u5f55"));
    }

    private String uniqueRoomNo(String suffix) {
        String raw = uniqueUsername(suffix).replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return raw.length() > 12 ? raw.substring(0, 12) : raw;
    }
}