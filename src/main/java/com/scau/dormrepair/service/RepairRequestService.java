package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.web.dto.CreateRepairRequestCommand;
import com.scau.dormrepair.web.dto.RepairRequestDetailResponse;
import com.scau.dormrepair.web.dto.RepairRequestSummaryResponse;
import org.springframework.data.domain.Page;

public interface RepairRequestService {

    RepairRequestDetailResponse create(CreateRepairRequestCommand command);

    RepairRequestDetailResponse getById(Long id);

    Page<RepairRequestSummaryResponse> page(RepairRequestStatus status, int page, int size);
}
