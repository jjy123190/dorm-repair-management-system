package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import java.util.List;

/**
 * 首页概览服务。
 */
public interface DashboardService {

    DashboardOverview loadOverview();

    List<RecentRepairRequestView> listRecentRepairRequests(int limit);
}
