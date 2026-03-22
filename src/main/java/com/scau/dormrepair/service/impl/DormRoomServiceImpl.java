package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.domain.entity.DormRoom;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import com.scau.dormrepair.repository.DormRoomRepository;
import com.scau.dormrepair.service.DormRoomService;
import com.scau.dormrepair.web.dto.DormRoomResponse;
import com.scau.dormrepair.web.dto.SaveDormRoomCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宿舍房间服务实现。
 */
@Service
public class DormRoomServiceImpl implements DormRoomService {

    /**
     * 宿舍房间数据访问对象。
     */
    private final DormRoomRepository dormRoomRepository;

    public DormRoomServiceImpl(DormRoomRepository dormRoomRepository) {
        this.dormRoomRepository = dormRoomRepository;
    }

    /**
     * 新增宿舍房间。
     * @param command 宿舍房间数据
     * @return 新增后的宿舍房间信息
     */
    @Override
    @Transactional
    public DormRoomResponse create(SaveDormRoomCommand command) {
        // 1. 创建新的宿舍房间实体对象。
        DormRoom dormRoom = new DormRoom();
        // 2. 把前端参数写入实体。
        applyCommand(dormRoom, command);
        // 3. 保存到数据库并返回结果。
        return toResponse(dormRoomRepository.save(dormRoom));
    }

    /**
     * 更新宿舍房间。
     * @param id 宿舍房间 ID
     * @param command 更新后的宿舍房间数据
     * @return 更新后的宿舍房间信息
     */
    @Override
    @Transactional
    public DormRoomResponse update(Long id, SaveDormRoomCommand command) {
        // 1. 先检查数据库里是否存在这条记录。
        DormRoom dormRoom = dormRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到宿舍信息，ID=" + id));
        // 2. 用最新参数覆盖旧数据。
        applyCommand(dormRoom, command);
        // 3. 保存更新结果。
        return toResponse(dormRoomRepository.save(dormRoom));
    }

    /**
     * 查询宿舍房间详情。
     * @param id 宿舍房间 ID
     * @return 宿舍房间详情
     */
    @Override
    @Transactional(readOnly = true)
    public DormRoomResponse getById(Long id) {
        DormRoom dormRoom = dormRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到宿舍信息，ID=" + id));
        return toResponse(dormRoom);
    }

    /**
     * 分页查询宿舍房间。
     * @param page 页码
     * @param size 每页条数
     * @return 宿舍房间分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DormRoomResponse> page(int page, int size) {
        // 把前端的页码转换为 Spring Data 使用的 0 基页码。
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Order.asc("campusName"), Sort.Order.asc("buildingNo"), Sort.Order.asc("roomNo"))
        );
        return dormRoomRepository.findAll(pageRequest).map(this::toResponse);
    }

    /**
     * 删除宿舍房间。
     * @param id 宿舍房间 ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // 删除前先判断是否存在，避免 deleteById 静默失败。
        if (!dormRoomRepository.existsById(id)) {
            throw new ResourceNotFoundException("未找到宿舍信息，ID=" + id);
        }
        dormRoomRepository.deleteById(id);
    }

    private void applyCommand(DormRoom dormRoom, SaveDormRoomCommand command) {
        // 把前端传入的参数统一写回实体，避免 create / update 逻辑各写一份。
        dormRoom.setCampusName(command.campusName());
        dormRoom.setBuildingNo(command.buildingNo());
        dormRoom.setRoomNo(command.roomNo());
        dormRoom.setFloorNo(command.floorNo());
        dormRoom.setBedCount(command.bedCount());
    }

    private DormRoomResponse toResponse(DormRoom dormRoom) {
        return new DormRoomResponse(
                dormRoom.getId(),
                dormRoom.getCampusName(),
                dormRoom.getBuildingNo(),
                dormRoom.getRoomNo(),
                dormRoom.getFloorNo(),
                dormRoom.getBedCount()
        );
    }
}
