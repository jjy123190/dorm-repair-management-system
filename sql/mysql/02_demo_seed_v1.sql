USE dorm_repair_db;

DELETE FROM work_order_records
WHERE work_order_id IN (
    SELECT id FROM (
        SELECT id
        FROM work_orders
        WHERE work_order_no IN ('WODEMO20260330001', 'WODEMO20260330002')
    ) seed_work_orders
);

DELETE FROM repair_feedbacks
WHERE repair_request_id IN (
    SELECT id FROM (
        SELECT id
        FROM repair_requests
        WHERE request_no IN ('RRDEMO20260330003')
    ) seed_feedback_requests
);

INSERT INTO repair_requests (
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
    completed_at
)
SELECT
    'RRDEMO20260330001',
    student.id,
    student.display_name,
    student.phone,
    NULL,
    'TAISHAN',
    '1-BLD',
    '207',
    'ELECTRICITY',
    'Dorm ceiling light is fully out and the room is too dark at night.',
    'SUBMITTED',
    NULL,
    NULL,
    1,
    '2026-03-27 09:10:00',
    NULL
FROM user_accounts student
WHERE student.username = 'student01'
ON DUPLICATE KEY UPDATE
    student_id = VALUES(student_id),
    student_name = VALUES(student_name),
    contact_phone = VALUES(contact_phone),
    dorm_area_snapshot = VALUES(dorm_area_snapshot),
    building_no_snapshot = VALUES(building_no_snapshot),
    room_no_snapshot = VALUES(room_no_snapshot),
    fault_category = VALUES(fault_category),
    description = VALUES(description),
    status = VALUES(status),
    reviewer_id = VALUES(reviewer_id),
    worker_id = VALUES(worker_id),
    urge_count = VALUES(urge_count),
    submitted_at = VALUES(submitted_at),
    completed_at = VALUES(completed_at);

INSERT INTO repair_requests (
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
    completed_at
)
SELECT
    'RRDEMO20260330002',
    student.id,
    student.display_name,
    student.phone,
    NULL,
    'YANSHAN',
    '4-BLD',
    '305',
    'WATER_PIPE',
    'Bathroom faucet keeps leaking and the noise affects sleeping.',
    'IN_PROGRESS',
    admin.id,
    worker.id,
    0,
    '2026-03-28 10:20:00',
    NULL
FROM user_accounts student
JOIN user_accounts admin ON admin.username = 'admin01'
JOIN user_accounts worker ON worker.username = 'worker01'
WHERE student.username = 'student01'
ON DUPLICATE KEY UPDATE
    student_id = VALUES(student_id),
    student_name = VALUES(student_name),
    contact_phone = VALUES(contact_phone),
    dorm_area_snapshot = VALUES(dorm_area_snapshot),
    building_no_snapshot = VALUES(building_no_snapshot),
    room_no_snapshot = VALUES(room_no_snapshot),
    fault_category = VALUES(fault_category),
    description = VALUES(description),
    status = VALUES(status),
    reviewer_id = VALUES(reviewer_id),
    worker_id = VALUES(worker_id),
    urge_count = VALUES(urge_count),
    submitted_at = VALUES(submitted_at),
    completed_at = VALUES(completed_at);

INSERT INTO repair_requests (
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
    completed_at
)
SELECT
    'RRDEMO20260330003',
    student.id,
    student.display_name,
    student.phone,
    NULL,
    'QILIN',
    '2-BLD',
    '410',
    'NETWORK',
    'Network wall port was down all day and was confirmed on multiple devices.',
    'COMPLETED',
    admin.id,
    worker.id,
    0,
    '2026-03-21 15:30:00',
    '2026-03-22 11:15:00'
FROM user_accounts student
JOIN user_accounts admin ON admin.username = 'admin01'
JOIN user_accounts worker ON worker.username = 'worker01'
WHERE student.username = 'student01'
ON DUPLICATE KEY UPDATE
    student_id = VALUES(student_id),
    student_name = VALUES(student_name),
    contact_phone = VALUES(contact_phone),
    dorm_area_snapshot = VALUES(dorm_area_snapshot),
    building_no_snapshot = VALUES(building_no_snapshot),
    room_no_snapshot = VALUES(room_no_snapshot),
    fault_category = VALUES(fault_category),
    description = VALUES(description),
    status = VALUES(status),
    reviewer_id = VALUES(reviewer_id),
    worker_id = VALUES(worker_id),
    urge_count = VALUES(urge_count),
    submitted_at = VALUES(submitted_at),
    completed_at = VALUES(completed_at);

INSERT INTO repair_requests (
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
    completed_at
)
SELECT
    'RRDEMO20260330004',
    student.id,
    student.display_name,
    student.phone,
    NULL,
    'TAISHAN',
    '6-BLD',
    '118',
    'PUBLIC_AREA',
    'Public corridor light flickers repeatedly and affects night safety.',
    'SUBMITTED',
    NULL,
    NULL,
    0,
    '2026-03-29 19:40:00',
    NULL
FROM user_accounts student
WHERE student.username = 'student01'
ON DUPLICATE KEY UPDATE
    student_id = VALUES(student_id),
    student_name = VALUES(student_name),
    contact_phone = VALUES(contact_phone),
    dorm_area_snapshot = VALUES(dorm_area_snapshot),
    building_no_snapshot = VALUES(building_no_snapshot),
    room_no_snapshot = VALUES(room_no_snapshot),
    fault_category = VALUES(fault_category),
    description = VALUES(description),
    status = VALUES(status),
    reviewer_id = VALUES(reviewer_id),
    worker_id = VALUES(worker_id),
    urge_count = VALUES(urge_count),
    submitted_at = VALUES(submitted_at),
    completed_at = VALUES(completed_at);

