package com.scau.dormrepair.service;

import com.scau.dormrepair.web.dto.DormRoomResponse;
import com.scau.dormrepair.web.dto.SaveDormRoomCommand;
import org.springframework.data.domain.Page;

/**
 * 宿舍房间服务接口。
 */
public interface DormRoomService {

    /**
     * 新增宿舍房间。
     */
    DormRoomResponse create(SaveDormRoomCommand command);

    /**
     * 更新宿舍房间。
     */
    DormRoomResponse update(Long id, SaveDormRoomCommand command);

    /**
     * 查询宿舍房间详情。
     */
    DormRoomResponse getById(Long id);

    /**
     * 分页查询宿舍房间。
     */
    Page<DormRoomResponse> page(int page, int size);

    /**
     * 删除宿舍房间。
     */
    void delete(Long id);
}
