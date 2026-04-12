# 宿舍报修与工单管理系统

当前主线已切换为 `JavaFX + MyBatis + MySQL` 桌面应用，用来突出数据库课设里的 SQL、事务、状态流转和统计查询能力，不再继续走普通的 `Spring Boot Web` 同质化路线。

## 当前技术路线

- 开发语言：`Java 17`
- 桌面框架：`JavaFX 17.0.10`
- 数据访问：`MyBatis 3.5.17`
- 连接池：`HikariCP 5.1.0`
- 数据库：`MySQL 8.4`
- 构建工具：`Maven 3.9+`

## 版本结论

- 正式目标运行环境固定为 `JDK 17`
- 当前仓库源码按 `release 17` 编译
- 这台机器实际用 `Java 24.0.1` 做了编译和测试，但这不是交付时承诺环境
- MySQL 本地默认配置继续统一为：
  - host：`127.0.0.1`
  - port：`3306`
  - database：`dorm_repair_db`
  - username：`root`
  - password：`123456`

## 当前已经落地

- JavaFX 主工作台，包含 `首页概览 / 学生报修 / 学生报修记录 / 管理员派单 / 维修处理 / 月度统计` 六个模块页
- 登录页已改成“角色 + 稳定账号”入口，不再依赖手填姓名演示身份
- `MyBatis + HikariCP` 基础装配
- `command / entity / mapper / service / ui` 分层骨架
- 报修创建、评价提交、派单、工单状态更新的 service 事务骨架
- 月度统计和首页概览查询 SQL
- MySQL 最小表设计文档与 DDL 草案
- 老师原始资料归档

## 运行方式

### 1. 数据库准备

先确认本机 MySQL 能用 `root / 123456` 连接，然后依次执行：

```sql
source sql/mysql/01_init_schema_v1.sql;
source sql/mysql/02_seed_demo_data.sql;
```

第二个脚本会刷新一组保留的演示数据，和当前登录页里的稳定账号保持一致，方便直接演示：

- `1001 张三`、`1002 李晓雨`、`1003 相逢的`
- `2001 李老师`、`2002 陈老师`
- `3001 王师傅`、`3002 周师傅`、`3003 陈师傅`
- 当前月会自动准备“已提交 / 已派单 / 处理中 / 已完成 / 已取消”几种典型状态，首页和月度统计可以直接出数

### 2. 编译

```bash
mvn compile
```

如果当前机器命令行没有 `mvn`，先安装 `Maven 3.9+`，或在 IDE 里指定可用的 Maven 再执行下面几条命令。

### 3. 运行桌面端

```bash
mvn javafx:run
```

### 4. 运行测试

```bash
mvn test
```

## 协作入口

- 协作根基：`项目协作根基.md`
- 协作历史记录：`ABCD协作历史记录.md`
- 文档索引：`docs/README.md`
- 协作约束：`AGENTS.md`
- 架构说明：`docs/architecture/系统架构与协作约定.md`
- 交接说明：`docs/architecture/JavaFX与MyBatis交接说明.md`
- 包分类号：`docs/architecture/软件包分层与分类号.md`
- 桌面端模块约定：`docs/architecture/桌面端模块与服务约定.md`
- 起步待办：`docs/architecture/起步问题与待办.md`
- 数据库设计：`docs/database/MySQL最小表设计.md`
- MySQL DDL：`sql/mysql/01_init_schema_v1.sql`
- 演示数据：`sql/mysql/02_seed_demo_data.sql`
- 老师资料：`docs/teacher-materials/README.md`

## 协作前提

- 每次开始改之前，先执行 `git pull origin main`，确保本地代码最新
- `pull` 完后先读：
  - `项目协作根基.md`
  - `ABCD协作历史记录.md`
- 凡是改动，结束前必须把本次进度和改动文件写进 `ABCD协作历史记录.md`

## 代码结构

```text
src
├─ main
│  ├─ java
│  │  └─ com
│  │     └─ scau
│  │        └─ dormrepair
│  │           ├─ common
│  │           │  ├─ AppContext.java
│  │           │  ├─ BusinessNumberGenerator.java
│  │           │  └─ MyBatisExecutor.java
│  │           ├─ config
│  │           │  ├─ AppProperties.java
│  │           │  └─ DatabaseConfig.java
│  │           ├─ domain
│  │           │  ├─ command
│  │           │  ├─ entity
│  │           │  ├─ enums
│  │           │  └─ view
│  │           ├─ exception
│  │           │  ├─ BusinessException.java
│  │           │  └─ ResourceNotFoundException.java
│  │           ├─ mapper
│  │           ├─ service
│  │           │  └─ impl
│  │           ├─ ui
│  │           │  ├─ AppShell.java
│  │           │  └─ module
│  │           └─ DormRepairApplication.java
│  └─ resources
│     ├─ application.yml
│     ├─ mapper
│     │  └─ *.xml
│     └─ styles
│        └─ app.css
└─ test
   └─ java
      └─ com
         └─ scau
            └─ dormrepair
               ├─ common
               └─ config
```

## 分层说明

- `common`：桌面端手写应用上下文、事务模板、业务编号生成
- `config`：读取 `application.yml`，装配数据源和 MyBatis
- `domain.command`：界面层调用 service 时传入的命令对象
- `domain.entity`：与数据库表直接对应的实体
- `domain.enums`：角色、状态、优先级、故障类型枚举
- `domain.view`：工作台表格和统计面板使用的视图对象
- `mapper`：只写 SQL 和结果映射
- `service`：定义业务能力
- `service.impl`：负责事务、流程、状态同步
- `ui`：JavaFX 工作台和模块页面

## 历史资料说明

- `docs/api/` 目录仅保留为早期方案归档，当前开发一律以桌面端架构和数据库文档为准
