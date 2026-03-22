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

@RestController
@RequestMapping("/api/v1/dorm-rooms")
@Tag(name = "Dorm Rooms", description = "宿舍基础信息接口")
/**
 * 宿舍房间控制器。
 */
public class DormRoomController {

    private final DormRoomService dormRoomService;

    public DormRoomController(DormRoomService dormRoomService) {
        this.dormRoomService = dormRoomService;
    }

    @PostMapping
    @Operation(summary = "创建宿舍信息")
    public ApiResponse<DormRoomResponse> create(@Valid @RequestBody SaveDormRoomCommand command) {
        return ApiResponse.created("宿舍信息已创建", dormRoomService.create(command));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宿舍信息")
    public ApiResponse<DormRoomResponse> update(@PathVariable Long id, @Valid @RequestBody SaveDormRoomCommand command) {
        return ApiResponse.ok(dormRoomService.update(id, command));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询宿舍详情")
    public ApiResponse<DormRoomResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(dormRoomService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询宿舍信息")
    public ApiResponse<PageResponse<DormRoomResponse>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<DormRoomResponse> pageData = dormRoomService.page(page, size);
        return ApiResponse.ok(PageResponse.from(pageData));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宿舍信息")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dormRoomService.delete(id);
        return ApiResponse.ok(null);
    }
}
