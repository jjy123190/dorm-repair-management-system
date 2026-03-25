package com.scau.dormrepair.common;

import com.scau.dormrepair.config.AppProperties;
import com.scau.dormrepair.config.DatabaseConfig;
import com.scau.dormrepair.config.SchemaCompatibilitySupport;
import com.scau.dormrepair.service.DashboardService;
import com.scau.dormrepair.service.DormCatalogService;
import com.scau.dormrepair.service.RepairRequestService;
import com.scau.dormrepair.service.StatisticsService;
import com.scau.dormrepair.service.WorkOrderService;
import com.scau.dormrepair.service.impl.DashboardServiceImpl;
import com.scau.dormrepair.service.impl.DormCatalogServiceImpl;
import com.scau.dormrepair.service.impl.RepairRequestServiceImpl;
import com.scau.dormrepair.service.impl.StatisticsServiceImpl;
import com.scau.dormrepair.service.impl.WorkOrderServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * 应用上下文。
 * 桌面端没有 Spring 容器，这里手动装配配置、数据库和服务实例。
 */
public final class AppContext implements AutoCloseable {

    private final AppProperties properties;
    private final HikariDataSource dataSource;
    private final SqlSessionFactory sqlSessionFactory;
    private final MyBatisExecutor myBatisExecutor;
    private final AppSession appSession;
    private final DashboardService dashboardService;
    private final DormCatalogService dormCatalogService;
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
        this.appSession = new AppSession();
        this.dashboardService = new DashboardServiceImpl(myBatisExecutor);
        this.dormCatalogService = new DormCatalogServiceImpl(myBatisExecutor);
        this.repairRequestService = new RepairRequestServiceImpl(myBatisExecutor);
        this.workOrderService = new WorkOrderServiceImpl(myBatisExecutor);
        this.statisticsService = new StatisticsServiceImpl(myBatisExecutor);
    }

    public static AppContext bootstrap() {
        AppProperties properties = AppProperties.load();
        HikariDataSource dataSource = DatabaseConfig.createDataSource(properties);
        SchemaCompatibilitySupport.repair(dataSource);
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

    public AppSession appSession() {
        return appSession;
    }

    public DashboardService dashboardService() {
        return dashboardService;
    }

    public DormCatalogService dormCatalogService() {
        return dormCatalogService;
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
