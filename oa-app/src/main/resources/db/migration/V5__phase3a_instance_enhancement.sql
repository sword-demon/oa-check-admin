-- Phase 3A: Instance enhancement for my-applications and dashboard stats
-- Add composite index for my-applications query
CREATE INDEX idx_instance_initiator_status ON biz_approval_instance (initiator_user_id, status);
