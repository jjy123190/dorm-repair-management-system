package com.scau.dormrepair.service;

import com.scau.dormrepair.web.dto.DormRoomResponse;
import com.scau.dormrepair.web.dto.SaveDormRoomCommand;
import org.springframework.data.domain.Page;

public interface DormRoomService {

    DormRoomResponse create(SaveDormRoomCommand command);

    DormRoomResponse update(Long id, SaveDormRoomCommand command);

    DormRoomResponse getById(Long id);

    Page<DormRoomResponse> page(int page, int size);

    void delete(Long id);
}
