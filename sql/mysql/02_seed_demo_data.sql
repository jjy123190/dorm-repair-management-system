-- Demo seed data for the JavaFX + MyBatis desktop workflow.
-- Run this after sql/mysql/01_init_schema_v1.sql.
-- The script only refreshes reserved demo ids so it can be rerun safely.

USE dorm_repair_db;

START TRANSACTION;

SET @today = CURRENT_DATE();
SET @this_month_start = STR_TO_DATE(DATE_FORMAT(@today, '%Y-%m-01'), '%Y-%m-%d');
SET @last_month_start = DATE_SUB(@this_month_start, INTERVAL 1 MONTH);

-- Refresh reserved demo rows in child-to-parent order.
DELETE FROM repair_feedbacks
WHERE id IN (8001, 8002)
   OR repair_request_id IN (5001, 5006);

DELETE FROM work_order_records
WHERE id IN (7001, 7002, 7003, 7004, 7005, 7006, 7007, 7008, 7009, 7010, 7011)
   OR work_order_id IN (6001, 6002, 6003, 6006);

DELETE FROM work_orders
WHERE id IN (6001, 6002, 6003, 6006)
   OR repair_request_id IN (5001, 5002, 5003, 5006);

DELETE FROM repair_request_images
WHERE id BETWEEN 5501 AND 5504
   OR repair_request_id IN (5001, 5002, 5003, 5004, 5005, 5006);

DELETE FROM repair_requests
WHERE id IN (5001, 5002, 5003, 5004, 5005, 5006);

DELETE FROM dorm_rooms
WHERE id IN (9001, 9002, 9003, 9004, 9005, 9006);

DELETE FROM user_accounts
WHERE id IN (1001, 1002, 1003, 2001, 2002, 3001, 3002, 3003);

INSERT INTO user_accounts (
    id,
    username,
    password_hash,
    display_name,
    phone,
    role_code,
    enabled,
    created_at,
    updated_at
)
VALUES
    (1001, '1001', NULL, '张三', '13800001001', 'STUDENT', 1, NOW(), NOW()),
    (1002, '1002', NULL, '李晓雨', '13800001002', 'STUDENT', 1, NOW(), NOW()),
    (1003, '1003', NULL, '相逢的', '13800001003', 'STUDENT', 1, NOW(), NOW()),
    (2001, '2001', NULL, '李老师', '13800002001', 'ADMIN', 1, NOW(), NOW()),
    (2002, '2002', NULL, '陈老师', '13800002002', 'ADMIN', 1, NOW(), NOW()),
    (3001, '3001', NULL, '王师傅', '13800003001', 'WORKER', 1, NOW(), NOW()),
    (3002, '3002', NULL, '周师傅', '13800003002', 'WORKER', 1, NOW(), NOW()),
    (3003, '3003', NULL, '陈师傅', '13800003003', 'WORKER', 1, NOW(), NOW());

INSERT INTO dorm_rooms (
    id,
    building_id,
    room_no,
    floor_no,
    bed_count,
    room_status,
    created_at,
    updated_at
)
SELECT 9001, b.id, '101', 1, 4, 'ACTIVE', NOW(), NOW()
FROM dorm_buildings b
WHERE b.campus_name = '泰山区' AND b.building_no = '1栋'
UNION ALL
SELECT 9002, b.id, '305', 3, 4, 'ACTIVE', NOW(), NOW()
FROM dorm_buildings b
WHERE b.campus_name = '泰山区' AND b.building_no = '1栋'
UNION ALL
SELECT 9003, b.id, '412', 4, 6, 'ACTIVE', NOW(), NOW()
FROM dorm_buildings b
WHERE b.campus_name = '华山区' AND b.building_no = '2栋'
UNION ALL
SELECT 9004, b.id, '208', 2, 4, 'ACTIVE', NOW(), NOW()
FROM dorm_buildings b
WHERE b.campus_name = '启林区' AND b.building_no = '3栋'
UNION ALL
SELECT 9005, b.id, '510', 5, 6, 'ACTIVE', NOW(), NOW()
FROM dorm_buildings b
WHERE b.campus_name = '黑山区' AND b.building_no = '4栋'
UNION ALL
SELECT 9006, b.id, '602', 6, 4, 'ACTIVE', NOW(), NOW()
FROM dorm_buildings b
WHERE b.campus_name = '燕山区' AND b.building_no = '5栋';

