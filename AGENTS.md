# AGENTS.md

本仓库当前主线是 `JavaFX + MyBatis + MySQL` 桌面应用。人和 Codex 协作时，统一按下面的约束继续。

## 1. 项目定位

- 项目名称：宿舍报修与工单管理系统
- 当前阶段：桌面应用基础架构 + 数据库设计 + 课程文档整理
- 当前目标：给小组成员提供一套清晰、稳定、可继续补功能的桌面端骨架

## 2. 先看哪些文件

- 项目总入口：[README.md](README.md)
- 文档总索引：[docs/README.md](docs/README.md)
- 架构说明：[docs/architecture/系统架构与协作约定.md](docs/architecture/系统架构与协作约定.md)
- 交接说明：[docs/architecture/JavaFX与MyBatis交接说明.md](docs/architecture/JavaFX与MyBatis交接说明.md)
- 桌面端模块约定：[docs/architecture/桌面端模块与服务约定.md](docs/architecture/桌面端模块与服务约定.md)
- 包分类号：[docs/architecture/软件包分层与分类号.md](docs/architecture/软件包分层与分类号.md)
- MySQL 最小表设计：[docs/database/MySQL最小表设计.md](docs/database/MySQL最小表设计.md)
- MySQL DDL 草案：[sql/mysql/01_init_schema_v1.sql](sql/mysql/01_init_schema_v1.sql)
- 老师资料索引：[docs/teacher-materials/README.md](docs/teacher-materials/README.md)

## 3. 代码结构

当前 Java 包按下面的桌面端分层组织：

- `com.scau.dormrepair.common`：应用上下文、事务模板、编号生成
- `com.scau.dormrepair.config`：配置读取、数据源与 MyBatis 装配
- `com.scau.dormrepair.domain.command`：业务命令对象
- `com.scau.dormrepair.domain.entity`：实体
- `com.scau.dormrepair.domain.enums`：枚举
- `com.scau.dormrepair.domain.view`：工作台展示对象
- `com.scau.dormrepair.exception`：业务异常
- `com.scau.dormrepair.mapper`：SQL 访问层
- `com.scau.dormrepair.service`：业务接口
- `com.scau.dormrepair.service.impl`：业务实现
- `com.scau.dormrepair.ui`：JavaFX 主工作台
- `com.scau.dormrepair.ui.module`：模块页面

## 4. 分层约束

- `ui` 只负责界面布局、按钮事件、表格展示
- `service.impl` 负责业务流程、状态流转、事务一致性
- `mapper` 只写 SQL，不堆业务判断
- `domain.entity` 只做数据承载，不塞流程逻辑
- 新增业务时，优先沿用 `command -> service -> mapper -> table` 这条链

## 5. 数据库约束

- 当前推荐最小落地方案仍然是 `8` 张核心表
- 表名统一 `snake_case`
- 时间字段统一：`created_at`、`updated_at`
- 状态类字段统一用 `varchar` 存枚举码
- 图片正式落库走 `repair_request_images`，不要再退回单字段拼接
- 当前仓库默认本地连接：`root / 123456 / dorm_repair_db`

## 6. 当前明确未完成项

- 登录鉴权未落地
- 文件上传未落地
- 宿舍基础信息维护界面未落地
- 用户管理界面未落地
- 图表化统计和导出未落地
- 初始化测试数据脚本未补

## 7. 旧方案说明

- `docs/api/` 仅作历史归档，不再作为当前实现依据
- 当前主线不要再新增 `controller / dto / repository` 那套 Web 分层
- 继续扩展时，统一在 `ui`、`service`、`mapper` 这三层往下写
