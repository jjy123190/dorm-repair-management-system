package com.scau.dormrepair.config;

import com.scau.dormrepair.mapper.DashboardMapper;
import com.scau.dormrepair.mapper.DormBuildingMapper;
import com.scau.dormrepair.mapper.RepairFeedbackMapper;
import com.scau.dormrepair.mapper.RepairRequestImageMapper;
import com.scau.dormrepair.mapper.RepairRequestMapper;
import com.scau.dormrepair.mapper.StatisticsMapper;
import com.scau.dormrepair.mapper.WorkOrderMapper;
import com.scau.dormrepair.mapper.WorkOrderRecordMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

/**
 * 负责 DataSource 和 MyBatis 工厂的基础装配。
 */
public final class DatabaseConfig {

    private static final String[] MAPPER_XMLS = {
            "mapper/DashboardMapper.xml",
            "mapper/DormBuildingMapper.xml",
            "mapper/RepairRequestMapper.xml",
            "mapper/RepairRequestImageMapper.xml",
            "mapper/RepairFeedbackMapper.xml",
            "mapper/WorkOrderMapper.xml",
            "mapper/WorkOrderRecordMapper.xml",
            "mapper/StatisticsMapper.xml"
    };

    private DatabaseConfig() {
    }

    public static HikariDataSource createDataSource(AppProperties appProperties) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(appProperties.database().url());
        hikariConfig.setUsername(appProperties.database().username());
        hikariConfig.setPassword(appProperties.database().password());
        hikariConfig.setDriverClassName(appProperties.database().driverClassName());
        hikariConfig.setMaximumPoolSize(8);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(5000);
        hikariConfig.setPoolName("DormRepairHikariPool");

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        validateConnection(dataSource);
        return dataSource;
    }

    public static SqlSessionFactory createSqlSessionFactory(HikariDataSource dataSource) {
        JdbcTransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("desktop", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);

        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);
        configuration.addMapper(DashboardMapper.class);
        configuration.addMapper(DormBuildingMapper.class);
        configuration.addMapper(RepairRequestMapper.class);
        configuration.addMapper(RepairRequestImageMapper.class);
        configuration.addMapper(RepairFeedbackMapper.class);
        configuration.addMapper(WorkOrderMapper.class);
        configuration.addMapper(WorkOrderRecordMapper.class);
        configuration.addMapper(StatisticsMapper.class);

        for (String mapperXml : MAPPER_XMLS) {
            loadMapperXml(configuration, mapperXml);
        }

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private static void validateConnection(HikariDataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(2)) {
                throw new IllegalStateException("数据库连接不可用");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("数据库连接失败，请检查 application.yml: " + exception.getMessage(), exception);
        }
    }

    private static void loadMapperXml(Configuration configuration, String mapperXml) {
        try (InputStream inputStream = DatabaseConfig.class.getClassLoader().getResourceAsStream(mapperXml)) {
            if (inputStream == null) {
                throw new IllegalStateException("未找到 MyBatis 映射文件: " + mapperXml);
            }

            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
                    inputStream,
                    configuration,
                    mapperXml,
                    configuration.getSqlFragments()
            );
            xmlMapperBuilder.parse();
        } catch (Exception exception) {
            throw new IllegalStateException("加载 MyBatis 映射文件失败: " + mapperXml, exception);
        }
    }
}
