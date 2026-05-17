-- V9: Add approval instance binding to leave request
ALTER TABLE biz_leave_request ADD COLUMN approval_instance_id BIGINT NULL COMMENT '关联审批实例ID' AFTER status;

CREATE INDEX idx_approval_instance ON biz_leave_request (approval_instance_id);
