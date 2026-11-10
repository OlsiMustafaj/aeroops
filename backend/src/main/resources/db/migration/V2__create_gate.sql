CREATE TABLE gate (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    airport_id     BIGINT      NOT NULL REFERENCES airport (id) ON DELETE RESTRICT,
    code           VARCHAR(10) NOT NULL,
    type           VARCHAR(10) NOT NULL,
    size_category  VARCHAR(1)  NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_gate_airport_code UNIQUE (airport_id, code),
    CONSTRAINT ck_gate_code CHECK (code ~ '^[A-Z0-9]{1,10}$'),
    CONSTRAINT ck_gate_type CHECK (type IN ('CONTACT', 'REMOTE')),
    CONSTRAINT ck_gate_size CHECK (size_category IN ('A', 'B', 'C', 'D', 'E', 'F'))
);

CREATE INDEX ix_gate_airport ON gate (airport_id);