INSERT INTO repair_requests (
    id,
    request_no,
    student_id,
    student_name,
    contact_phone,
    dorm_room_id,
    dorm_area_snapshot,
    building_no_snapshot,
    room_no_snapshot,
    fault_category,
    description,
    status,
    reviewer_id,
    worker_id,
    urge_count,
    submitted_at,
    completed_at,
    created_at,
    updated_at
)
VALUES
    (
        5001,
        'RR-DEMO-5001',
        1001,
        '张三',
        '13800001001',
        9001,
        '泰山区',
        '1栋',
        '101',
        'WATER_PIPE',
        '洗手台下方渗水，夜间会持续滴水，已经影响地面使用。',
        'COMPLETED',
        2001,
        3001,
        1,
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 1 DAY), '08:30:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 3 DAY), '18:20:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 1 DAY), '08:30:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 3 DAY), '18:20:00')
    ),
    (
        5002,
        'RR-DEMO-5002',
        1003,
        '相逢的',
        '13800001003',
        9006,
        '燕山区',
        '5栋',
        '602',
        'FURNITURE',
        '上铺床架连接件松动，翻身时异响明显，维修员已登记待配件。',
        'IN_PROGRESS',
        2002,
        3002,
        2,
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 4 DAY), '10:15:00'),
        NULL,
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 4 DAY), '10:15:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 6 DAY), '16:40:00')
    ),
    (
        5003,
        'RR-DEMO-5003',
        1002,
        '李晓雨',
        '13800001002',
        9003,
        '华山区',
        '2栋',
        '412',
        'ELECTRICITY',
        '书桌上方照明灯闪烁后无法点亮，晚上学习受影响。',
        'ASSIGNED',
        2001,
        3003,
        0,
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 7 DAY), '13:20:00'),
        NULL,
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 7 DAY), '13:20:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 7 DAY), '16:00:00')
    ),
    (
        5004,
        'RR-DEMO-5004',
        1001,
        '张三',
        '13800001001',
        9002,
        '泰山区',
        '1栋',
        '305',
        'PUBLIC_AREA',
        '走廊公共洗衣区排水缓慢，地面积水，需要管理员安排查看。',
        'SUBMITTED',
        NULL,
        NULL,
        0,
        TIMESTAMP(@today, '09:15:00'),
        NULL,
        TIMESTAMP(@today, '09:15:00'),
        TIMESTAMP(@today, '09:15:00')
    ),
    (
        5005,
        'RR-DEMO-5005',
        1002,
        '李晓雨',
        '13800001002',
        9003,
        '华山区',
        '2栋',
        '412',
        'NETWORK',
        '宿舍网口偶发掉线，后来发现是自己网线接触不良，已主动取消报修。',
        'CANCELLED',
        NULL,
        NULL,
        0,
        TIMESTAMP(DATE_SUB(@today, INTERVAL 1 DAY), '20:10:00'),
        NULL,
        TIMESTAMP(DATE_SUB(@today, INTERVAL 1 DAY), '20:10:00'),
        TIMESTAMP(DATE_SUB(@today, INTERVAL 1 DAY), '21:00:00')
    ),
    (
        5006,
        'RR-DEMO-5006',
        1003,
        '相逢的',
        '13800001003',
        9006,
        '燕山区',
        '5栋',
        '602',
        'NETWORK',
        '宿舍网口完全无信号，检查后确认是面板松脱，上个月已修复。',
        'COMPLETED',
        2002,
        3002,
        0,
        TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 12 DAY), '11:30:00'),
        TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 14 DAY), '15:30:00'),
        TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 12 DAY), '11:30:00'),
        TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 14 DAY), '15:30:00')
    );

