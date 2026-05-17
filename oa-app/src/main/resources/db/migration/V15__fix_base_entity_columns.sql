-- V15: Add missing BaseEntity columns (updated_at, deleted) to biz_audit_log, biz_notification, biz_process_node_config

ALTER TABLE biz_audit_log
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;

ALTER TABLE biz_notification
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;

ALTER TABLE biz_process_node_config
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;
