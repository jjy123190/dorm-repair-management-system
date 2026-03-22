# 宿舍报修与工单管理系统

基于 Spring Boot + Vue + MySQL 的宿舍报修与工单管理系统，面向学生、宿舍管理员和维修人员三类用户，支持学生报修、管理员派单、维修员处理及数据统计分析。

## 参考仓库结论

参考仓库 `jjy123190/dorm-repair-management-system` 当前只有一段 README 说明：

> 基于 SpringBoot + Vue + MySQL 的宿舍报修与工单管理系统。支持学生报修、管理员派单、维修员处理及数据统计分析。

因此本目录直接搭建后端基础骨架，不依赖上游现成代码。

## 版本定稿

当前项目按下面这组版本收口：

- Spring Boot：`3.2.12`
- JDK：`17 LTS`
- MySQL Server：`8.4 LTS`
- Maven：`3.6.3+`

## 为什么这样定

### 1. Spring Boot 版本边界

根据 Spring Boot 3.2.12 官方文档，`Spring Boot 3.2.12 requires at least Java 17 and is compatible with versions up to and including Java 23`。

所以从 Spring Boot 自己的支持矩阵看：

- 最低可用 JDK：`17`
- 当前项目选择 JDK：`17`
- 官方兼容上限：`23`

### 2. MySQL 驱动兼容边界

根据 MySQL Connector/J 官方文档：

- 当前 Connector/J `9.6` 官方兼容说明是：
  - 支持 `MySQL 8.0 and up`
  - 支持 `JRE 8 or higher`

这意味着从 JDBC 驱动层面看，`JDK 17 + MySQL 8.4` 完全在官方兼容范围内。

项目里不会手写锁死 `mysql-connector-j` 的具体小版本，而是继续让 Spring Boot 的依赖管理接管。

### 3. 为什么不直接用当前机器的 Java 24

这台机器当前默认 Java 是 `24.0.1`。但 Spring Boot 3.2.12 官方写明兼容到 `Java 23` 为止。

结论：

- `Java 24` 不是这套项目的推荐运行版本
- 项目标准运行环境固定为 `JDK 17`
- 如果只是临时编译，JDK 24 可能能过，但这不属于我现在要写进 README 的正式兼容承诺

## 推荐组合

最稳方案：

- `JDK 17 LTS`
- `Spring Boot 3.2.12`
- `MySQL 8.4 LTS`

这是一个基于官方文档推出来的稳定组合。这里的“推荐”是我的工程判断；依据是 Spring Boot 的 Java 支持矩阵，以及 MySQL Connector/J 对 JRE/MySQL Server 的兼容说明。

## 当前已落地的基础范围

- Spring Boot 后端骨架
- MySQL + JPA 基础配置
- 宿舍、报修单、工单、维修记录、评价等核心实体
- 学生报修、管理员派单、工单查询、维修员更新状态、评价反馈、月度统计的接口骨架
- Swagger/OpenAPI 文档入口
- `docs/` 下的接口文档、数据库设计草案、课程文档和老师资料归档

## 角色范围

- 学生：提交报修、查看进度、提交评价
- 管理员：审核、派单、催办、查看统计
- 维修人员：接单、更新处理状态、提交维修说明

## 运行方式

### 开发环境要求

- 本项目推荐安装 `JDK 17`
- 本项目推荐安装 `MySQL 8.4`
- 本项目需要 `Maven 3.6.3+`

### 数据库准备

1. 准备 MySQL 数据库，例如 `dorm_repair_db`
2. 当前仓库的本地开发配置已经写死为：
   - host：`127.0.0.1`
   - port：`3306`
   - database：`dorm_repair_db`
   - username：`root`
   - password：`123456`
3. 如果你同事 pull 下来后连不上，不要先怀疑代码，先确认他本机 MySQL root 密码是不是 `123456`

### 启动命令

```bash
mvn spring-boot:run
```

## 协作入口

- 文档总索引：`docs/README.md`
- 协作约束：`AGENTS.md`
- 接口规范：`docs/api/接口规范.md`
- 接口清单：`docs/api/接口清单.md`
- 接口联调示例：`docs/api/接口联调示例.http`
- MySQL 最小表设计：`docs/database/MySQL最小表设计.md`
- MySQL DDL 草案：`sql/mysql/01_init_schema_v1.sql`
- 老师资料索引：`docs/teacher-materials/README.md`

## 文档入口

