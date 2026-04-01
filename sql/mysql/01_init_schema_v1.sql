-- 閺佺増宓佹惔鎾冲灥婵瀵查懘姘拱閿涘澊1 閼藉顢嶉敍?-- 閻╊喗鐖ｉ敍姘辩舶鐠囧墽鈻肩拋鎹愵吀娑撯偓娑擃亜褰查惄瀛樺复閽€钘夌氨閵嗕礁鍟€闁劖顒炵紒鍡楀閻ㄥ嫭娓剁亸蹇曞閺堫兙鈧?
CREATE DATABASE IF NOT EXISTS dorm_repair_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE dorm_repair_db;

-- 閻劍鍩涚悰顭掔窗鐎涳妇鏁撻妴浣侯吀閻炲棗鎲抽妴浣烘樊娣囶喖鎲抽崗杈╂暏娑撯偓瀵姾澶勯崣鐤€冮妴?CREATE TABLE IF NOT EXISTS user_accounts (
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

-- 濡ゅ吋鐖х悰顭掔窗鏉╂瑩鍣烽幎濠傤問閼稿秴灏惄瀛樺复閽€钘夋躬 campus_name閿涘瞼鏅棃銏犵湴缂佺喍绔寸仦鏇犮仛娑撹　鈧粌顔栭懜宥呭隘閳ユ縿鈧?CREATE TABLE IF NOT EXISTS dorm_buildings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    campus_name VARCHAR(64) NOT NULL,
    building_no VARCHAR(32) NOT NULL,
    building_name VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dorm_building (campus_name, building_no)
);

-- 閹村潡妫跨悰顭掔窗瑜版挸澧犳稉濠氭懠娴犲秶鍔ч崗浣筋啅閹村潡妫块崣閿嬪婵夘偓绱濋幍鈧禒銉ㄧ箹瀵姾銆冮崗鍫滅稊娑撳搫鎮楃紒顓涒偓婊冾問閼稿秴鐔€绾偓鐠у嫭鏋＄紒瀛樺Б閳ユ繄娈戦幍鈺佺潔閻愰€涚箽閻ｆ瑣鈧?CREATE TABLE IF NOT EXISTS dorm_rooms (
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

-- 閹躲儰鎱ㄩ崡鏇°€冮敍姘箽閻ｆ瑥顔栭懜宥呭隘/濡ゅ吋鐖?閹村潡妫胯箛顐ゅ弾閿涘矂浼╅崗宥呮倵闂堛垺銈奸弽瀣カ閺傛瑨鐨熼弫瀛樻閹跺﹤宸婚崣鑼额唶瑜版洖鐢稊渚库偓?CREATE TABLE IF NOT EXISTS repair_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_no VARCHAR(64) NOT NULL UNIQUE,
    student_id BIGINT NULL,
    student_name VARCHAR(64) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    dorm_room_id BIGINT NULL,
    dorm_area_snapshot VARCHAR(64) NOT NULL,
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

-- 閹躲儰鎱ㄩ崶鍓у鐞涱煉绱板锝呯础鐎圭偟骞囬弮鏈电瑝瀵ら缚顔呴幎濠傤樋瀵姴娴橀悧鍥毐閺堢喎顢ｆ潻娑楃娑擃亝鏋冮張顒€鐡у▓鐐光偓?CREATE TABLE IF NOT EXISTS repair_request_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    repair_request_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_no INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_repair_request_images_request_id (repair_request_id),
    CONSTRAINT fk_request_images_request FOREIGN KEY (repair_request_id) REFERENCES repair_requests(id)
);

-- 瀹搞儱宕熺悰顭掔窗娑撯偓閺夆剝濮ゆ穱顔煎礋閺堚偓婢舵碍娣抽悽鐔稿灇娑撯偓瀵姴浼愰崡鏇樷偓?CREATE TABLE IF NOT EXISTS work_orders (
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

-- 瀹搞儱宕熸径鍕倞鐠佹澘缍嶇悰顭掔窗鐠佹澘缍嶅ú鎯у礋閵嗕焦甯撮崡鏇樷偓浣割槱閻炲棔鑵戦妴浣哥暚瀹搞儳鐡戦崝銊ょ稊濞翠焦鎸夐妴?CREATE TABLE IF NOT EXISTS work_order_records (
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

-- 鐠囧嫪鐜悰顭掔窗娑撯偓閺夆剝濮ゆ穱顔煎礋閺堚偓婢舵矮绔撮弶陇鐦庢禒鏋偓?CREATE TABLE IF NOT EXISTS work_order_completion_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_no INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_work_order_completion_images_work_order_id (work_order_id),
    CONSTRAINT fk_work_order_completion_images_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NULL,
    action_type VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(64) NULL,
    target_label VARCHAR(255) NULL,
    old_value TEXT NULL,
    new_value TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_logs_operator_id (operator_id),
    KEY idx_audit_logs_created_at (created_at),
    CONSTRAINT fk_audit_logs_operator FOREIGN KEY (operator_id) REFERENCES user_accounts(id)
);

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

-- 鐎硅儻鍨楅崠?/ 濡ゅ吋鐖ч崺铏诡攨鐠у嫭鏋￠敍? 娑擃亜顔栭懜宥呭隘閿涘本鐦￠崠?15 閺嶅銈奸妴?INSERT IGNORE INTO dorm_buildings (campus_name, building_no, building_name)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 15
),
areas AS (
    SELECT '濞夋澘鍖楅崠? AS campus_name
    UNION ALL SELECT '閸楀骸鍖楅崠?
    UNION ALL SELECT '閸氼垱鐏勯崠?
    UNION ALL SELECT '姒涙垵鍖楅崠?
    UNION ALL SELECT '閻曟洖鍖楅崠?
)
SELECT
    areas.campus_name,
    CONCAT(seq.n, '閺?) AS building_no,
    CONCAT(areas.campus_name, ' ', seq.n, '閺?) AS building_name
FROM areas
CROSS JOIN seq;

INSERT INTO user_accounts (username, password_hash, display_name, phone, role_code, enabled)
VALUES
    ('student01', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '张三', '13800138001', 'STUDENT', 1),
    ('admin01', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '李老师', '13800138101', 'ADMIN', 1),
    ('worker01', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '王师傅', '13800138201', 'WORKER', 1)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    display_name = VALUES(display_name),
    phone = VALUES(phone),
    role_code = VALUES(role_code),
    enabled = VALUES(enabled);