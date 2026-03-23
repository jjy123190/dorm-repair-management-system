package com.scau.dormrepair.common;

import com.scau.dormrepair.config.AppProperties;
import com.scau.dormrepair.config.DatabaseConfig;
import com.scau.dormrepair.service.DashboardService;
import com.scau.dormrepair.service.RepairRequestService;
import com.scau.dormrepair.service.StatisticsService;
import com.scau.dormrepair.service.WorkOrderService;
import com.scau.dormrepair.service.impl.DashboardServiceImpl;
import com.scau.dormrepair.service.impl.RepairRequestServiceImpl;
import com.scau.dormrepair.service.impl.StatisticsServiceImpl;
import com.scau.dormrepair.service.impl.WorkOrderServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * 应用上下文负责集中装配配置、数据库和业务服务。
 * 桌面端没有 Spring 容器，这里相当于手写一个最小可控的依赖注入入口。
 */
public final class AppContext implements AutoCloseable {

    private final AppProperties properties;
    private final HikariDataSource dataSource;
    private final SqlSessionFactory sqlSessionFactory;
    private final MyBatisExecutor myBatisExecutor;
    private final DashboardService dashboardService;
    private final RepairRequestService repairRequestService;
    private final WorkOrderService workOrderService;
    private final StatisticsService statisticsService;

    private AppContext(
            AppProperties properties,
            HikariDataSource dataSource,
            SqlSessionFactory sqlSessionFactory,
            MyBatisExecutor myBatisExecutor
    ) {
        this.properties = properties;
        this.dataSource = dataSource;
        this.sqlSessionFactory = sqlSessionFactory;
        this.myBatisExecutor = myBatisExecutor;
        this.dashboardService = new DashboardServiceImpl(myBatisExecutor);
        this.repairRequestService = new RepairRequestServiceImpl(myBatisExecutor);
        this.workOrderService = new WorkOrderServiceImpl(myBatisExecutor);
        this.statisticsService = new StatisticsServiceImpl(myBatisExecutor);
    }

    /**
     * 按统一入口完成配置读取和数据库装配。
     */
    public static AppContext bootstrap() {
        AppProperties properties = AppProperties.load();
        HikariDataSource dataSource = DatabaseConfig.createDataSource(properties);
        SqlSessionFactory sqlSessionFactory = DatabaseConfig.createSqlSessionFactory(dataSource);
        MyBatisExecutor myBatisExecutor = new MyBatisExecutor(sqlSessionFactory);
        return new AppContext(properties, dataSource, sqlSessionFactory, myBatisExecutor);
    }

    public AppProperties properties() {
        return properties;
    }

    public SqlSessionFactory sqlSessionFactory() {
        return sqlSessionFactory;
    }

    public DashboardService dashboardService() {
        return dashboardService;
    }

    public RepairRequestService repairRequestService() {
        return repairRequestService;
    }

    public WorkOrderService workOrderService() {
        return workOrderService;
    }

    public StatisticsService statisticsService() {
        return statisticsService;
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