INSERT INTO work_orders (
    work_order_no,
    repair_request_id,
    admin_id,
    worker_id,
    status,
    priority,
    assignment_note,
    assigned_at,
    accepted_at,
    completed_at
)
SELECT
    'WODEMO20260330001',
    request.id,
    admin.id,
    worker.id,
    'IN_PROGRESS',
    'HIGH',
    'Handle the leaking faucet with high priority.',
    '2026-03-28 10:40:00',
    '2026-03-28 11:00:00',
    NULL
FROM repair_requests request
JOIN user_accounts admin ON admin.username = 'admin01'
JOIN user_accounts worker ON worker.username = 'worker01'
WHERE request.request_no = 'RRDEMO20260330002'
ON DUPLICATE KEY UPDATE
    repair_request_id = VALUES(repair_request_id),
    admin_id = VALUES(admin_id),
    worker_id = VALUES(worker_id),
    status = VALUES(status),
    priority = VALUES(priority),
    assignment_note = VALUES(assignment_note),
    assigned_at = VALUES(assigned_at),
    accepted_at = VALUES(accepted_at),
    completed_at = VALUES(completed_at);

INSERT INTO work_orders (
    work_order_no,
    repair_request_id,
    admin_id,
    worker_id,
    status,
    priority,
    assignment_note,
    assigned_at,
    accepted_at,
    completed_at
)
SELECT
    'WODEMO20260330002',
    request.id,
    admin.id,
    worker.id,
    'COMPLETED',
    'NORMAL',
    'Recheck the wall network port and confirm recovery.',
    '2026-03-21 16:00:00',
    '2026-03-21 16:20:00',
    '2026-03-22 11:15:00'
FROM repair_requests request
JOIN user_accounts admin ON admin.username = 'admin01'
JOIN user_accounts worker ON worker.username = 'worker01'
WHERE request.request_no = 'RRDEMO20260330003'
ON DUPLICATE KEY UPDATE
    repair_request_id = VALUES(repair_request_id),
    admin_id = VALUES(admin_id),
    worker_id = VALUES(worker_id),
    status = VALUES(status),
    priority = VALUES(priority),
    assignment_note = VALUES(assignment_note),
    assigned_at = VALUES(assigned_at),
    accepted_at = VALUES(accepted_at),
    completed_at = VALUES(completed_at);

INSERT INTO work_order_records (work_order_id, operator_id, status, record_note)
SELECT work_order.id, admin.id, 'ASSIGNED', 'Admin assigned the job and is waiting for worker acceptance.'
FROM work_orders work_order
JOIN user_accounts admin ON admin.username = 'admin01'
WHERE work_order.work_order_no = 'WODEMO20260330001';

INSERT INTO work_order_records (work_order_id, operator_id, status, record_note)
SELECT work_order.id, worker.id, 'ACCEPTED', 'Worker accepted the work order.'
FROM work_orders work_order
JOIN user_accounts worker ON worker.username = 'worker01'
WHERE work_order.work_order_no = 'WODEMO20260330001';

INSERT INTO work_order_records (work_order_id, operator_id, status, record_note)
SELECT work_order.id, worker.id, 'IN_PROGRESS', 'Worker is currently replacing the faulty part.'
FROM work_orders work_order
JOIN user_accounts worker ON worker.username = 'worker01'
WHERE work_order.work_order_no = 'WODEMO20260330001';

INSERT INTO work_order_records (work_order_id, operator_id, status, record_note)
SELECT work_order.id, admin.id, 'ASSIGNED', 'Admin assigned the network repair order.'
FROM work_orders work_order
JOIN user_accounts admin ON admin.username = 'admin01'
WHERE work_order.work_order_no = 'WODEMO20260330002';

INSERT INTO work_order_records (work_order_id, operator_id, status, record_note)
SELECT work_order.id, worker.id, 'ACCEPTED', 'Worker confirmed the visit slot.'
FROM work_orders work_order
JOIN user_accounts worker ON worker.username = 'worker01'
WHERE work_order.work_order_no = 'WODEMO20260330002';

INSERT INTO work_order_records (work_order_id, operator_id, status, record_note)
SELECT work_order.id, worker.id, 'IN_PROGRESS', 'Worker located the network failure point.'
FROM work_orders work_order
JOIN user_accounts worker ON worker.username = 'worker01'
WHERE work_order.work_order_no = 'WODEMO20260330002';

INSERT INTO work_order_records (work_order_id, operator_id, status, record_note)
SELECT work_order.id, worker.id, 'COMPLETED', 'Line was restored and verified on site.'
FROM work_orders work_order
JOIN user_accounts worker ON worker.username = 'worker01'
WHERE work_order.work_order_no = 'WODEMO20260330002';

INSERT INTO repair_feedbacks (repair_request_id, rating, feedback_comment, anonymous_flag, anonymous)
SELECT request.id, 5, 'Handled quickly and the room is back to normal.', 0, b'0'
FROM repair_requests request
WHERE request.request_no = 'RRDEMO20260330003'
ON DUPLICATE KEY UPDATE
    rating = VALUES(rating),
    feedback_comment = VALUES(feedback_comment),
    anonymous_flag = VALUES(anonymous_flag),
    anonymous = VALUES(anonymous);