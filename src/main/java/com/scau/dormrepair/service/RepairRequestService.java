package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.command.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import java.util.List;

/**
 * 报修申请服务。
 */
public interface RepairRequestService {

    Long create(CreateRepairRequestCommand command);

    List<RecentRepairRequestView> listLatestSubmittedRequests(int limit);

    List<RecentRepairRequestView> listPendingAssignmentRequests(int limit);

    void submitFeedback(SubmitRepairFeedbackCommand command);
}
