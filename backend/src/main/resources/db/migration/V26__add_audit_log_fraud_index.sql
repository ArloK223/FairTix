CREATE INDEX IF NOT EXISTS idx_audit_logs_user_action_created ON audit_logs (user_id, action, created_at);
