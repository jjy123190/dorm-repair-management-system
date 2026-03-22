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
     * @param command 宿舍房间数据
     * @return 新增后的宿舍房间信息
     */
    DormRoomResponse create(SaveDormRoomCommand command);

    /**
     * 更新宿舍房间。
     * @param id 宿舍房间 ID
     * @param command 更新后的宿舍房间数据
     * @return 更新后的宿舍房间信息
     */
    DormRoomResponse update(Long id, SaveDormRoomCommand command);

    /**
     * 查询宿舍房间详情。
     * @param id 宿舍房间 ID
     * @return 宿舍房间详情
     */
    DormRoomResponse getById(Long id);

    /**
     * 分页查询宿舍房间。
     * @param page 页码
     * @param size 每页条数
     * @return 宿舍房间分页结果
     */
    Page<DormRoomResponse> page(int page, int size);

    /**
     * 删除宿舍房间。
     * @param id 宿舍房间 ID
     */
    void delete(Long id);
}