- 根路径：`http://localhost:8082/`，会自动跳转到 Swagger UI
- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`

## 代码结构

项目主代码按 `com.scau.dormrepair` 作为根包组织，推荐前后端和数据库同学都先按这棵树理解项目：

```text
src
├─ main
│  ├─ java
│  │  └─ com
│  │     └─ scau
│  │        └─ dormrepair
│  │           ├─ common
│  │           │  ├─ ApiResponse.java
│  │           │  └─ PageResponse.java
│  │           ├─ config
│  │           │  └─ OpenApiConfig.java
│  │           ├─ domain
│  │           │  ├─ entity
│  │           │  │  ├─ BaseTimeEntity.java
│  │           │  │  ├─ DormRoom.java
│  │           │  │  ├─ RepairFeedback.java
│  │           │  │  ├─ RepairRecord.java
│  │           │  │  ├─ RepairRequest.java
│  │           │  │  ├─ UserAccount.java
│  │           │  │  └─ WorkOrder.java
│  │           │  └─ enums
│  │           │     ├─ FaultCategory.java
│  │           │     ├─ RepairRequestStatus.java
│  │           │     ├─ UserRole.java
│  │           │     ├─ WorkOrderPriority.java
│  │           │     └─ WorkOrderStatus.java
│  │           ├─ exception
│  │           │  ├─ GlobalExceptionHandler.java
│  │           │  └─ ResourceNotFoundException.java
│  │           ├─ repository
│  │           │  ├─ DormRoomRepository.java
│  │           │  ├─ RepairFeedbackRepository.java
│  │           │  ├─ RepairRecordRepository.java
│  │           │  ├─ RepairRequestRepository.java
│  │           │  ├─ UserAccountRepository.java
│  │           │  └─ WorkOrderRepository.java
│  │           ├─ service
│  │           │  ├─ DormRoomService.java
│  │           │  ├─ RepairRequestService.java
│  │           │  ├─ StatisticsService.java
│  │           │  ├─ WorkOrderService.java
│  │           │  └─ impl
│  │           │     ├─ DormRoomServiceImpl.java
│  │           │     ├─ RepairRequestServiceImpl.java
│  │           │     ├─ StatisticsServiceImpl.java
│  │           │     └─ WorkOrderServiceImpl.java
│  │           ├─ web
│  │           │  ├─ controller
│  │           │  │  ├─ DormRoomController.java
│  │           │  │  ├─ HomeController.java
│  │           │  │  ├─ RepairFeedbackController.java
│  │           │  │  ├─ RepairRequestController.java
│  │           │  │  ├─ StatisticsController.java
│  │           │  │  └─ WorkOrderController.java
│  │           │  └─ dto
│  │           │     ├─ AssignWorkOrderCommand.java
│  │           │     ├─ CreateRepairRequestCommand.java
│  │           │     ├─ DormRoomResponse.java
│  │           │     ├─ MonthlyStatisticsResponse.java
│  │           │     ├─ RepairRequestDetailResponse.java
│  │           │     ├─ RepairRequestSummaryResponse.java
│  │           │     ├─ SaveDormRoomCommand.java
│  │           │     ├─ SubmitRepairFeedbackCommand.java
│  │           │     ├─ UpdateWorkOrderStatusCommand.java
│  │           │     └─ WorkOrderResponse.java
│  │           └─ DormRepairApplication.java
│  └─ resources
│     └─ application.yml
└─ test
   ├─ java
   │  └─ com
   │     └─ scau
   │        └─ dormrepair
   │           └─ DormRepairApplicationTests.java
   └─ resources
      └─ application-test.yml
```

各层职责固定如下：

- `common`：统一响应体、分页结构这类全项目复用对象。
- `config`：Swagger、Spring Boot 运行配置。
- `domain/entity`：数据库实体，和表结构一一对应。
- `domain/enums`：状态、角色、故障类型、优先级等枚举常量。
- `exception`：统一异常与全局异常处理。
- `repository`：JPA 数据访问层，直接和数据库交互。
- `service`：业务接口层，定义宿舍、报修、工单、统计等业务能力。
- `service/impl`：业务接口的具体实现，写流程和状态流转逻辑。
- `web/controller`：REST 接口入口，负责接收请求、返回 JSON。
- `web/dto`：请求体和响应体对象，给前端联调用。
- `resources/application.yml`：本地开发默认配置。
- `test`：基础冒烟测试，先保证应用能启动、根路径能跳转、健康检查可用。

## 官方依据

- Spring Boot 3.2.12 Reference: https://docs.spring.io/spring-boot/docs/3.2.12/reference/htmlsingle/
- Spring Boot 3.2.12 System Requirements: https://docs.spring.io/spring-boot/docs/3.2.12/reference/htmlsingle/#getting-started.system-requirements
- MySQL Connector/J Compatibility: https://dev.mysql.com/doc/connector-j/en/connector-j-versions.html
- MySQL 8.4 Reference Manual: https://dev.mysql.com/doc/refman/8.4/en/
- MySQL 8.4 安装版本说明: https://dev.mysql.com/doc/refman/8.4/en/which-version.html
