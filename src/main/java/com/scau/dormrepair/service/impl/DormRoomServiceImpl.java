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

@Service
public class DormRoomServiceImpl implements DormRoomService {

    private final DormRoomRepository dormRoomRepository;

    public DormRoomServiceImpl(DormRoomRepository dormRoomRepository) {
        this.dormRoomRepository = dormRoomRepository;
    }

    @Override
    @Transactional
    public DormRoomResponse create(SaveDormRoomCommand command) {
        DormRoom dormRoom = new DormRoom();
        applyCommand(dormRoom, command);
        return toResponse(dormRoomRepository.save(dormRoom));
    }

    @Override
    @Transactional
    public DormRoomResponse update(Long id, SaveDormRoomCommand command) {
        DormRoom dormRoom = dormRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到宿舍信息，ID=" + id));
        applyCommand(dormRoom, command);
        return toResponse(dormRoomRepository.save(dormRoom));
    }

    @Override
    @Transactional(readOnly = true)
    public DormRoomResponse getById(Long id) {
        DormRoom dormRoom = dormRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到宿舍信息，ID=" + id));
        return toResponse(dormRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DormRoomResponse> page(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Order.asc("campusName"), Sort.Order.asc("buildingNo"), Sort.Order.asc("roomNo"))
        );
        return dormRoomRepository.findAll(pageRequest).map(this::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!dormRoomRepository.existsById(id)) {
            throw new ResourceNotFoundException("未找到宿舍信息，ID=" + id);
        }
        dormRoomRepository.deleteById(id);
    }

    private void applyCommand(DormRoom dormRoom, SaveDormRoomCommand command) {
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
