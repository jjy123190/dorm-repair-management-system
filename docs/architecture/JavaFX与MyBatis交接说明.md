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
- 配置读取：[AppProperties.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/config/AppProperties.java)
- 数据源与 MyBatis 装配：[DatabaseConfig.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/config/DatabaseConfig.java)
- 应用上下文：[AppContext.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/common/AppContext.java)
- 事务模板：[MyBatisExecutor.java](/D:/1ForCode/SCAU/DB/src/main/java/com/scau/dormrepair/common/MyBatisExecutor.java)
- SQL 映射目录：`src/main/resources/mapper/`

## 3. 启动顺序

1. 启动 `DormRepairApplication`
2. 读取 `application.yml`
3. `DatabaseConfig` 创建 `HikariDataSource` 和 `SqlSessionFactory`
4. `AppContext` 装配 service
5. `AppShell` 创建主窗口和左侧模块导航
6. 各模块再调用 service 拉取数据

## 4. 分层怎么接

### UI 层

- 位置：`com.scau.dormrepair.ui`、`com.scau.dormrepair.ui.module`
- 只负责界面布局、按钮事件、表格展示
- 不允许直接打开 `SqlSession`
- 不允许在界面类里手写 SQL

### Service 层

- 位置：`com.scau.dormrepair.service`、`com.scau.dormrepair.service.impl`
- 负责业务流程、状态流转、事务边界、异常抛出
- UI 想新增功能，先补 service 接口，再补实现

### Mapper 层

- 位置：`com.scau.dormrepair.mapper`
- XML 位置：`src/main/resources/mapper`
- 一个 Mapper 对应一类表或一类统计主题
- SQL 统一写 XML，不要散落到 JavaFX 代码里

### Domain 层

- `domain.command`：界面传给 service 的入参对象
- `domain.entity`：和数据表字段基本一一对应
- `domain.enums`：状态、角色、优先级、故障分类
- `domain.view`：表格和统计面板展示对象

## 5. 标准开发链路

新增一个功能时，按这个顺序走：

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
| 首页概览 | `DashboardModule` | `DashboardService` |
| 学生报修 | `StudentRepairModule` | `RepairRequestService` |
| 管理员派单 | `AdminDispatchModule` | `RepairRequestService`、`WorkOrderService` |
| 维修处理 | `WorkerProcessingModule` | `WorkOrderService` |
| 月度统计 | `StatisticsModule` | `StatisticsService` |

## 7. 现在已经接好的主链路

- 报修申请创建：`RepairRequestService`
- 评价提交：`RepairRequestService`
- 工单派单：`WorkOrderService`
- 工单状态流转：`WorkOrderService`
- 首页概览查询：`DashboardService`
- 月度统计查询：`StatisticsService`

这说明底层主链路已经有骨架，后面主要是继续把界面表单、下拉框、校验和交互补完整。

## 8. 当前默认配置

- 数据库地址：`jdbc:mysql://127.0.0.1:3306/dorm_repair_db`
- 用户名：`root`
- 密码：`123456`
- 配置文件：[application.yml](/D:/1ForCode/SCAU/DB/src/main/resources/application.yml)

## 9. 常见禁区

- 不要再新增 `controller / dto / repository`
- 不要把旧 `docs/api/` 当成当前开发规范
- 不要在 UI 里直接操作数据库
- 不要把业务判断塞进 Mapper XML
- 不要随意改枚举英文值，否则会影响数据库和状态流转

## 10. 小组分工建议

### 前端界面同学

- 主要改 `ui/module`
- 只调用 service，不碰 SQL

### 数据库同学

- 主要改 `sql/mysql`、`mapper/*.xml`、`domain.entity`
- 改表后要同步 `MySQL最小表设计.md`

### 业务逻辑同学

- 主要改 `service`、`service.impl`
- 负责状态流转、校验、事务

### 文档同学

- 主要维护 `docs/course`、`docs/architecture`
- 技术口径统一写 `JavaFX + MyBatis + MySQL`

## 11. 建议下一步

1. 先补初始化测试数据 SQL
2. 再做登录页和角色入口
3. 接着补学生报修表单和管理员派单表单
4. 最后补文件上传、图表统计和导出
