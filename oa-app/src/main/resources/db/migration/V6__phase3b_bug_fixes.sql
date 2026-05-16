-- Phase 3B: Add CC configuration to node config
ALTER TABLE biz_process_node_config
    ADD COLUMN cc_config TEXT NULL COMMENT 'JSON array of userIds to CC when this node completes';
