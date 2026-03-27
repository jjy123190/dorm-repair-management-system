package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.BusinessNumberGenerator;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.command.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.domain.entity.RepairFeedback;
import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.entity.WorkOrder;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import com.scau.dormrepair.mapper.RepairFeedbackMapper;
import com.scau.dormrepair.mapper.RepairRequestImageMapper;
import com.scau.dormrepair.mapper.RepairRequestMapper;
import com.scau.dormrepair.mapper.WorkOrderMapper;
import com.scau.dormrepair.service.RepairRequestService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

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
    public List<RecentRepairRequestView> listStudentSubmittedRequests(Long studentId, String studentName, int limit) {
        if (studentId == null) {
            throw new BusinessException("学生 ID 不能为空。");
        }
        if (studentName == null || studentName.isBlank()) {
            throw new BusinessException("学生姓名不能为空。");
        }

        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return myBatisExecutor.executeRead(
                session -> session.getMapper(RepairRequestMapper.class)
                        .selectStudentSubmittedRequests(studentId, studentName.trim(), safeLimit)
        );
    }

    @Override
    public StudentRepairDetailView getStudentRequestDetail(Long studentId, String studentName, Long requestId) {
        if (studentId == null) {
            throw new BusinessException("学生 ID 不能为空。");
        }
        if (studentName == null || studentName.isBlank()) {
            throw new BusinessException("学生姓名不能为空。");
        }
        if (requestId == null) {
            throw new BusinessException("报修记录 ID 不能为空。");
        }

        return myBatisExecutor.executeRead(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            RepairRequestImageMapper imageMapper = session.getMapper(RepairRequestImageMapper.class);
            RepairFeedbackMapper repairFeedbackMapper = session.getMapper(RepairFeedbackMapper.class);
            WorkOrderMapper workOrderMapper = session.getMapper(WorkOrderMapper.class);

            StudentRepairDetailView detailView =
                    repairRequestMapper.selectStudentRequestDetail(studentId, studentName.trim(), requestId);
            if (detailView == null) {
                throw new ResourceNotFoundException("未找到当前学生对应的报修记录，ID=" + requestId);
            }

            detailView.setImageUrls(
                    imageMapper.selectByRepairRequestId(requestId).stream()
                            .map(image -> image.getImageUrl() == null ? "" : image.getImageUrl().trim())
                            .filter(imageUrl -> !imageUrl.isBlank())
                            .toList()
            );

            WorkOrder workOrder = workOrderMapper.selectByRepairRequestId(requestId);
            if (workOrder != null) {
                detailView.setWorkOrderNo(workOrder.getWorkOrderNo());
                detailView.setAssignmentNote(workOrder.getAssignmentNote());
                detailView.setAssignedAt(workOrder.getAssignedAt());
                detailView.setAcceptedAt(workOrder.getAcceptedAt());
                if (workOrder.getWorkerId() != null) {
                    detailView.setWorkerId(workOrder.getWorkerId());
                    detailView.setWorkerName(DemoAccountDirectory.workerName(workOrder.getWorkerId()));
                }
            } else if (detailView.getWorkerId() != null) {
                detailView.setWorkerName(DemoAccountDirectory.workerName(detailView.getWorkerId()));
            }

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
    public int urgeStudentRequest(Long studentId, String studentName, Long requestId) {
        if (studentId == null) {
            throw new BusinessException("学生 ID 不能为空。");
        }
        if (studentName == null || studentName.isBlank()) {
            throw new BusinessException("学生姓名不能为空。");
        }
        if (requestId == null) {
            throw new BusinessException("报修记录 ID 不能为空。");
        }

        return myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            String normalizedStudentName = studentName.trim();
            RepairRequest repairRequest =
                    requireStudentOwnedRequest(repairRequestMapper, studentId, normalizedStudentName, requestId);

            if (repairRequest.getStatus() == RepairRequestStatus.COMPLETED
                    || repairRequest.getStatus() == RepairRequestStatus.REJECTED
                    || repairRequest.getStatus() == RepairRequestStatus.CANCELLED) {
                throw new BusinessException("当前状态不支持催办。");
            }

            int affectedRows = repairRequestMapper.increaseUrgeCount(studentId, normalizedStudentName, requestId);
            if (affectedRows == 0) {
                throw new BusinessException("当前报修暂时无法催办，请刷新后重试。");
            }

            RepairRequest refreshed = repairRequestMapper.selectById(requestId);
            return refreshed == null || refreshed.getUrgeCount() == null ? 0 : refreshed.getUrgeCount();
        });
    }

    @Override
    public void cancelStudentRequest(Long studentId, String studentName, Long requestId) {
        if (studentId == null) {
            throw new BusinessException("学生 ID 不能为空。");
        }
        if (studentName == null || studentName.isBlank()) {
            throw new BusinessException("学生姓名不能为空。");
        }
        if (requestId == null) {
            throw new BusinessException("报修记录 ID 不能为空。");
        }

        myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            String normalizedStudentName = studentName.trim();
            RepairRequest repairRequest =
                    requireStudentOwnedRequest(repairRequestMapper, studentId, normalizedStudentName, requestId);

            if (repairRequest.getStatus() == RepairRequestStatus.COMPLETED) {
                throw new BusinessException("已完成报修不能取消。");
            }
            if (repairRequest.getStatus() == RepairRequestStatus.REJECTED
                    || repairRequest.getStatus() == RepairRequestStatus.CANCELLED) {
                throw new BusinessException("当前报修已经关闭，不能重复取消。");
            }
            if (repairRequest.getStatus() == RepairRequestStatus.IN_PROGRESS) {
                throw new BusinessException("维修处理中不能直接取消，请联系管理员。");
            }

            int affectedRows = repairRequestMapper.cancelStudentRequest(
                    studentId,
                    normalizedStudentName,
                    requestId,
                    RepairRequestStatus.CANCELLED
            );
            if (affectedRows == 0) {
                throw new BusinessException("当前报修暂时无法取消，请刷新后重试。");
            }
            return null;
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
            throw new BusinessException("评分只能在 1 到 5 分之间。");
        }

        myBatisExecutor.executeWrite(session -> {
            RepairRequestMapper repairRequestMapper = session.getMapper(RepairRequestMapper.class);
            RepairFeedbackMapper repairFeedbackMapper = session.getMapper(RepairFeedbackMapper.class);

            RepairRequest repairRequest = repairRequestMapper.selectById(command.repairRequestId());
            if (repairRequest == null) {
                throw new ResourceNotFoundException("未找到对应报修记录，ID=" + command.repairRequestId());
            }
            if (repairRequest.getStatus() != RepairRequestStatus.COMPLETED) {
                throw new BusinessException("只有已完成报修才能提交评价。");
            }
            if (repairFeedbackMapper.selectByRepairRequestId(command.repairRequestId()) != null) {
                throw new BusinessException("当前报修已经提交过评价。");
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
            throw new BusinessException("联系电话格式不正确，请输入数字、空格、加号或减号。");
        }
        if (command.dormAreaSnapshot() == null || command.dormAreaSnapshot().isBlank()) {
            throw new BusinessException("宿舍区不能为空。");
        }
        if (command.buildingNoSnapshot() == null || command.buildingNoSnapshot().isBlank()) {
            throw new BusinessException("楼栋不能为空。");
        }
        if (command.roomNoSnapshot() == null || command.roomNoSnapshot().isBlank()) {
            throw new BusinessException("房间号不能为空。");
        }
        if (!ROOM_PATTERN.matcher(command.roomNoSnapshot().trim()).matches()) {
            throw new BusinessException("房间号格式不正确，请只输入字母、数字或短横线。");
        }
        if (command.faultCategory() == null) {
            throw new BusinessException("故障类别不能为空。");
        }
        if (command.description() == null || command.description().isBlank()) {
            throw new BusinessException("故障描述不能为空。");
        }
        if (command.description().trim().length() < 5) {
            throw new BusinessException("故障描述至少需要 5 个字符。");
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
        return phone == null ? "" : phone.trim().replace('\u3000', ' ');
    }

    private RepairRequest requireStudentOwnedRequest(
            RepairRequestMapper repairRequestMapper,
            Long studentId,
            String studentName,
            Long requestId
    ) {
        RepairRequest repairRequest = repairRequestMapper.selectById(requestId);
        if (repairRequest == null
                || repairRequest.getStudentId() == null
                || !studentId.equals(repairRequest.getStudentId())
                || !studentName.equals(repairRequest.getStudentName())) {
            throw new ResourceNotFoundException("未找到当前学生对应的报修记录，ID=" + requestId);
        }
        return repairRequest;
    }
}
