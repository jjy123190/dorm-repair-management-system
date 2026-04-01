# 宿舍报修与工单管理系统

当前主线为 `JavaFX + MyBatis + MySQL` 桌面应用，聚焦课程设计里的登录鉴权、报修工单流转、统计查询和后台资料维护，不再走普通 `Spring Boot Web CRUD` 路线。

## 当前技术路线

- 开发语言：`Java 17`
- 桌面框架：`JavaFX 17.0.10`
- 数据访问：`MyBatis 3.5.17`
- 连接池：`HikariCP 5.1.0`
- 数据库：`MySQL 8.4`
- 构建工具：`Maven 3.9+`

## 本地默认配置

- host：`127.0.0.1`
- port：`3306`
- database：`dorm_repair_db`
- username：`root`
- password：`123456`
- 主应用入口：`com.scau.dormrepair.DormRepairApplication`
- 关键配置：`src/main/resources/application.yml`

## 当前已落地功能

- 登录、学生自助注册、手机号找回密码、个人中心改密与基础资料维护
- 学生提交报修、上传图片、查看历史记录、催办、取消、提交评价
- 管理员派单、内部用户管理、宿舍目录维护、月度统计查看
- 维修员待处理工单筛选、接单、处理中、完工状态更新
- 学生取消已派单报修时，服务层同步关闭关联工单并写入工单记录
- 学生历史记录、首页统计和详情归属统一按 `student_id` 判断，不再依赖当前姓名
- 个人中心支持学生与内部账号最小自助资料维护：姓名、手机号
- 内部用户管理支持创建、编辑姓名/手机号/角色、启停用、管理员重置密码
- 宿舍目录维护支持管理员维护楼栋、房间与房间启停用状态
- 月度统计支持表格、近六个月趋势图和故障分类占比图
- 工单追踪闭环已落地：学生历史、管理员派单、维修员处理三端都可回看时间线与评价

## 数据兼容说明

- 当前仓库默认通过 `SchemaCompatibilitySupport` 修复本地旧表差异。
- `repair_requests` 兼容旧字段并补齐快照列。
- `dorm_rooms` 若仍残留旧版 `building_no`、`campus_name` 非空列，启动时会放宽为可空；Mapper 写入也会同步回填旧列，保证不同同学本地库都能跑。
- 种子账号至少保留 1 个启用学生、管理员、维修员，默认密码基线为 `123456` 的 SHA-256。

## 初始化与运行

### 1. 初始化数据库

先确保本机 MySQL 可用，然后执行：

```sql
source sql/mysql/01_init_schema_v1.sql;
source sql/mysql/02_demo_seed_v1.sql;
```

### 2. 编译

```bash
mvn compile
```

### 3. 运行桌面端

```bash
mvn javafx:run
```

### 4. 运行测试

```bash
mvn test
```

当前测试基线：`mvn test` 通过 `25` 个测试。

## 协作入口

- 项目协作根基：`项目协作根基.md`
- 协作历史记录：`ABCD协作历史记录.md`
- 协作约束：`AGENTS.md`
- 文档索引：`docs/README.md`
- 架构说明：`docs/architecture/系统架构与协作约定.md`
- MySQL DDL：`sql/mysql/01_init_schema_v1.sql`
- Demo seed：`sql/mysql/02_demo_seed_v1.sql`

## 协作前提

- 开始改动前先检查工作树；如果本地已有脏改动，不要盲目执行 `git pull origin main`。
- 本地工作树可安全同步时，再拉取最新代码并继续开发。
- 任意改动结束前，都要把本轮进度与改动文件写进 `ABCD协作历史记录.md`。

## 分层说明

- `ui`：JavaFX 页面、布局、按钮事件和静态表格展示
- `service.impl`：业务流程、状态流转、事务一致性和权限边界
- `mapper`：SQL 和结果映射，不堆业务判断
- `domain.command`：界面层提交给服务层的命令对象
- `domain.entity` / `domain.view`：实体与工作台展示对象

## 当前重点

- 继续收后台模块的一致性问题，优先是筛选区、静态表格和详情区交互体验。
- 继续完善 demo seed，让 fresh DB 启动后三种角色页面都能直接看到非空数据。
- 继续优化后台高频页面的文案质量和文档一致性，避免演示时出现乱码或过期说明。