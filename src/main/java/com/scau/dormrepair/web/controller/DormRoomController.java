package com.scau.dormrepair.web.controller;

import com.scau.dormrepair.common.ApiResponse;
import com.scau.dormrepair.common.PageResponse;
import com.scau.dormrepair.service.DormRoomService;
import com.scau.dormrepair.web.dto.DormRoomResponse;
import com.scau.dormrepair.web.dto.SaveDormRoomCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 宿舍房间控制器。
 */
@RestController
@RequestMapping("/api/v1/dorm-rooms")
@Tag(name = "Dorm Rooms", description = "宿舍基础信息接口")
public class DormRoomController {

    /**
     * 宿舍房间业务入口。
     */
    private final DormRoomService dormRoomService;

    public DormRoomController(DormRoomService dormRoomService) {
        this.dormRoomService = dormRoomService;
    }

    /**
     * 创建宿舍信息。
     * @param command 宿舍房间数据
     * @return 创建后的宿舍房间信息
     */
    @PostMapping
    @Operation(summary = "创建宿舍信息")
    public ApiResponse<DormRoomResponse> create(@Valid @RequestBody SaveDormRoomCommand command) {
        return ApiResponse.created("宿舍信息已创建", dormRoomService.create(command));
    }

    /**
     * 更新宿舍信息。
     * @param id 宿舍房间 ID
     * @param command 更新后的宿舍房间数据
     * @return 更新后的宿舍房间信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新宿舍信息")
    public ApiResponse<DormRoomResponse> update(@PathVariable Long id, @Valid @RequestBody SaveDormRoomCommand command) {
        return ApiResponse.ok(dormRoomService.update(id, command));
    }

    /**
     * 查询宿舍详情。
     * @param id 宿舍房间 ID
     * @return 宿舍房间详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询宿舍详情")
    public ApiResponse<DormRoomResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(dormRoomService.getById(id));
    }

    /**
     * 分页查询宿舍列表。
     * @param page 页码
     * @param size 每页条数
     * @return 宿舍分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询宿舍信息")
    public ApiResponse<PageResponse<DormRoomResponse>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 先查分页数据，再统一包装成前端固定结构。
        Page<DormRoomResponse> pageData = dormRoomService.page(page, size);
        return ApiResponse.ok(PageResponse.from(pageData));
    }

    /**
     * 删除宿舍信息。
     * @param id 宿舍房间 ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除宿舍信息")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dormRoomService.delete(id);
        return ApiResponse.ok(null);
    }
}
