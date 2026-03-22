package com.scau.dormrepair.web.controller;

import com.scau.dormrepair.common.ApiResponse;
import com.scau.dormrepair.service.DormRoomService;
import com.scau.dormrepair.web.dto.DormRoomResponse;
import com.scau.dormrepair.web.dto.SaveDormRoomCommand;
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
@RequestMapping("/api/dorm-rooms")
public class DormRoomController {

    private final DormRoomService dormRoomService;

    public DormRoomController(DormRoomService dormRoomService) {
        this.dormRoomService = dormRoomService;
    }

    @PostMapping
    public ApiResponse<DormRoomResponse> create(@Valid @RequestBody SaveDormRoomCommand command) {
        return ApiResponse.created("宿舍信息已创建", dormRoomService.create(command));
    }

    @PutMapping("/{id}")
    public ApiResponse<DormRoomResponse> update(@PathVariable Long id, @Valid @RequestBody SaveDormRoomCommand command) {
        return ApiResponse.ok(dormRoomService.update(id, command));
    }

    @GetMapping("/{id}")
    public ApiResponse<DormRoomResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(dormRoomService.getById(id));
    }

    @GetMapping
    public ApiResponse<Page<DormRoomResponse>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(dormRoomService.page(page, size));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dormRoomService.delete(id);
        return ApiResponse.ok(null);
    }
}
