package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.common.RepairSlaPolicy;
import com.scau.dormrepair.domain.enums.TimeoutLevel;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.mapper.DashboardMapper;
import com.scau.dormrepair.service.DashboardService;
import java.util.Collections;
import java.util.List;

public class DashboardServiceImpl implements DashboardService {

    private final MyBatisExecutor myBatisExecutor;

    public DashboardServiceImpl(MyBatisExecutor myBatisExecutor) {
        this.myBatisExecutor = myBatisExecutor;
    }

    @Override
    public DashboardOverview loadOverview(UserRole role, Long accountId) {
        DashboardOverview overview = myBatisExecutor.executeRead(session -> {
            DashboardMapper mapper = session.getMapper(DashboardMapper.class);
            if (role == UserRole.STUDENT) {
                return accountId == null ? null : mapper.selectStudentOverview(accountId);
            }
            if (role == UserRole.WORKER) {
                return accountId == null ? null : mapper.selectWorkerOverview(accountId);
            }
            return mapper.selectOverview();
        });
        if (overview == null) {
            overview = new DashboardOverview();
        }
        normalizeOverview(overview);
        return overview;
    }

    @Override
    public List<RecentRepairRequestView> listRecentRepairRequests(UserRole role, Long accountId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        if ((role == UserRole.STUDENT || role == UserRole.WORKER) && accountId == null) {
            return Collections.emptyList();
        }
        return myBatisExecutor.executeRead(session -> {
            DashboardMapper mapper = session.getMapper(DashboardMapper.class);
            List<RecentRepairRequestView> rows = role == UserRole.STUDENT
                    ? mapper.selectStudentRecentRepairRequests(accountId, safeLimit)
                    : role == UserRole.WORKER
                    ? mapper.selectWorkerRecentRepairRequests(accountId, safeLimit)
                    : mapper.selectRecentRepairRequests(safeLimit);
            rows.forEach(this::applyTimeout);
            return rows;
        });
    }

    private void normalizeOverview(DashboardOverview overview) {
        if (overview.getTotalRequests() == null) {
            overview.setTotalRequests(0L);
        }
        if (overview.getPendingReviewRequests() == null) {
            overview.setPendingReviewRequests(0L);
        }
        if (overview.getActiveWorkOrders() == null) {
            overview.setActiveWorkOrders(0L);
        }
        if (overview.getCompletedThisMonth() == null) {
            overview.setCompletedThisMonth(0L);
        }
        if (overview.getTimeoutWarningCount() == null) {
            overview.setTimeoutWarningCount(0L);
        }
        if (overview.getTimeoutOverdueCount() == null) {
            overview.setTimeoutOverdueCount(0L);
        }
        if (overview.getPendingConfirmationCount() == null) {
            overview.setPendingConfirmationCount(0L);
        }
        if (overview.getReworkInProgressCount() == null) {
            overview.setReworkInProgressCount(0L);
        }
    }

    private void applyTimeout(RecentRepairRequestView row) {
        if (row == null) {
            return;
        }
        TimeoutLevel level = RepairSlaPolicy.resolveRequestLevel(row.getStatus(), row.getSubmittedAt(), row.getAssignedAt(), row.getAcceptedAt());
        row.setTimeoutLevel(level);
        row.setTimeoutLabel(RepairSlaPolicy.resolveRequestLabel(row.getStatus(), level));
    }
}