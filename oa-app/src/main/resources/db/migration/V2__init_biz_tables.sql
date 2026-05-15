-- ============================================================
-- V2: Business approval tables
-- ============================================================

CREATE TABLE IF NOT EXISTS biz_process_template (
    id                             BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name                  VARCHAR(200) NOT NULL,
    template_key                   VARCHAR(100) NOT NULL COMMENT '唯一标识如 leave_request',
    flowable_process_definition_id VARCHAR(100) NULL     COMMENT '关联Flowable的ProcessDefinition ID',
    form_config                    TEXT         NULL     COMMENT 'JSON表单字段定义',
    version                        INT          NOT NULL DEFAULT 1,
    status                         TINYINT      NOT NULL DEFAULT 1 COMMENT '1=草稿 2=已发布',
    created_at                     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted                        TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_template_key (template_key, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批流程模板';

CREATE TABLE IF NOT EXISTS biz_approval_instance (
    id                             BIGINT AUTO_INCREMENT PRIMARY KEY,
    process_template_id            BIGINT       NOT NULL,
    instance_title                 VARCHAR(200) NOT NULL,
    flowable_process_instance_id   VARCHAR(100) NULL     COMMENT 'Flowable ProcessInstance ID',
    initiator_user_id              BIGINT       NOT NULL,
    status                         TINYINT      NOT NULL DEFAULT 1 COMMENT '1=审批中 2=通过 3=驳回 4=撤回 5=终止',
    form_data                      TEXT         NULL     COMMENT 'JSON提交的表单数据',
    created_at                     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    end_at                         DATETIME     NULL,
    INDEX idx_template_id (process_template_id),
    INDEX idx_initiator (initiator_user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批实例';

CREATE TABLE IF NOT EXISTS biz_approval_task (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    approval_instance_id BIGINT       NOT NULL,
    flowable_task_id     VARCHAR(100) NULL     COMMENT 'Flowable Task ID',
    assignee_user_id     BIGINT       NOT NULL,
    task_name            VARCHAR(100) NOT NULL COMMENT '节点名称如 部门经理审批',
    task_result          TINYINT      NULL     COMMENT '1=通过 2=驳回 3=转办',
    task_comment         VARCHAR(500) NULL     COMMENT '审批意见',
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at         DATETIME     NULL,
    INDEX idx_instance_id (approval_instance_id),
    INDEX idx_assignee (assignee_user_id, task_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批任务';

CREATE TABLE IF NOT EXISTS biz_approval_cc (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    approval_instance_id BIGINT       NOT NULL,
    cc_user_id           BIGINT       NOT NULL,
    cc_reason            VARCHAR(500) NULL,
    read_at              DATETIME     NULL,
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_instance_id (approval_instance_id),
    INDEX idx_cc_user (cc_user_id, read_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抄送记录';

CREATE TABLE IF NOT EXISTS biz_audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    module      VARCHAR(50)  NOT NULL COMMENT '模块如 system, approval',
    action      VARCHAR(50)  NOT NULL COMMENT '操作如 create, update, approve',
    target_type VARCHAR(50)  NULL     COMMENT '目标类型如 user, role, instance',
    target_id   BIGINT       NULL,
    detail      TEXT         NULL     COMMENT 'JSON详情',
    ip          VARCHAR(50)  NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_module_action (module, action),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志';
