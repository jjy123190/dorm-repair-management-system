package com.scau.dormrepair;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 项目最小冒烟测试。
 * 目标是先守住“能启动、能跳转、能返回基础接口”这几条最基本的生命线。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DormRepairApplicationTests {

    /**
     * 用 MockMvc 直接访问 Spring MVC 层，避免测试里再占真实端口。
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * 根路径应该直接跳到 Swagger，避免浏览器看到 404 白页。
     */
    @Test
    void shouldRedirectRootToSwaggerUi() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui.html"));
    }

    /**
     * 健康检查接口要可用，方便本地和后续部署环境快速判断服务是否存活。
     */
    @Test
    void shouldExposeHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    /**
     * 宿舍分页接口至少要能返回统一的响应结构。
     */
    @Test
    void shouldReturnDormRoomPageResponse() throws Exception {
        mockMvc.perform(get("/api/v1/dorm-rooms")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20));
    }

    /**
     * Swagger 入口至少要存在，方便前后端同学联调时直接查接口。
     */
    @Test
    void shouldExposeSwaggerUiEntry() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/swagger-ui/index.html"));
    }
}
