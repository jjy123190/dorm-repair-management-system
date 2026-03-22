# AGENTS.md

本仓库给人和 Codex 的统一协作约束如下。

## 1. 项目定位

- 项目名称：宿舍报修与工单管理系统
- 当前阶段：后端骨架 + 接口契约 + 数据库设计草案 + 课程文档整理
- 当前目标：为前端同事、数据库同事、文档同事提供一套清晰且稳定的起步结构

## 2. 先看哪些文件

- 项目总入口：[README.md](README.md)
- 文档索引：[docs/README.md](docs/README.md)
- 架构说明：[docs/architecture/系统架构与协作约定.md](docs/architecture/系统架构与协作约定.md)
- 包分类号：[docs/architecture/软件包分层与分类号.md](docs/architecture/软件包分层与分类号.md)
- 接口规范：[docs/api/接口规范.md](docs/api/接口规范.md)
- 接口清单：[docs/api/接口清单.md](docs/api/接口清单.md)
- MySQL 最小表设计：[docs/database/MySQL最小表设计.md](docs/database/MySQL最小表设计.md)
- MySQL DDL 草案：[sql/mysql/01_init_schema_v1.sql](sql/mysql/01_init_schema_v1.sql)
- 老师资料索引：[docs/teacher-materials/README.md](docs/teacher-materials/README.md)

## 3. 代码结构

当前 Java 包按分层组织，不要随意打散：

- `com.scau.dormrepair.common`：统一返回结构、分页结构
- `com.scau.dormrepair.config`：Spring 配置
- `com.scau.dormrepair.domain.entity`：实体
- `com.scau.dormrepair.domain.enums`：枚举
- `com.scau.dormrepair.exception`：异常处理
- `com.scau.dormrepair.repository`：数据访问
- `com.scau.dormrepair.service`：服务接口
- `com.scau.dormrepair.service.impl`：服务实现
- `com.scau.dormrepair.web.controller`：控制器
- `com.scau.dormrepair.web.dto`：接口输入输出对象

如果后续代码量继续增加，再拆成按业务模块分包；在当前规模下，优先保持稳定，不要为了“更优雅”重构到前端和数据库同事接不上。

## 4. 接口约束

- 所有接口统一使用 `/api/v1`
- 路径统一用复数资源名和 `kebab-case`
- 新增用 `POST`
- 查询详情用 `GET /{id}`
- 分页查询用 `GET`
- 更新状态用 `PATCH /{id}/status`
- 删除用 `DELETE /{id}`
- 分页参数统一：`page` 从 `1` 开始，`size` 默认 `20`
- 列表响应统一返回 `PageResponse`

任何人改接口时，必须同时更新：

1. `docs/api/接口规范.md`
2. `docs/api/接口清单.md`
3. 若字段变更，更新 `docs/database/MySQL最小表设计.md`

## 5. 数据库约束

- 当前推荐的 MySQL 最小落地方案是 `8` 张表
- 不要再把图片长期塞在一个大文本字段里，数据库正式落地时要拆 `repair_request_images`
- 表名统一 `snake_case`
- 时间字段统一：`created_at`、`updated_at`
- 状态类字段统一使用 `varchar` 存枚举码
- 当前仓库本地开发默认数据库连接写死为：`root / 123456 / dorm_repair_db`

## 6. 当前明确未完成项

- 登录鉴权未落地
- 文件上传未落地
- MySQL 正式建库脚本还只是 v1 草案
- Flyway / Liquibase 未接入
- 单元测试和集成测试未补
- 前端页面未开始

## 7. 协作原则

- 不要改老师原始 `.doc` 文件内容，原件只归档
- 不要改接口名后只在口头说明，必须落文档
- 不要让前端根据 Spring 的 `Page` 默认结构对接，统一按 `PageResponse`
- 不要把数据库设计只停留在 ER 图口头描述，表和字段必须落到 `docs/database` 和 `sql/mysql`
