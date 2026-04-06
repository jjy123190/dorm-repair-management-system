# JavaFX 与 MyBatis 交接说明

## 1. 当前技术栈

- 桌面端：`JavaFX 17.0.10`
- 数据访问：`MyBatis 3.5.17`
- 连接池：`HikariCP 5.1.0`
- 数据库：`MySQL 8.4`
- JDK 目标版本：`17`
- 构建工具：`Maven`

## 2. 先看哪些文件

- 启动入口：[DormRepairApplication.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/DormRepairApplication.java)
- 主工作台：[AppShell.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/ui/AppShell.java)
- 登录页：[LoginView.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/ui/LoginView.java)
- 配置读取：[AppProperties.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/config/AppProperties.java)
- 数据源与 MyBatis 装配：[DatabaseConfig.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/config/DatabaseConfig.java)
- 应用上下文：[AppContext.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/common/AppContext.java)
- 会话上下文：[AppSession.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/common/AppSession.java)
- 事务模板：[MyBatisExecutor.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/common/MyBatisExecutor.java)
- SQL 映射目录：`src/main/resources/mapper/`

## 3. 启动顺序

1. 启动 `DormRepairApplication`
2. 读取 `application.yml`
3. `DatabaseConfig` 创建 `HikariDataSource` 和 `SqlSessionFactory`
4. `AppContext` 装配各个 service
5. `AppShell` 根据当前登录状态渲染 `LoginView` 或工作台
6. 各业务模块再通过 service 拉取数据并渲染

## 4. 分层怎么接

### UI 层

- 位置：`com.scau.dormrepair.ui`、`com.scau.dormrepair.ui.module`
- 只负责界面布局、按钮事件、输入提示、表格和时间线展示
- 不允许直接打开 `SqlSession`
- 不允许在界面类里手写 SQL

### Service 层

- 位置：`com.scau.dormrepair.service`、`com.scau.dormrepair.service.impl`
- 负责业务流程、状态流转、事务边界、权限判断和异常抛出
- UI 想新增功能，先补 service 接口，再补实现

### Mapper 层

- 位置：`com.scau.dormrepair.mapper`
- XML 位置：`src/main/resources/mapper`
- 一个 Mapper 对应一类表或一类统计主题
- SQL 统一写 XML，不要散落到 JavaFX 代码里

### Domain 层

- `domain.command`：界面传给 service 的入参对象
- `domain.entity`：与数据表字段基本一一对应
- `domain.enums`：状态、角色、优先级、故障分类
- `domain.view`：表格、统计面板和时间线展示对象

## 5. 标准开发链路

新增功能时，按这个顺序走：

1. 先确认表和字段
2. 再补 `entity / enum / command / view`
3. 写 Mapper 接口
4. 写 Mapper XML
5. 在 `service` 定义能力
6. 在 `service.impl` 完成流程和事务
7. 最后接到 JavaFX 模块界面

不要反着来，不要先写界面再补底层。

## 6. 当前模块对应关系

| 功能模块 | JavaFX 模块类 | 主要服务 |
| --- | --- | --- |
| 登录与账号入口 | `LoginView`、`ProfileCenterModule` | `UserAccountService` |
| 首页概览 | `DashboardModule` | `DashboardService` |
| 学生报修 | `StudentRepairModule` | `RepairRequestService`、`DormCatalogService` |
| 学生历史 | `StudentRepairHistoryModule` | `RepairRequestService`、`WorkOrderService` |
| 管理员派单 | `AdminDispatchModule` | `RepairRequestService`、`WorkOrderService` |
| 账号管理 | `AccountManagementModule` | `UserAccountService` |
| 宿舍目录维护 | `DormCatalogManagementModule` | `DormCatalogService` |
| 维修处理 | `WorkerProcessingModule` | `WorkOrderService` |
| 月度统计 | `StatisticsModule` | `StatisticsService` |
| 审计日志 | `AuditLogModule` | `AuditLogService` |

## 7. 现在已经接好的主链路

- 真实数据库账号登录与角色切换：`UserAccountService`
- 学生自助注册、手机号找回密码、个人中心改密
- 报修申请创建、历史列表、补图、删图、评价、返修：`RepairRequestService`
- 工单派单、接单、处理、完工、待确认闭环：`WorkOrderService`
- 首页概览查询：`DashboardService`
- 宿舍区/楼栋/房间目录维护：`DormCatalogService`
- 内部账号维护：`UserAccountService`
- 审计日志查询：`AuditLogService`
- 月度统计与图表数据：`StatisticsService`

这说明当前主链已经不是“待补骨架”，而是可运行、可测试、可演示的完整桌面应用。

## 8. 当前默认配置

- 数据库地址：`jdbc:mysql://127.0.0.1:3306/dorm_repair_db`
- 用户名：`root`
- 密码：`123456`
- 配置文件：[application.yml](/D:/1ForCode/SCAU/DB/src/main/resources/application.yml)
- 初始化脚本：
  - `sql/mysql/01_init_schema_v1.sql`
  - `sql/mysql/02_demo_seed_v1.sql`

## 9. 常见禁区

- 不要再新增 `controller / dto / repository`
- 不要把旧 `docs/api/` 当成当前开发规范
- 不要在 UI 里直接操作数据库
- 不要把业务判断塞进 Mapper XML
- 不要随意改枚举英文值，否则会影响数据库和状态流转
- 不要把认证逻辑退回 `DemoAccountDirectory` 本地假账号

## 10. 小组分工建议

### A. 桌面壳层与视觉

- 主要改 `ui`、`ui.component`、`ui.support`、`styles/app.css`
- 负责登录页、AppShell、公共交互和主题统一

### B. 学生报修主链

- 主要改 `StudentRepairModule`、`StudentRepairHistoryModule`、`RepairRequestService`
- 负责学生提交、历史详情、补图、评价、返修交互

### C. 管理员 / 维修员 / 统计主链

- 主要改 `AdminDispatchModule`、`WorkerProcessingModule`、`StatisticsModule`
- 负责派单、处理、完工凭证和统计展示

### D. 数据库 / 文档 / 测试基线

- 主要改 `sql/mysql`、`docs/**`、公共配置与测试
- 负责 DDL、种子数据、课程文档和最终收口

## 11. 建议下一步

1. 继续把课程交付材料收全，包括 ER 图、关键截图和答辩 PPT
2. 继续完善 demo seed，让 fresh DB 的三类角色演示更顺滑
3. 如需继续收尾，优先保证文档、DDL、测试基线和真实实现保持一致
