package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import com.scau.dormrepair.repository.RepairRequestRepository;
import com.scau.dormrepair.service.RepairRequestService;
import com.scau.dormrepair.web.dto.CreateRepairRequestCommand;
import com.scau.dormrepair.web.dto.RepairRequestDetailResponse;
import com.scau.dormrepair.web.dto.RepairRequestSummaryResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepairRequestServiceImpl implements RepairRequestService {

    private static final String IMAGE_SEPARATOR = "||";
    private static final DateTimeFormatter NUMBER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RepairRequestRepository repairRequestRepository;

    public RepairRequestServiceImpl(RepairRequestRepository repairRequestRepository) {
        this.repairRequestRepository = repairRequestRepository;
    }

    @Override
    @Transactional
    public RepairRequestDetailResponse create(CreateRepairRequestCommand command) {
        RepairRequest repairRequest = new RepairRequest();
        repairRequest.setRequestNo(generateRequestNo());
        repairRequest.setStudentName(command.studentName());
        repairRequest.setContactPhone(command.contactPhone());
        repairRequest.setBuildingNo(command.buildingNo());
        repairRequest.setRoomNo(command.roomNo());
        repairRequest.setFaultCategory(command.faultCategory());
        repairRequest.setDescription(command.description());
        repairRequest.setImageUrls(joinImages(command.imageUrls()));
        repairRequest.setStatus(RepairRequestStatus.SUBMITTED);
        repairRequest.setSubmittedAt(LocalDateTime.now());

        return toDetailResponse(repairRequestRepository.save(repairRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public RepairRequestDetailResponse getById(Long id) {
        RepairRequest repairRequest = repairRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到报修单，ID=" + id));
        return toDetailResponse(repairRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RepairRequestSummaryResponse> page(RepairRequestStatus status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Order.desc("submittedAt"), Sort.Order.desc("id"))
        );

        Page<RepairRequest> requestPage = status == null
                ? repairRequestRepository.findAll(pageRequest)
                : repairRequestRepository.findAllByStatus(status, pageRequest);

        return requestPage.map(this::toSummaryResponse);
    }

    private RepairRequestSummaryResponse toSummaryResponse(RepairRequest entity) {
        return new RepairRequestSummaryResponse(
                entity.getId(),
                entity.getRequestNo(),
                entity.getStudentName(),
                entity.getBuildingNo(),
                entity.getRoomNo(),
                entity.getFaultCategory(),
                entity.getStatus(),
                entity.getSubmittedAt()
        );
    }

    private RepairRequestDetailResponse toDetailResponse(RepairRequest entity) {
        return new RepairRequestDetailResponse(
                entity.getId(),
                entity.getRequestNo(),
                entity.getStudentName(),
                entity.getContactPhone(),
                entity.getBuildingNo(),
                entity.getRoomNo(),
                entity.getFaultCategory(),
                entity.getDescription(),
                splitImages(entity.getImageUrls()),
                entity.getStatus(),
                entity.getReviewerId(),
                entity.getWorkerId(),
                entity.getUrgeCount(),
                entity.getSubmittedAt(),
                entity.getCompletedAt()
        );
    }

    private String generateRequestNo() {
        return "RR" + NUMBER_TIME_FORMATTER.format(LocalDateTime.now())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private String joinImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        return imageUrls.stream()
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .map(String::trim)
                .reduce((left, right) -> left + IMAGE_SEPARATOR + right)
                .orElse(null);
    }

    private List<String> splitImages(String imageUrls) {
        if (imageUrls == null || imageUrls.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(imageUrls.split("\\|\\|"));
    }
}
