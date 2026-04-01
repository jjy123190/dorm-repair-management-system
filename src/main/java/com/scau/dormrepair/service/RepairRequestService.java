package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.command.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import java.util.List;

/**
 * 鎶ヤ慨鐢宠鏈嶅姟銆?
 */
public interface RepairRequestService {

    Long create(CreateRepairRequestCommand command);

    List<RecentRepairRequestView> listLatestSubmittedRequests(int limit);

    List<RecentRepairRequestView> listStudentSubmittedRequests(Long studentId, int limit);

    StudentRepairDetailView getStudentRequestDetail(Long studentId, Long requestId);

    int urgeStudentRequest(Long studentId, Long requestId);

    void cancelStudentRequest(Long studentId, Long requestId);

    void confirmStudentCompletion(Long studentId, Long requestId);

    void requestStudentRework(Long studentId, Long requestId, String reworkNote);

    List<RecentRepairRequestView> listPendingAssignmentRequests(int limit);

    void submitFeedback(SubmitRepairFeedbackCommand command);
}