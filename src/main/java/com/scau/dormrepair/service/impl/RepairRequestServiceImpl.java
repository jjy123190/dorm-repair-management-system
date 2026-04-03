package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.BusinessNumberGenerator;
import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.common.RepairSlaPolicy;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.command.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.domain.entity.DormBuilding;
import com.scau.dormrepair.domain.entity.DormRoom;
import com.scau.dormrepair.domain.entity.RepairFeedback;
import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.entity.RepairRequestImage;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.entity.WorkOrder;
import com.scau.dormrepair.domain.entity.WorkOrderCompletionImage;
import com.scau.dormrepair.domain.entity.WorkOrderRecord;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.TimeoutLevel;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import com.scau.dormrepair.mapper.DormBuildingMapper;
import com.scau.dormrepair.mapper.DormRoomMapper;
import com.scau.dormrepair.mapper.RepairFeedbackMapper;
import com.scau.dormrepair.mapper.RepairRequestImageMapper;
import com.scau.dormrepair.mapper.RepairRequestMapper;
import com.scau.dormrepair.mapper.UserAccountMapper;
import com.scau.dormrepair.mapper.WorkOrderCompletionImageMapper;
import com.scau.dormrepair.mapper.WorkOrderMapper;
import com.scau.dormrepair.mapper.WorkOrderRecordMapper;
import com.scau.dormrepair.service.RepairRequestService;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

