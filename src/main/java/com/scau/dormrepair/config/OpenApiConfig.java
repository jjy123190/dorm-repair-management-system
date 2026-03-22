package com.scau.dormrepair.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 基础配置。
 * 作用是让前端和测试同学可以直接从在线文档查看接口。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    /**
     * 生成 OpenAPI 文档的基础信息。
     * 后续如果项目名、版本号变化，优先改这里。
     * @return OpenAPI 配置对象
     */
    public OpenAPI dormRepairOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("宿舍报修与工单管理系统 API")
                        .description("面向学生、宿舍管理员、维修人员的基础后端骨架")
                        .version("v0.0.1"));
    }
}
