CREATE TABLE editor_schema.catalog
(
    version TEXT PRIMARY KEY,
    catalog JSONB       NOT NULL,
    created TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE editor_schema.session
(
    id              TEXT PRIMARY KEY,
    pull_token_hash TEXT        NOT NULL,
    catalog         TEXT        NOT NULL REFERENCES editor_schema.catalog (version),
    payload         JSONB       NOT NULL,
    problems        JSONB       NOT NULL DEFAULT '[]',
    state           TEXT        NOT NULL,
    created         TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires         TIMESTAMPTZ NOT NULL
);

CREATE INDEX session_expires ON editor_schema.session (expires);
