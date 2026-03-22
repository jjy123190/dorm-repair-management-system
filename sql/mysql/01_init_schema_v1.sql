-- 数据库初始化脚本（v1 草案）
-- 目标：给数据库同事一个可以直接落库、再逐步细化的最小版本。

CREATE DATABASE IF NOT EXISTS dorm_repair_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE dorm_repair_db;

-- 用户表：学生、管理员、维修人员共用一张账号表。
CREATE TABLE IF NOT EXISTS user_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NULL,
    display_name VARCHAR(64) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    role_code VARCHAR(32) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 楼栋表：把校区和楼栋信息单独抽出来，避免房间表重复存校区/楼栋基础信息。
CREATE TABLE IF NOT EXISTS dorm_buildings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    campus_name VARCHAR(64) NOT NULL,
    building_no VARCHAR(32) NOT NULL,
    building_name VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dorm_building (campus_name, building_no)
);

-- 房间表：和楼栋表形成一对多关系。
CREATE TABLE IF NOT EXISTS dorm_rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id BIGINT NOT NULL,
    room_no VARCHAR(32) NOT NULL,
    floor_no INT NOT NULL,
    bed_count INT NOT NULL,
    room_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dorm_room (building_id, room_no),
    CONSTRAINT fk_room_building FOREIGN KEY (building_id) REFERENCES dorm_buildings(id)
);

-- 报修单表：学生提交报修的主表。
CREATE TABLE IF NOT EXISTS repair_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_no VARCHAR(64) NOT NULL UNIQUE,
    student_id BIGINT NULL,
    student_name VARCHAR(64) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    dorm_room_id BIGINT NULL,
    building_no_snapshot VARCHAR(32) NOT NULL,
    room_no_snapshot VARCHAR(32) NOT NULL,
    fault_category VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    reviewer_id BIGINT NULL,
    worker_id BIGINT NULL,
    urge_count INT NOT NULL DEFAULT 0,
    submitted_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_repair_requests_status (status),
    KEY idx_repair_requests_submitted_at (submitted_at),
    CONSTRAINT fk_repair_request_room FOREIGN KEY (dorm_room_id) REFERENCES dorm_rooms(id)
);

-- 报修图片表：正式数据库设计里不建议把图片长期塞到一个文本字段中。
CREATE TABLE IF NOT EXISTS repair_request_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    repair_request_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_no INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_repair_request_images_request_id (repair_request_id),
    CONSTRAINT fk_request_images_request FOREIGN KEY (repair_request_id) REFERENCES repair_requests(id)
);

-- 工单表：一条报修单最多派生成一张工单。
CREATE TABLE IF NOT EXISTS work_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_no VARCHAR(64) NOT NULL UNIQUE,
    repair_request_id BIGINT NOT NULL UNIQUE,
    admin_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    assignment_note VARCHAR(1000) NULL,
    assigned_at DATETIME NOT NULL,
    accepted_at DATETIME NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_work_orders_worker_id (worker_id),
    KEY idx_work_orders_status (status),
    CONSTRAINT fk_work_orders_request FOREIGN KEY (repair_request_id) REFERENCES repair_requests(id)
);

-- 工单处理记录表：用来记录派单、接单、处理中、完工等动作流水。
CREATE TABLE IF NOT EXISTS work_order_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    record_note VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_work_order_records_work_order_id (work_order_id),
    CONSTRAINT fk_work_order_records_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id)
);

-- 评价表：一条报修单最多一条评价。
CREATE TABLE IF NOT EXISTS repair_feedbacks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    repair_request_id BIGINT NOT NULL UNIQUE,
    rating TINYINT NOT NULL,
    feedback_comment VARCHAR(1000) NULL,
    anonymous_flag TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_repair_feedbacks_request FOREIGN KEY (repair_request_id) REFERENCES repair_requests(id)
);
