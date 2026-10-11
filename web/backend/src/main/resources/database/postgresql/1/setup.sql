CREATE TABLE web_schema.editor_catalog
(
    version TEXT PRIMARY KEY,
    catalog JSONB       NOT NULL,
    created TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE web_schema.editor_session
(
    id              TEXT PRIMARY KEY,
    pull_token_hash TEXT        NOT NULL,
    catalog         TEXT        NOT NULL REFERENCES web_schema.editor_catalog (version),
    payload         JSONB       NOT NULL,
    problems        JSONB       NOT NULL DEFAULT '[]',
    state           TEXT        NOT NULL,
    created         TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires         TIMESTAMPTZ NOT NULL
);

CREATE INDEX session_expires ON web_schema.editor_session (expires);
