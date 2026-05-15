-- ============================================================
-- V4: Phase 2 - Flow design support
-- ============================================================

-- Add BPMN XML storage and deployment tracking to process template
ALTER TABLE biz_process_template
    ADD COLUMN bpmn_xml TEXT NULL COMMENT '草稿状态的BPMN XML',
    ADD COLUMN published_bpmn_xml TEXT NULL COMMENT '已发布的不可变BPMN XML',
    ADD COLUMN flowable_deployment_id VARCHAR(100) NULL COMMENT 'Flowable Deployment ID';

-- Add task type for countersign/or-sign support
ALTER TABLE biz_approval_task
    ADD COLUMN task_type TINYINT NOT NULL DEFAULT 1 COMMENT '1=普通 2=会签 3=或签' AFTER task_name;

-- Process node configuration table (separates designer config from BPMN XML)
CREATE TABLE IF NOT EXISTS biz_process_node_config (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id          BIGINT       NOT NULL,
    node_id              VARCHAR(100) NOT NULL COMMENT 'BPMN element ID',
    node_name            VARCHAR(200) NULL,
    node_type            VARCHAR(50)  NOT NULL COMMENT 'userTask, exclusiveGateway, parallelGateway, startEvent, endEvent',
    assignee_type        VARCHAR(50)  NULL COMMENT 'fixed, deptLeader, role, initiator, expression',
    assignee_config      TEXT         NULL COMMENT 'JSON: {"userId":1} or {"roleId":2} or {"expression":"..."}',
    multi_instance_type  VARCHAR(20)  NULL COMMENT 'none, countersign, orSign',
    completion_ratio     DECIMAL(5,2) NULL COMMENT 'null=all, 0.6=60%',
    sort_order           INT          NOT NULL DEFAULT 0,
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_template_node (template_id, node_id),
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程节点配置';
