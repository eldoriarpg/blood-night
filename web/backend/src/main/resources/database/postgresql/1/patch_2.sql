ALTER TABLE web_schema.editor_session
    ADD COLUMN applied_revision INTEGER;

ALTER TABLE web_schema.editor_session
    ADD COLUMN applied_at TIMESTAMPTZ;
