package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.mapper.DashboardMapper;
import com.scau.dormrepair.service.DashboardService;
import java.util.Collections;
import java.util.List;

/**
 * 首页概览服务实现。
 */
public class DashboardServiceImpl implements DashboardService {

    private final MyBatisExecutor myBatisExecutor;

    public DashboardServiceImpl(MyBatisExecutor myBatisExecutor) {
        this.myBatisExecutor = myBatisExecutor;
    }

    @Override
    public DashboardOverview loadOverview(UserRole role, Long accountId, String displayName) {
        DashboardOverview overview = myBatisExecutor.executeRead(session -> {
            DashboardMapper mapper = session.getMapper(DashboardMapper.class);
            if (role == UserRole.STUDENT) {
                // 学生首页只能看自己的统计，没有稳定账号时宁可返回空概览，也不能回退到全量数据。
                if (accountId == null || displayName == null || displayName.isBlank()) {
                    return null;
                }
                return mapper.selectStudentOverview(accountId, displayName.trim());
            }
            return mapper.selectOverview();
        });
        return overview == null ? new DashboardOverview() : overview;
    }

    @Override
    public List<RecentRepairRequestView> listRecentRepairRequests(
            UserRole role,
            Long accountId,
            String displayName,
            int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        if (role == UserRole.STUDENT && (accountId == null || displayName == null || displayName.isBlank())) {
            return Collections.emptyList();
        }
        return myBatisExecutor.executeRead(session -> {
            DashboardMapper mapper = session.getMapper(DashboardMapper.class);
            if (role == UserRole.STUDENT) {
                // 最近报修记录必须和当前学生绑定，避免首页直接看到别人的工单。
                return mapper.selectStudentRecentRepairRequests(accountId, displayName.trim(), safeLimit);
            }
            return mapper.selectRecentRepairRequests(safeLimit);
        });
    }
}