public class RepairRequestServiceImpl implements RepairRequestService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s]{6,32}$");
    private static final Pattern ROOM_PATTERN = Pattern.compile("^[A-Za-z0-9\\-]{1,32}$");
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int MAX_IMAGE_COUNT = 5;
    private static final int MAX_REWORK_NOTE_LENGTH = 300;
    private static final String STUDENT_CANCEL_WORK_ORDER_NOTE = "学生取消报修，工单同步关闭。";
    private static final String STUDENT_CONFIRM_NOTE = "学生确认维修完成。";

    private final MyBatisExecutor myBatisExecutor;

    public RepairRequestServiceImpl(MyBatisExecutor myBatisExecutor) {
        this.myBatisExecutor = myBatisExecutor;
    }

    @Override
    public Long create(CreateRepairRequestCommand command) {
        validateCreateCommand(command);

        RepairRequest repairRequest = new RepairRequest();
        repairRequest.setRequestNo(BusinessNumberGenerator.nextRepairRequestNo());
        repairRequest.setStudentId(command.studentId());
        repairRequest.setStudentName(command.studentName().trim());
        repairRequest.setContactPhone(normalizePhone(command.contactPhone()));
        repairRequest.setDormRoomId(command.dormRoomId());
        repairRequest.setDormAreaSnapshot(command.dormAreaSnapshot().trim());
        repairRequest.setBuildingNoSnapshot(command.buildingNoSnapshot().trim());
        repairRequest.setRoomNoSnapshot(command.roomNoSnapshot().trim().toUpperCase());
        repairRequest.setFaultCategory(command.faultCategory());
        repairRequest.setDescription(command.description().trim());
        repairRequest.setStatus(RepairRequestStatus.SUBMITTED);
        repairRequest.setUrgeCount(0);
        repairRequest.setSubmittedAt(LocalDateTime.now());

        return myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            RepairRequestImageMapper imageMapper = session.getMapper(RepairRequestImageMapper.class);
            DormRoomMapper dormRoomMapper = session.getMapper(DormRoomMapper.class);
            DormBuildingMapper dormBuildingMapper = session.getMapper(DormBuildingMapper.class);

            validateDormRoomContext(command, dormRoomMapper, dormBuildingMapper);
            repairRequestMapper.insert(repairRequest);

            List<String> imageUrls = sanitizeImages(command.imageUrls());
            if (!imageUrls.isEmpty()) {
                imageMapper.batchInsert(repairRequest.getId(), 1, imageUrls);
            }
            return repairRequest.getId();
        });
    }

    @Override
    public List<RecentRepairRequestView> listLatestSubmittedRequests(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return myBatisExecutor.executeRead(session ->
                decorateRows(session.getMapper(RepairRequestMapper.class).selectLatestSubmittedRequests(safeLimit))
        );
    }

    @Override
    public List<RecentRepairRequestView> listStudentSubmittedRequests(Long studentId, int limit) {
        if (studentId == null) {
            throw new BusinessException("学生账号不存在，无法查询报修记录。");
        }
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return myBatisExecutor.executeRead(session ->
                decorateRows(session.getMapper(RepairRequestMapper.class).selectStudentSubmittedRequests(studentId, safeLimit))
        );
    }

    @Override
    public StudentRepairDetailView getStudentRequestDetail(Long studentId, Long requestId) {
        if (studentId == null) {
            throw new BusinessException("学生账号不存在，无法查看记录详情。");
        }
        if (requestId == null) {
            throw new BusinessException("报修记录不存在，无法查看详情。");
        }
        return myBatisExecutor.executeRead(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            RepairRequestImageMapper imageMapper = session.getMapper(RepairRequestImageMapper.class);
            RepairFeedbackMapper repairFeedbackMapper = session.getMapper(RepairFeedbackMapper.class);
            WorkOrderMapper workOrderMapper = session.getMapper(WorkOrderMapper.class);
            WorkOrderRecordMapper workOrderRecordMapper = session.getMapper(WorkOrderRecordMapper.class);
            WorkOrderCompletionImageMapper completionImageMapper = session.getMapper(WorkOrderCompletionImageMapper.class);
            UserAccountMapper userAccountMapper = session.getMapper(UserAccountMapper.class);

            StudentRepairDetailView detailView = repairRequestMapper.selectStudentRequestDetail(studentId, requestId);
            if (detailView == null) {
                throw new ResourceNotFoundException("未找到对应的报修记录，记录 ID=" + requestId);
            }

            detailView.setImageUrls(imageMapper.selectByRepairRequestId(requestId).stream()
                    .map(image -> image.getImageUrl() == null ? "" : image.getImageUrl().trim())
                    .filter(imageUrl -> !imageUrl.isBlank())
                    .toList());

            WorkOrder workOrder = workOrderMapper.selectByRepairRequestId(requestId);
            if (workOrder != null) {
                detailView.setWorkOrderNo(trimToNull(workOrder.getWorkOrderNo()));
                detailView.setAssignmentNote(trimToNull(workOrder.getAssignmentNote()));
                detailView.setAssignedAt(workOrder.getAssignedAt());
                detailView.setAcceptedAt(workOrder.getAcceptedAt());
                detailView.setCompletionNote(trimToNull(workOrder.getCompletionNote()));
                detailView.setCompletionImageUrls(completionImageMapper.selectByWorkOrderId(workOrder.getId()).stream()
                        .map(WorkOrderCompletionImage::getImageUrl)
                        .filter(item -> item != null && !item.isBlank())
                        .toList());
                if (workOrder.getWorkerId() != null) {
                    detailView.setWorkerId(workOrder.getWorkerId());
                    detailView.setWorkerName(resolveWorkerName(userAccountMapper, workOrder.getWorkerId()));
                }
                detailView.setRecords(workOrderRecordMapper.selectTimelineByWorkOrderId(workOrder.getId()));
            } else if (detailView.getWorkerId() != null) {
                detailView.setWorkerName(resolveWorkerName(userAccountMapper, detailView.getWorkerId()));
                detailView.setRecords(List.of());
            } else {
                detailView.setRecords(List.of());
            }

            RepairFeedback repairFeedback = repairFeedbackMapper.selectByRepairRequestId(requestId);
            if (repairFeedback != null) {
                detailView.setFeedbackRating(repairFeedback.getRating());
                detailView.setFeedbackComment(trimToNull(repairFeedback.getFeedbackComment()));
                detailView.setFeedbackAnonymousFlag(repairFeedback.getAnonymousFlag());
            }

            applyTimeout(detailView);
            return detailView;
        });
    }

    @Override
    public int appendStudentRequestImages(Long studentId, Long requestId, List<String> imageUrls) {
        if (studentId == null) {
            throw new BusinessException("学生账号不存在，无法补充图片。");
        }
        if (requestId == null) {
            throw new BusinessException("报修记录不存在，无法补充图片。");
        }
        return myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            RepairRequestImageMapper imageMapper = session.getMapper(RepairRequestImageMapper.class);

            RepairRequest repairRequest = requireStudentOwnedRequest(repairRequestMapper, studentId, requestId);
            if (repairRequest.getStatus() == RepairRequestStatus.COMPLETED
                    || repairRequest.getStatus() == RepairRequestStatus.REJECTED
                    || repairRequest.getStatus() == RepairRequestStatus.CANCELLED) {
                throw new BusinessException("当前报修已结束，不能继续补充图片。");
            }

            List<String> incomingImages = sanitizeImages(imageUrls);
            if (incomingImages.isEmpty()) {
                throw new BusinessException("请先选择至少 1 张图片再提交。");
            }

            List<RepairRequestImage> existingImages = imageMapper.selectByRepairRequestId(requestId);
            LinkedHashSet<String> mergedImages = new LinkedHashSet<>();
            existingImages.stream()
                    .map(RepairRequestImage::getImageUrl)
                    .filter(item -> item != null && !item.isBlank())
                    .map(String::trim)
                    .forEach(mergedImages::add);
            incomingImages.forEach(mergedImages::add);

            if (mergedImages.size() > MAX_IMAGE_COUNT) {
                throw new BusinessException("当前报修最多保留 " + MAX_IMAGE_COUNT + " 张图片，请减少后再提交。");
            }

            List<String> newImageUrls = mergedImages.stream()
                    .skip(existingImages.size())
                    .toList();
            if (newImageUrls.isEmpty()) {
                throw new BusinessException("这批图片已经补充过了，请不要重复提交。");
            }

            imageMapper.batchInsert(requestId, existingImages.size() + 1, newImageUrls);
            return mergedImages.size();
        });
    }

    @Override
    public int urgeStudentRequest(Long studentId, Long requestId) {
        if (studentId == null) {
            throw new BusinessException("学生账号不存在，无法催办报修。");
        }
        if (requestId == null) {
            throw new BusinessException("报修记录不存在，无法催办。");
        }
        return myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            RepairRequest repairRequest = requireStudentOwnedRequest(repairRequestMapper, studentId, requestId);
            if (repairRequest.getStatus() == RepairRequestStatus.COMPLETED
                    || repairRequest.getStatus() == RepairRequestStatus.REJECTED
                    || repairRequest.getStatus() == RepairRequestStatus.CANCELLED
                    || repairRequest.getStatus() == RepairRequestStatus.PENDING_CONFIRMATION) {
                throw new BusinessException("当前状态不能继续催办。");
            }
            int affectedRows = repairRequestMapper.increaseUrgeCount(studentId, requestId);
            if (affectedRows == 0) {
                throw new BusinessException("当前状态不能继续催办。");
            }
            RepairRequest refreshed = repairRequestMapper.selectById(requestId);
            return refreshed == null || refreshed.getUrgeCount() == null ? 0 : refreshed.getUrgeCount();
        });
    }

    @Override
    public void cancelStudentRequest(Long studentId, Long requestId) {
        if (studentId == null) {
            throw new BusinessException("学生账号不存在，无法取消报修。");
        }
        if (requestId == null) {
            throw new BusinessException("报修记录不存在，无法取消。");
        }
        myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            WorkOrderMapper workOrderMapper = session.getMapper(WorkOrderMapper.class);
            WorkOrderRecordMapper workOrderRecordMapper = session.getMapper(WorkOrderRecordMapper.class);

            RepairRequest repairRequest = requireStudentOwnedRequest(repairRequestMapper, studentId, requestId);
            if (repairRequest.getStatus() == RepairRequestStatus.COMPLETED) {
                throw new BusinessException("当前报修已处理完成，不能取消。");
            }
            if (repairRequest.getStatus() == RepairRequestStatus.REJECTED
                    || repairRequest.getStatus() == RepairRequestStatus.CANCELLED
                    || repairRequest.getStatus() == RepairRequestStatus.PENDING_CONFIRMATION
                    || repairRequest.getStatus() == RepairRequestStatus.REWORK_IN_PROGRESS
                    || repairRequest.getStatus() == RepairRequestStatus.IN_PROGRESS) {
                throw new BusinessException("当前阶段不能取消报修。");
            }
            int affectedRows = repairRequestMapper.cancelStudentRequest(studentId, requestId, RepairRequestStatus.CANCELLED);
            if (affectedRows == 0) {
                throw new BusinessException("当前状态不能取消报修。");
            }
            cancelLinkedWorkOrderIfPresent(workOrderMapper, workOrderRecordMapper, studentId, requestId);
            return null;
        });
    }

    @Override
    public void confirmStudentCompletion(Long studentId, Long requestId) {
        if (studentId == null || requestId == null) {
            throw new BusinessException("确认完成参数不完整。");
        }
        myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            WorkOrderMapper workOrderMapper = session.getMapper(WorkOrderMapper.class);
            WorkOrderRecordMapper workOrderRecordMapper = session.getMapper(WorkOrderRecordMapper.class);

            RepairRequest repairRequest = requireStudentOwnedRequest(repairRequestMapper, studentId, requestId);
            if (repairRequest.getStatus() != RepairRequestStatus.PENDING_CONFIRMATION) {
                throw new BusinessException("当前报修还未进入待确认完成阶段。");
            }

            WorkOrder workOrder = workOrderMapper.selectByRepairRequestId(requestId);
            if (workOrder == null) {
                throw new BusinessException("当前报修缺少关联工单，无法确认完成。");
            }

            LocalDateTime confirmedAt = LocalDateTime.now();
            repairRequestMapper.updateStatus(requestId, RepairRequestStatus.COMPLETED, confirmedAt);
            workOrderMapper.updateStatus(
                    workOrder.getId(),
                    WorkOrderStatus.COMPLETED,
                    workOrder.getAcceptedAt(),
                    workOrder.getCompletedAt() == null ? confirmedAt : workOrder.getCompletedAt(),
                    workOrder.getCompletionNote()
            );

            WorkOrderRecord record = new WorkOrderRecord();
            record.setWorkOrderId(workOrder.getId());
            record.setOperatorId(studentId);
            record.setStatus(WorkOrderStatus.COMPLETED);
            record.setRecordNote(STUDENT_CONFIRM_NOTE);
            workOrderRecordMapper.insert(record);
            return null;
        });
    }

    @Override
    public void requestStudentRework(Long studentId, Long requestId, String reworkNote) {
        if (studentId == null || requestId == null) {
            throw new BusinessException("返修申请参数不完整。");
        }
        String normalizedNote = normalizeReworkNote(reworkNote);
        myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            WorkOrderMapper workOrderMapper = session.getMapper(WorkOrderMapper.class);
            WorkOrderRecordMapper workOrderRecordMapper = session.getMapper(WorkOrderRecordMapper.class);

            RepairRequest repairRequest = requireStudentOwnedRequest(repairRequestMapper, studentId, requestId);
            if (repairRequest.getStatus() != RepairRequestStatus.PENDING_CONFIRMATION) {
                throw new BusinessException("当前报修还未进入待确认完成阶段，不能申请返修。");
            }

            WorkOrder workOrder = workOrderMapper.selectByRepairRequestId(requestId);
            if (workOrder == null) {
                throw new BusinessException("当前报修缺少关联工单，无法发起返修。");
            }

            repairRequestMapper.updateStatus(requestId, RepairRequestStatus.REWORK_IN_PROGRESS, null);
            workOrderMapper.updateStatus(
                    workOrder.getId(),
                    WorkOrderStatus.IN_PROGRESS,
                    workOrder.getAcceptedAt(),
                    null,
                    workOrder.getCompletionNote()
            );

            WorkOrderRecord record = new WorkOrderRecord();
            record.setWorkOrderId(workOrder.getId());
            record.setOperatorId(studentId);
            record.setStatus(WorkOrderStatus.IN_PROGRESS);
            record.setRecordNote("学生申请返修：" + normalizedNote);
            workOrderRecordMapper.insert(record);
            return null;
        });
    }

    @Override
    public List<RecentRepairRequestView> listPendingAssignmentRequests(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return myBatisExecutor.executeRead(session ->
                decorateRows(session.getMapper(RepairRequestMapper.class).selectPendingAssignmentRequests(safeLimit))
        );
    }

    @Override
    public void submitFeedback(SubmitRepairFeedbackCommand command) {
        if (command == null || command.studentId() == null || command.repairRequestId() == null) {
            throw new BusinessException("报修记录不存在，无法提交评价。");
        }
        if (command.rating() == null || command.rating() < 1 || command.rating() > 5) {
            throw new BusinessException("评分必须在 1 到 5 分之间。");
        }
        myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            RepairFeedbackMapper repairFeedbackMapper = session.getMapper(RepairFeedbackMapper.class);

            RepairRequest repairRequest = requireStudentOwnedRequest(
                    repairRequestMapper,
                    command.studentId(),
                    command.repairRequestId()
            );
            if (repairRequest.getStatus() != RepairRequestStatus.COMPLETED) {
                throw new BusinessException("当前报修尚未处理完成，暂时不能评价。");
            }
            if (repairFeedbackMapper.selectByRepairRequestId(command.repairRequestId()) != null) {
                throw new BusinessException("该报修已提交评价，不能重复提交。");
            }

            RepairFeedback repairFeedback = new RepairFeedback();
            repairFeedback.setRepairRequestId(command.repairRequestId());
            repairFeedback.setRating(command.rating());
            repairFeedback.setFeedbackComment(trimToNull(command.feedbackComment()));
            repairFeedback.setAnonymousFlag(command.anonymousFlag());
            repairFeedbackMapper.insert(repairFeedback);
            return null;
        });
    }

    private void validateCreateCommand(CreateRepairRequestCommand command) {
        if (command == null) {
            throw new BusinessException("报修信息不能为空。");
        }
        if (command.studentId() == null) {
            throw new BusinessException("当前学生账号不存在，无法提交报修。");
        }
        normalizeStudentName(command.studentName());
        if (command.contactPhone() == null || command.contactPhone().isBlank()) {
            throw new BusinessException("联系电话不能为空。");
        }
        if (!PHONE_PATTERN.matcher(normalizePhone(command.contactPhone())).matches()) {
            throw new BusinessException("联系电话格式不合法，请输入常见手机号或座机号。");
        }
        if (command.dormAreaSnapshot() == null || command.dormAreaSnapshot().isBlank()) {
            throw new BusinessException("宿舍区不能为空。");
        }
        if (command.buildingNoSnapshot() == null || command.buildingNoSnapshot().isBlank()) {
            throw new BusinessException("宿舍楼不能为空。");
        }
        if (command.roomNoSnapshot() == null || command.roomNoSnapshot().isBlank()) {
            throw new BusinessException("房间号不能为空。");
        }
        if (!ROOM_PATTERN.matcher(command.roomNoSnapshot().trim()).matches()) {
            throw new BusinessException("房间号格式不合法，请输入常见房间号。");
        }
        if (command.faultCategory() == null) {
            throw new BusinessException("故障类别不能为空。");
        }
        if (command.description() == null || command.description().isBlank()) {
            throw new BusinessException("故障描述不能为空。");
        }

        String normalizedDescription = command.description().trim();
        if (normalizedDescription.length() < 5) {
            throw new BusinessException("故障描述至少需要 5 个字符。");
        }
        if (normalizedDescription.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException("故障描述不能超过 " + MAX_DESCRIPTION_LENGTH + " 个字符。");
        }
        if (countImages(command.imageUrls()) > MAX_IMAGE_COUNT) {
            throw new BusinessException("最多只能上传 " + MAX_IMAGE_COUNT + " 张图片。");
        }
    }

    private void validateDormRoomContext(
            CreateRepairRequestCommand command,
            DormRoomMapper dormRoomMapper,
            DormBuildingMapper dormBuildingMapper
    ) {
        if (command.dormRoomId() == null) {
            return;
        }

        DormRoom dormRoom = dormRoomMapper.selectById(command.dormRoomId());
        if (dormRoom == null) {
            throw new BusinessException("所选房间不存在，请重新选择。");
        }

        String roomStatus = trimToNull(dormRoom.getRoomStatus());
        if (roomStatus != null && !"ACTIVE".equalsIgnoreCase(roomStatus)) {
            throw new BusinessException("所选房间已停用，请重新选择可报修房间。");
        }

        DormBuilding dormBuilding = dormBuildingMapper.selectById(dormRoom.getBuildingId());
        if (dormBuilding == null) {
            throw new BusinessException("所选楼栋不存在，请刷新页面后重新选择。");
        }

        String areaSnapshot = trimToNull(command.dormAreaSnapshot());
        String buildingSnapshot = trimToNull(command.buildingNoSnapshot());
        String roomSnapshot = trimToNull(command.roomNoSnapshot());
        boolean areaMatched = areaSnapshot != null && areaSnapshot.equals(dormBuilding.getCampusName());
        boolean buildingMatched = buildingSnapshot != null
                && buildingSnapshot.equalsIgnoreCase(dormBuilding.getBuildingNo());
        boolean roomMatched = roomSnapshot != null && roomSnapshot.equalsIgnoreCase(dormRoom.getRoomNo());
        if (!areaMatched || !buildingMatched || !roomMatched) {
            throw new BusinessException("宿舍位置信息已变更，请重新选择房间后再提交。");
        }
    }

    private int countImages(List<String> imageUrls) {
        if (imageUrls == null) {
            return 0;
        }
        return (int) imageUrls.stream()
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .map(String::trim)
                .distinct()
                .count();
    }

    private List<String> sanitizeImages(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }
        return imageUrls.stream()
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .map(String::trim)
                .distinct()
                .limit(MAX_IMAGE_COUNT)
                .toList();
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.trim().replace('\u3000', ' ');
    }

    private String normalizeStudentName(String studentName) {
        if (studentName == null || studentName.isBlank()) {
            throw new BusinessException("当前学生信息不完整，请重新登录后再试。");
        }
        return studentName.trim();
    }

    private void cancelLinkedWorkOrderIfPresent(
            WorkOrderMapper workOrderMapper,
            WorkOrderRecordMapper workOrderRecordMapper,
            Long studentId,
            Long repairRequestId
    ) {
        WorkOrder workOrder = workOrderMapper.selectByRepairRequestId(repairRequestId);
        if (workOrder == null || workOrder.getStatus() == null || isFinishedStatus(workOrder.getStatus())) {
            return;
        }

        LocalDateTime cancelledAt = LocalDateTime.now();
        workOrderMapper.updateStatus(
                workOrder.getId(),
                WorkOrderStatus.CANCELLED,
                workOrder.getAcceptedAt(),
                cancelledAt,
                workOrder.getCompletionNote()
        );

        WorkOrderRecord workOrderRecord = new WorkOrderRecord();
        workOrderRecord.setWorkOrderId(workOrder.getId());
        workOrderRecord.setOperatorId(studentId);
        workOrderRecord.setStatus(WorkOrderStatus.CANCELLED);
        workOrderRecord.setRecordNote(STUDENT_CANCEL_WORK_ORDER_NOTE);
        workOrderRecordMapper.insert(workOrderRecord);
    }

    private boolean isFinishedStatus(WorkOrderStatus workOrderStatus) {
        return workOrderStatus == WorkOrderStatus.COMPLETED
                || workOrderStatus == WorkOrderStatus.CANCELLED
                || workOrderStatus == WorkOrderStatus.REJECTED;
    }

    private String resolveWorkerName(UserAccountMapper userAccountMapper, Long workerId) {
        if (workerId == null) {
            return null;
        }
        UserAccount workerAccount = userAccountMapper.selectById(workerId);
        if (workerAccount == null) {
            return null;
        }
        return trimToNull(workerAccount.getDisplayName());
    }

    private String normalizeReworkNote(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException("申请返修时请补充异议说明。");
        }
        if (normalized.length() > MAX_REWORK_NOTE_LENGTH) {
            throw new BusinessException("返修说明不能超过 " + MAX_REWORK_NOTE_LENGTH + " 个字符。");
        }
        return normalized;
    }

    private void applyTimeout(RecentRepairRequestView row) {
        if (row == null) {
            return;
        }
        TimeoutLevel level = RepairSlaPolicy.resolveRequestLevel(
                row.getStatus(),
                row.getSubmittedAt(),
                row.getAssignedAt(),
                row.getAcceptedAt()
        );
        row.setTimeoutLevel(level);
        row.setTimeoutLabel(RepairSlaPolicy.resolveRequestLabel(row.getStatus(), level));
    }

    private void applyTimeout(StudentRepairDetailView detail) {
        if (detail == null) {
            return;
        }
        TimeoutLevel level = RepairSlaPolicy.resolveRequestLevel(
                detail.getStatus(),
                detail.getSubmittedAt(),
                detail.getAssignedAt(),
                detail.getAcceptedAt()
        );
        detail.setTimeoutLevel(level);
        detail.setTimeoutLabel(RepairSlaPolicy.resolveRequestLabel(detail.getStatus(), level));
    }

    private List<RecentRepairRequestView> decorateRows(List<RecentRepairRequestView> rows) {
        rows.forEach(this::applyTimeout);
        return rows;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private RepairRequest requireStudentOwnedRequest(
            RepairRequestMapper repairRequestMapper,
            Long studentId,
            Long requestId
    ) {
        RepairRequest repairRequest = repairRequestMapper.selectById(requestId);
        if (repairRequest == null || repairRequest.getStudentId() == null || !studentId.equals(repairRequest.getStudentId())) {
            throw new ResourceNotFoundException("未找到对应的报修记录，记录 ID=" + requestId);
        }
        return repairRequest;
    }
}
