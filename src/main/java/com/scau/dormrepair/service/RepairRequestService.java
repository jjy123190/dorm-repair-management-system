package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.command.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import java.util.List;

/**
 * 报修申请服务。
 */
public interface RepairRequestService {

    Long create(CreateRepairRequestCommand command);

    List<RecentRepairRequestView> listLatestSubmittedRequests(int limit);

    List<RecentRepairRequestView> listStudentSubmittedRequests(Long studentId, int limit);

    StudentRepairDetailView getStudentRequestDetail(Long studentId, Long requestId);

    List<RecentRepairRequestView> listPendingAssignmentRequests(int limit);

    void submitFeedback(SubmitRepairFeedbackCommand command);
}
