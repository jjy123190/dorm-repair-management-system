package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.AppSession;
import com.scau.dormrepair.common.AuditLogSupport;
import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.domain.command.SaveDormBuildingCommand;
import com.scau.dormrepair.domain.command.SaveDormRoomCommand;
import com.scau.dormrepair.domain.entity.DormBuilding;
import com.scau.dormrepair.domain.entity.DormRoom;
import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import com.scau.dormrepair.mapper.AuditLogMapper;
import com.scau.dormrepair.mapper.DormBuildingMapper;
import com.scau.dormrepair.mapper.DormRoomMapper;
import com.scau.dormrepair.service.DormCatalogService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 宿舍区、楼栋、房间基础资料服务实现。
 */
public class DormCatalogServiceImpl implements DormCatalogService {

    private static final int MAX_AREA_LENGTH = 64;
    private static final int MAX_BUILDING_NO_LENGTH = 32;
    private static final int MAX_BUILDING_NAME_LENGTH = 64;
    private static final int MAX_ROOM_NO_LENGTH = 32;
    private static final List<String> STABLE_DORM_AREAS = List.of(
            "\u6cf0\u5c71\u533a",
            "\u534e\u5c71\u533a",
            "\u542f\u6797\u533a",
            "\u96c1\u5c71\u533a",
            "\u71d5\u5c71\u533a"
    );
    private static final String ROOM_STATUS_ACTIVE = "ACTIVE";
    private static final String ROOM_STATUS_INACTIVE = "INACTIVE";

    private final MyBatisExecutor myBatisExecutor;
    private final AppSession appSession;

    public DormCatalogServiceImpl(MyBatisExecutor myBatisExecutor) {
        this(myBatisExecutor, null);
    }

    public DormCatalogServiceImpl(MyBatisExecutor myBatisExecutor, AppSession appSession) {
        this.myBatisExecutor = myBatisExecutor;
        this.appSession = appSession;
    }

    @Override
    public List<String> listDormAreas() {
        List<String> areas = myBatisExecutor.executeRead(
                session -> session.getMapper(DormBuildingMapper.class).selectDistinctAreas()
        );
        Set<String> normalizedAreas = new LinkedHashSet<>(STABLE_DORM_AREAS);
        for (String area : areas) {
            String normalized = trimToNull(area);
            if (normalized != null) {
                normalizedAreas.add(normalized);
            }
        }
        return normalizedAreas.stream().toList();
    }

    @Override
    public List<DormBuilding> listBuildingsByArea(String dormArea) {
        String normalizedArea = trimToNull(dormArea);
        if (normalizedArea == null) {
            return List.of();
        }
        return myBatisExecutor.executeRead(
                session -> session.getMapper(DormBuildingMapper.class).selectByArea(normalizedArea)
        );
    }

    @Override
    public List<DormBuilding> listAllBuildings() {
        return myBatisExecutor.executeRead(
                session -> session.getMapper(DormBuildingMapper.class).selectAll()
        );
    }

    @Override
    public Long saveBuilding(SaveDormBuildingCommand command) {
        if (command == null) {
            throw new BusinessException("宿舍楼栋信息不能为空。");
        }

        String campusName = normalizeRequired(command.campusName(), MAX_AREA_LENGTH, "宿舍区不能为空。");
        String buildingNo = normalizeRequired(command.buildingNo(), MAX_BUILDING_NO_LENGTH, "楼栋编号不能为空。");
        String buildingName = normalizeOptional(command.buildingName(), MAX_BUILDING_NAME_LENGTH);

        return myBatisExecutor.executeWrite(session -> {
            DormBuildingMapper mapper = session.getMapper(DormBuildingMapper.class);
            DormBuilding duplicated = mapper.selectByCampusAndBuildingNo(campusName, buildingNo);
            if (duplicated != null && !duplicated.getId().equals(command.id())) {
                throw new BusinessException("同一宿舍区下已存在相同楼栋编号，请勿重复创建。");
            }

            DormBuilding dormBuilding = new DormBuilding();
            dormBuilding.setCampusName(campusName);
            dormBuilding.setBuildingNo(buildingNo);
            dormBuilding.setBuildingName(buildingName == null ? campusName + " " + buildingNo : buildingName);

            if (command.id() == null) {
                mapper.insert(dormBuilding);
                return dormBuilding.getId();
            }

            DormBuilding existing = mapper.selectById(command.id());
            if (existing == null) {
                throw new ResourceNotFoundException("未找到要更新的宿舍楼栋，ID=" + command.id());
            }
            dormBuilding.setId(command.id());
            mapper.update(dormBuilding);
            return dormBuilding.getId();
        });
    }

    @Override
    public void deleteBuilding(Long buildingId) {
        if (buildingId == null) {
            throw new BusinessException("未选择宿舍楼栋，无法删除。");
        }
        myBatisExecutor.executeWrite(session -> {
            DormBuildingMapper buildingMapper = session.getMapper(DormBuildingMapper.class);
            DormBuilding existing = buildingMapper.selectById(buildingId);
            if (existing == null) {
                throw new ResourceNotFoundException("未找到要删除的宿舍楼栋，ID=" + buildingId);
            }
            if (buildingMapper.countRooms(buildingId) > 0) {
                throw new BusinessException("当前楼栋下仍有宿舍房间，请先清理或迁移房间后再删除楼栋。");
            }
            buildingMapper.deleteById(buildingId);
            return null;
        });
    }

