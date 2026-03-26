package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.BusinessNumberGenerator;
import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.command.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.domain.entity.RepairFeedback;
import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import com.scau.dormrepair.mapper.RepairFeedbackMapper;
import com.scau.dormrepair.mapper.RepairRequestImageMapper;
import com.scau.dormrepair.mapper.RepairRequestMapper;
import com.scau.dormrepair.service.RepairRequestService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 报修申请服务实现。
 */
public class RepairRequestServiceImpl implements RepairRequestService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s]{6,32}$");
    private static final Pattern ROOM_PATTERN = Pattern.compile("^[A-Za-z0-9\\-]{1,32}$");
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int MAX_IMAGE_COUNT = 5;

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
            repairRequestMapper.insert(repairRequest);

            List<String> imageUrls = sanitizeImages(command.imageUrls());
            if (!imageUrls.isEmpty()) {
                imageMapper.batchInsert(repairRequest.getId(), imageUrls);
            }
            return repairRequest.getId();
        });
    }

    @Override
    public List<RecentRepairRequestView> listLatestSubmittedRequests(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return myBatisExecutor.executeRead(
                session -> session.getMapper(RepairRequestMapper.class).selectLatestSubmittedRequests(safeLimit)
        );
    }

    @Override
    public List<RecentRepairRequestView> listStudentSubmittedRequests(Long studentId, int limit) {
        if (studentId == null) {
            throw new BusinessException("学生 ID 不能为空。");
        }

        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return myBatisExecutor.executeRead(
                session -> session.getMapper(RepairRequestMapper.class).selectStudentSubmittedRequests(studentId, safeLimit)
        );
    }

    @Override
    public StudentRepairDetailView getStudentRequestDetail(Long studentId, Long requestId) {
        if (studentId == null) {
            throw new BusinessException("学生 ID 不能为空。");
        }
        if (requestId == null) {
            throw new BusinessException("报修记录 ID 不能为空。");
        }

        return myBatisExecutor.executeRead(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            RepairRequestImageMapper imageMapper = session.getMapper(RepairRequestImageMapper.class);
            RepairFeedbackMapper repairFeedbackMapper = session.getMapper(RepairFeedbackMapper.class);

            StudentRepairDetailView detailView =
                    repairRequestMapper.selectStudentRequestDetail(studentId, requestId);
            if (detailView == null) {
                throw new ResourceNotFoundException("未找到当前学生的报修记录，ID=" + requestId);
            }

            detailView.setImageUrls(
                    imageMapper.selectByRepairRequestId(requestId).stream()
                            .map(image -> image.getImageUrl() == null ? "" : image.getImageUrl().trim())
                            .filter(imageUrl -> !imageUrl.isBlank())
                            .toList()
            );
            RepairFeedback repairFeedback = repairFeedbackMapper.selectByRepairRequestId(requestId);
            if (repairFeedback != null) {
                detailView.setFeedbackRating(repairFeedback.getRating());
                detailView.setFeedbackComment(repairFeedback.getFeedbackComment());
                detailView.setFeedbackAnonymousFlag(repairFeedback.getAnonymousFlag());
            }
            return detailView;
        });
    }

    @Override
    public List<RecentRepairRequestView> listPendingAssignmentRequests(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return myBatisExecutor.executeRead(
                session -> session.getMapper(RepairRequestMapper.class).selectPendingAssignmentRequests(safeLimit)
        );
    }

    @Override
    public void submitFeedback(SubmitRepairFeedbackCommand command) {
        if (command.rating() == null || command.rating() < 1 || command.rating() > 5) {
            throw new BusinessException("评分必须在 1 到 5 之间。");
        }

        myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            RepairFeedbackMapper repairFeedbackMapper = session.getMapper(RepairFeedbackMapper.class);

            RepairRequest repairRequest = repairRequestMapper.selectById(command.repairRequestId());
            if (repairRequest == null) {
                throw new ResourceNotFoundException("未找到报修单，ID=" + command.repairRequestId());
            }
            if (repairRequest.getStatus() != RepairRequestStatus.COMPLETED) {
                throw new BusinessException("只有已完成的报修单才能评价。");
            }
            if (repairFeedbackMapper.selectByRepairRequestId(command.repairRequestId()) != null) {
                throw new BusinessException("该报修单已经评价过。");
            }

            RepairFeedback repairFeedback = new RepairFeedback();
            repairFeedback.setRepairRequestId(command.repairRequestId());
            repairFeedback.setRating(command.rating());
            repairFeedback.setFeedbackComment(command.feedbackComment());
            repairFeedback.setAnonymousFlag(command.anonymousFlag());
            repairFeedbackMapper.insert(repairFeedback);
            return null;
        });
    }

    private void validateCreateCommand(CreateRepairRequestCommand command) {
        if (command.studentName() == null || command.studentName().isBlank()) {
            throw new BusinessException("学生姓名不能为空。");
        }
        if (command.contactPhone() == null || command.contactPhone().isBlank()) {
            throw new BusinessException("联系电话不能为空。");
        }
        if (!PHONE_PATTERN.matcher(normalizePhone(command.contactPhone())).matches()) {
            throw new BusinessException("联系电话格式不正确，请输入常用手机号或座机号。");
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
            throw new BusinessException("房间号仅支持字母、数字和短横线。");
        }
        if (command.faultCategory() == null) {
            throw new BusinessException("故障类型不能为空。");
        }
        if (command.description() == null || command.description().isBlank()) {
            throw new BusinessException("故障描述不能为空。");
        }
        if (command.description().trim().length() < 5) {
            throw new BusinessException("故障描述至少填写 5 个字，方便后续派单处理。");
        }
        if (command.description().trim().length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException("故障描述不能超过 " + MAX_DESCRIPTION_LENGTH + " 个字符。");
        }
        if (sanitizeImages(command.imageUrls()).size() > MAX_IMAGE_COUNT) {
            throw new BusinessException("最多只能上传 " + MAX_IMAGE_COUNT + " 张图片。");
        }
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
        return phone == null ? "" : phone.trim().replace("　", " ");
    }
}
