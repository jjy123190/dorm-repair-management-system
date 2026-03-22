package com.scau.dormrepair.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dormRepairOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("宿舍报修与工单管理系统 API")
                        .description("面向学生、宿舍管理员、维修人员的基础后端骨架")
                        .version("v0.0.1"));
    }
}