    @Override
    public List<DormRoom> listRoomsByBuilding(Long buildingId) {
        if (buildingId == null) {
            return List.of();
        }
        return myBatisExecutor.executeRead(
                session -> session.getMapper(DormRoomMapper.class).selectByBuildingId(buildingId)
        );
    }

    @Override
    public List<DormRoom> listActiveRoomsByBuilding(Long buildingId) {
        if (buildingId == null) {
            return List.of();
        }
        return myBatisExecutor.executeRead(
                session -> session.getMapper(DormRoomMapper.class).selectActiveByBuildingId(buildingId)
        );
    }

    @Override
    public Long saveRoom(SaveDormRoomCommand command) {
        if (command == null || command.buildingId() == null) {
            throw new BusinessException("请先选择宿舍楼栋，再维护房间信息。");
        }

        String roomNo = normalizeRequired(command.roomNo(), MAX_ROOM_NO_LENGTH, "房间号不能为空。");
        Integer floorNo = normalizePositive(command.floorNo(), 1, 99, "楼层必须在 1 到 99 之间。");
        Integer bedCount = normalizePositive(command.bedCount(), 1, 16, "床位数必须在 1 到 16 之间。");
        String roomStatus = normalizeRoomStatus(command.roomStatus());

        return myBatisExecutor.executeWrite(session -> {
            DormBuildingMapper buildingMapper = session.getMapper(DormBuildingMapper.class);
            DormRoomMapper roomMapper = session.getMapper(DormRoomMapper.class);
            if (buildingMapper.selectById(command.buildingId()) == null) {
                throw new ResourceNotFoundException("未找到对应的宿舍楼栋，ID=" + command.buildingId());
            }

            DormRoom duplicated = roomMapper.selectByBuildingAndRoomNo(command.buildingId(), roomNo);
            if (duplicated != null && !duplicated.getId().equals(command.id())) {
                throw new BusinessException("当前楼栋下已存在相同房间号，请勿重复创建。");
            }

            DormRoom dormRoom = new DormRoom();
            dormRoom.setBuildingId(command.buildingId());
            dormRoom.setRoomNo(roomNo);
            dormRoom.setFloorNo(floorNo);
            dormRoom.setBedCount(bedCount);
            dormRoom.setRoomStatus(roomStatus);

            if (command.id() == null) {
                roomMapper.insert(dormRoom);
                return dormRoom.getId();
            }

            DormRoom existing = roomMapper.selectById(command.id());
            if (existing == null) {
                throw new ResourceNotFoundException("未找到要更新的宿舍房间，ID=" + command.id());
            }
            dormRoom.setId(command.id());
            roomMapper.update(dormRoom);
            return dormRoom.getId();
        });
    }

    @Override
    public void deleteRoom(Long roomId) {
        if (roomId == null) {
            throw new BusinessException("未选择宿舍房间，无法删除。");
        }
        myBatisExecutor.executeWrite(session -> {
            DormRoomMapper roomMapper = session.getMapper(DormRoomMapper.class);
            DormRoom existing = roomMapper.selectById(roomId);
            if (existing == null) {
                throw new ResourceNotFoundException("未找到要删除的宿舍房间，ID=" + roomId);
            }
            if (roomMapper.countBoundRepairRequests(roomId) > 0) {
                throw new BusinessException("该房间已关联历史报修记录，不能直接删除，请改为停用。");
            }
            roomMapper.deleteById(roomId);
            return null;
        });
    }

    private Integer normalizePositive(Integer value, int min, int max, String message) {
        if (value == null || value < min || value > max) {
            throw new BusinessException(message);
        }
        return value;
    }

    private String normalizeRequired(String value, int maxLength, String emptyMessage) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(emptyMessage);
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException("输入内容过长，请缩短后再保存。");
        }
        return normalized;
    }

    private String normalizeOptional(String value, int maxLength) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException("输入内容过长，请缩短后再保存。");
        }
        return normalized;
    }

    private String normalizeRoomStatus(String rawStatus) {
        String normalized = trimToNull(rawStatus);
        if (normalized == null) {
            return ROOM_STATUS_ACTIVE;
        }
        String upperCase = normalized.toUpperCase(Locale.ROOT);
        if (!ROOM_STATUS_ACTIVE.equals(upperCase) && !ROOM_STATUS_INACTIVE.equals(upperCase)) {
            throw new BusinessException("房间状态只支持 ACTIVE 或 INACTIVE。");
        }
        return upperCase;
    }

    private Long currentOperatorId() {
        return appSession == null ? null : appSession.getCurrentAccountId();
    }

    private static String summarizeBuilding(DormBuilding building) {
        if (building == null) {
            return null;
        }
        return "campus=" + building.getCampusName() + ", buildingNo=" + building.getBuildingNo() + ", buildingName=" + building.getBuildingName();
    }

    private static String summarizeRoom(DormRoom room) {
        if (room == null) {
            return null;
        }
        return "buildingId=" + room.getBuildingId() + ", roomNo=" + room.getRoomNo() + ", floor=" + room.getFloorNo() + ", beds=" + room.getBedCount() + ", status=" + room.getRoomStatus();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}