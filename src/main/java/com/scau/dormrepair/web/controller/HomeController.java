package com.scau.dormrepair.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 浏览器入口控制器。
 * 作用是把没有前端首页的根路径请求，直接引导到 Swagger 文档页。
 */
@Controller
public class HomeController {

    /**
     * 访问系统根路径时，直接跳到 Swagger UI。
     * 这样同学们输入 localhost:8082 时，不会再看到 404 白页。
     *
     * @return Swagger 页面重定向地址
     */
    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/swagger-ui.html";
    }

    /**
     * 兼容部分浏览器或工具默认访问 /index.html 的情况。
     *
     * @return Swagger 页面重定向地址
     */
    @GetMapping("/index.html")
    public String redirectIndex() {
        return "redirect:/swagger-ui.html";
    }
}
