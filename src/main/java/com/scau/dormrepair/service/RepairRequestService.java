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

    List<RecentRepairRequestView> listStudentSubmittedRequests(Long studentId, String studentName, int limit);

    StudentRepairDetailView getStudentRequestDetail(Long studentId, String studentName, Long requestId);

    int urgeStudentRequest(Long studentId, String studentName, Long requestId);

    void cancelStudentRequest(Long studentId, String studentName, Long requestId);

    List<RecentRepairRequestView> listPendingAssignmentRequests(int limit);

    void submitFeedback(SubmitRepairFeedbackCommand command);
}