INSERT INTO work_orders (
    id,
    work_order_no,
    repair_request_id,
    admin_id,
    worker_id,
    status,
    priority,
    assignment_note,
    assigned_at,
    accepted_at,
    completed_at,
    created_at,
    updated_at
)
VALUES
    (
        6001,
        'WO-DEMO-6001',
        5001,
        2001,
        3001,
        'COMPLETED',
        'NORMAL',
        '优先排查漏水点，处理后确认是否需要更换软管。',
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 2 DAY), '09:00:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 2 DAY), '09:40:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 3 DAY), '18:20:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 2 DAY), '09:00:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 3 DAY), '18:20:00')
    ),
    (
        6002,
        'WO-DEMO-6002',
        5002,
        2002,
        3002,
        'WAITING_PARTS',
        'HIGH',
        '先做安全加固，待床架连接件到货后再完成更换。',
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 5 DAY), '08:45:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 5 DAY), '09:20:00'),
        NULL,
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 5 DAY), '08:45:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 6 DAY), '16:40:00')
    ),
    (
        6003,
        'WO-DEMO-6003',
        5003,
        2001,
        3003,
        'ASSIGNED',
        'URGENT',
        '晚间自习受影响，今晚前先安排上门检查照明线路。',
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 7 DAY), '15:10:00'),
        NULL,
        NULL,
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 7 DAY), '15:10:00'),
        TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 7 DAY), '15:10:00')
    ),
    (
        6006,
        'WO-DEMO-6006',
        5006,
        2002,
        3002,
        'COMPLETED',
        'LOW',
        '检查网口面板与跳线，修复后确认宿舍联网恢复。',
        TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 13 DAY), '08:00:00'),
        TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 13 DAY), '10:10:00'),
        TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 14 DAY), '15:30:00'),
        TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 13 DAY), '08:00:00'),
        TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 14 DAY), '15:30:00')
    );

INSERT INTO work_order_records (
    id,
    work_order_id,
    operator_id,
    status,
    record_note,
    created_at,
    updated_at
)
VALUES
    (7001, 6001, 2001, 'ASSIGNED', '管理员已派单，要求今天内完成漏水排查。', TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 2 DAY), '09:00:00'), TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 2 DAY), '09:00:00')),
    (7002, 6001, 3001, 'ACCEPTED', '已接单，准备携带常用水管配件上门。', TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 2 DAY), '09:40:00'), TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 2 DAY), '09:40:00')),
    (7003, 6001, 3001, 'IN_PROGRESS', '已拆开洗手台下方软管接口，确认垫片老化。', TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 3 DAY), '14:10:00'), TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 3 DAY), '14:10:00')),
    (7004, 6001, 3001, 'COMPLETED', '更换软管垫片并做通水测试，现场已恢复正常。', TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 3 DAY), '18:20:00'), TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 3 DAY), '18:20:00')),
    (7005, 6002, 2002, 'ASSIGNED', '先安排上门查看床架安全风险。', TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 5 DAY), '08:45:00'), TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 5 DAY), '08:45:00')),
    (7006, 6002, 3002, 'ACCEPTED', '已接单，现场检查后先完成临时加固。', TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 5 DAY), '09:20:00'), TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 5 DAY), '09:20:00')),
    (7007, 6002, 3002, 'WAITING_PARTS', '连接件型号已登记，等待仓库补件。', TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 6 DAY), '16:40:00'), TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 6 DAY), '16:40:00')),
    (7008, 6003, 2001, 'ASSIGNED', '晚间优先处理照明故障，避免影响学生使用。', TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 7 DAY), '15:10:00'), TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 7 DAY), '15:10:00')),
    (7009, 6006, 2002, 'ASSIGNED', '安排维修员检查宿舍网络面板。', TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 13 DAY), '08:00:00'), TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 13 DAY), '08:00:00')),
    (7010, 6006, 3002, 'ACCEPTED', '已接单，开始排查面板和网口接线。', TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 13 DAY), '10:10:00'), TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 13 DAY), '10:10:00')),
    (7011, 6006, 3002, 'COMPLETED', '重新压接松脱网线并固定面板，联网恢复正常。', TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 14 DAY), '15:30:00'), TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 14 DAY), '15:30:00'));

INSERT INTO repair_feedbacks (
    id,
    repair_request_id,
    rating,
    feedback_comment,
    anonymous_flag,
    created_at,
    updated_at
)
VALUES
    (8001, 5001, 5, '维修速度快，处理后马上恢复正常用水。', 0, TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 4 DAY), '09:00:00'), TIMESTAMP(DATE_ADD(@this_month_start, INTERVAL 4 DAY), '09:00:00')),
    (8002, 5006, 4, '更换和加固后网络恢复稳定，整体处理比较及时。', 1, TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 15 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(@last_month_start, INTERVAL 15 DAY), '10:00:00'));

COMMIT;
