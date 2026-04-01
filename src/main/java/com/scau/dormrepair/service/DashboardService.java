package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import java.util.List;

/**
 * 首页概览服务。
 */
public interface DashboardService {

    DashboardOverview loadOverview(UserRole role, Long accountId);

    List<RecentRepairRequestView> listRecentRepairRequests(UserRole role, Long accountId, int limit);
}