# MySQL最小表设计

## 1. 结论

当前项目正式落 MySQL 时，最少按 `8` 张表设计，不建议少于这个数量。

## 2. 表清单

| 序号 | 表名 | 用途 |
| --- | --- | --- |
| 1 | `user_accounts` | 用户表，存学生、管理员、维修人员 |
| 2 | `dorm_buildings` | 宿舍楼栋表 |
| 3 | `dorm_rooms` | 宿舍房间表 |
| 4 | `repair_requests` | 报修单表 |
| 5 | `repair_request_images` | 报修图片表 |
| 6 | `work_orders` | 工单表 |
| 7 | `work_order_records` | 工单处理记录表 |
| 8 | `repair_feedbacks` | 评价反馈表 |

## 3. 字段设计

### 3.1 `user_accounts`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint | PK | 主键 |
| username | varchar(64) | NOT NULL, UNIQUE | 登录账号 |
| password_hash | varchar(255) | NULL | 密码哈希，现阶段可先留空 |
| display_name | varchar(64) | NOT NULL | 显示名称 |
| phone | varchar(32) | NOT NULL | 电话 |
| role_code | varchar(32) | NOT NULL | `STUDENT/ADMIN/WORKER` |
| enabled | tinyint(1) | NOT NULL | 是否启用 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

### 3.2 `dorm_buildings`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint | PK | 主键 |
| campus_name | varchar(64) | NOT NULL | 校区 |
| building_no | varchar(32) | NOT NULL | 楼栋号 |
| building_name | varchar(64) | NULL | 楼栋名称 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

### 3.3 `dorm_rooms`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint | PK | 主键 |
| building_id | bigint | NOT NULL | 关联楼栋 ID |
| room_no | varchar(32) | NOT NULL | 房间号 |
| floor_no | int | NOT NULL | 楼层 |
| bed_count | int | NOT NULL | 床位数 |
| room_status | varchar(32) | NOT NULL | 房间状态，建议默认 `ACTIVE` |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

### 3.4 `repair_requests`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint | PK | 主键 |
| request_no | varchar(64) | NOT NULL, UNIQUE | 报修单号 |
| student_id | bigint | NULL | 学生 ID |
| student_name | varchar(64) | NOT NULL | 学生姓名 |
| contact_phone | varchar(32) | NOT NULL | 联系电话 |
| dorm_room_id | bigint | NULL | 房间 ID |
| building_no_snapshot | varchar(32) | NOT NULL | 楼栋号快照 |
| room_no_snapshot | varchar(32) | NOT NULL | 房间号快照 |
| fault_category | varchar(32) | NOT NULL | 故障类型 |
| description | text | NOT NULL | 故障描述 |
| status | varchar(32) | NOT NULL | 报修状态 |
| reviewer_id | bigint | NULL | 审核管理员 ID |
| worker_id | bigint | NULL | 维修人员 ID |
| urge_count | int | NOT NULL | 催办次数 |
| submitted_at | datetime | NOT NULL | 提交时间 |
| completed_at | datetime | NULL | 完成时间 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

### 3.5 `repair_request_images`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint | PK | 主键 |
| repair_request_id | bigint | NOT NULL | 报修单 ID |
| image_url | varchar(255) | NOT NULL | 图片 URL |
| sort_no | int | NOT NULL | 排序号 |
| created_at | datetime | NOT NULL | 创建时间 |

### 3.6 `work_orders`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint | PK | 主键 |
| work_order_no | varchar(64) | NOT NULL, UNIQUE | 工单号 |
| repair_request_id | bigint | NOT NULL, UNIQUE | 对应报修单 ID |
| admin_id | bigint | NOT NULL | 派单管理员 ID |
| worker_id | bigint | NOT NULL | 维修人员 ID |
| status | varchar(32) | NOT NULL | 工单状态 |
| priority | varchar(16) | NOT NULL | 优先级 |
| assignment_note | varchar(1000) | NULL | 派单备注 |
| assigned_at | datetime | NOT NULL | 派单时间 |
| accepted_at | datetime | NULL | 接单时间 |
| completed_at | datetime | NULL | 完成时间 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

### 3.7 `work_order_records`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint | PK | 主键 |
| work_order_id | bigint | NOT NULL | 工单 ID |
| operator_id | bigint | NOT NULL | 操作人 ID |
| status | varchar(32) | NOT NULL | 本次记录对应状态 |
| record_note | varchar(1000) | NULL | 处理说明 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

### 3.8 `repair_feedbacks`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint | PK | 主键 |
| repair_request_id | bigint | NOT NULL, UNIQUE | 报修单 ID |
| rating | tinyint | NOT NULL | 评分 1-5 |
| feedback_comment | varchar(1000) | NULL | 评价内容 |
| anonymous_flag | tinyint(1) | NOT NULL | 是否匿名 |
| created_at | datetime | NOT NULL | 创建时间 |
| updated_at | datetime | NOT NULL | 更新时间 |

## 4. 必建索引

- `user_accounts.username`
- `dorm_buildings(campus_name, building_no)`
- `dorm_rooms(building_id, room_no)`
- `repair_requests.request_no`
- `repair_requests.status`
- `repair_requests.submitted_at`
- `work_orders.work_order_no`
- `work_orders.worker_id`
- `work_orders.status`
- `repair_feedbacks.repair_request_id`

## 5. 当前 Java 桌面端骨架与正式 MySQL 设计的关系

- 当前 Java 骨架已经按 `repair_request_images` 独立表的方向建模
- 当前 Java 骨架已经按 `dorm_buildings` + `dorm_rooms` 两张表建模
- 当前还没有把宿舍基础数据维护界面和用户管理界面真正做出来
- 当前 SQL v1 草案已经足够支撑首页概览、待派单列表、活动工单和月度统计
