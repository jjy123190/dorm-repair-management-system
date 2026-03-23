package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.mapper.DashboardMapper;
import com.scau.dormrepair.service.DashboardService;
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
    public DashboardOverview loadOverview() {
        DashboardOverview overview = myBatisExecutor.executeRead(
                session -> session.getMapper(DashboardMapper.class).selectOverview()
        );
        return overview == null ? new DashboardOverview() : overview;
    }

    @Override
    public List<RecentRepairRequestView> listRecentRepairRequests(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return myBatisExecutor.executeRead(
                session -> session.getMapper(DashboardMapper.class).selectRecentRepairRequests(safeLimit)
        );
    }
}
