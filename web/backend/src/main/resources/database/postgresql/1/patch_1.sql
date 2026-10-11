ALTER TABLE web_schema.editor_session
    ADD COLUMN revision INTEGER NOT NULL DEFAULT 0;

ALTER TABLE web_schema.editor_session
    ADD COLUMN initial_payload JSONB;

UPDATE web_schema.editor_session
SET initial_payload = payload;

ALTER TABLE web_schema.editor_session
    ALTER COLUMN initial_payload SET NOT NULL;

CREATE TABLE web_schema.editor_revision
(
    session_id TEXT        NOT NULL REFERENCES web_schema.editor_session (id) ON DELETE CASCADE,
    revision   INTEGER     NOT NULL,
    operations JSONB       NOT NULL,
    created    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, revision)
);

ALTER TABLE web_schema.editor_revision
    ADD COLUMN author TEXT;

CREATE TABLE web_schema.editor_participant
(
    session_id TEXT        NOT NULL REFERENCES web_schema.editor_session (id) ON DELETE CASCADE,
    token_hash TEXT        NOT NULL,
    name       TEXT        NOT NULL,
    created    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, token_hash)
);
