-- Add status tables

CREATE TABLE rpt.invoice_state
(
    id              BIGSERIAL                    NOT NULL,
    invoice_id      CHARACTER VARYING            NOT NULL,
    sequence_id     BIGINT                       NOT NULL,
    change_id       INT                          NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    status          rpt.invoice_status           NOT NULL,
    status_details  CHARACTER VARYING            NULL,
    CONSTRAINT invoice_status_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX invoice_status_idx ON rpt.invoice_state (invoice_id, sequence_id, change_id, created_at);


CREATE TABLE rpt.payment_state
(
    id                               BIGSERIAL                    NOT NULL,
    invoice_id                       CHARACTER VARYING            NOT NULL,
    sequence_id                      BIGINT                       NOT NULL,
    change_id                        INT                          NOT NULL,
    payment_id                       CHARACTER VARYING            NOT NULL,
    created_at                       TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    payment_status                   rpt.invoice_payment_status   NOT NULL,
    payment_operation_failure_class  rpt.failure_class            NULL,
    external_failure                 CHARACTER VARYING            NULL,
    external_failure_reason          CHARACTER VARYING            NULL,
    CONSTRAINT payment_status_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_status_idx ON rpt.payment_state (invoice_id, sequence_id, change_id, created_at);


CREATE TABLE rpt.refund_state
(
    id                              BIGSERIAL                    NOT NULL,
    invoice_id                      CHARACTER VARYING            NOT NULL,
    sequence_id                     BIGINT                       NOT NULL,
    change_id                       INT                          NOT NULL,
    created_at                      TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    payment_id                      CHARACTER VARYING            NOT NULL,
    refund_id                       CHARACTER VARYING            NOT NULL,
    refund_status                   rpt.refund_status            NOT NULL,
    refund_operation_failure_class  rpt.failure_class            NULL,
    external_failure                CHARACTER VARYING            NULL,
    external_failure_reason         CHARACTER VARYING            NULL,
    CONSTRAINT refund_status_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX refund_status_idx ON rpt.refund_state (invoice_id, sequence_id, change_id, created_at);


CREATE TABLE rpt.adjustment_state
(
    id                    BIGSERIAL                    NOT NULL,
    invoice_id            CHARACTER VARYING            NOT NULL,
    sequence_id           BIGINT                       NOT NULL,
    change_id             INT                          NOT NULL,
    payment_id            CHARACTER VARYING            NOT NULL,
    created_at            TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    status                rpt.adjustment_status        NOT NULL,
    CONSTRAINT adjustment_status_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX adjustment_status_idx ON rpt.adjustment_state (invoice_id, sequence_id, change_id, created_at);


-- another tables
CREATE TABLE rpt.payment_routing
(
    id              BIGSERIAL                    NOT NULL,
    invoice_id      CHARACTER VARYING            NOT NULL,
    sequence_id     BIGINT                       NOT NULL,
    change_id       INT                          NOT NULL,
    payment_id      CHARACTER VARYING            NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    provider_id     INTEGER                      NULL,
    terminal_id     INTEGER                      NULL,
    CONSTRAINT payment_route_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_route_idx ON rpt.payment_routing (invoice_id, sequence_id, change_id, created_at);


CREATE TABLE rpt.payment_short_id
(
    id                BIGSERIAL                    NOT NULL,
    invoice_id        CHARACTER VARYING            NOT NULL,
    sequence_id       BIGINT                       NOT NULL,
    change_id         INT                          NOT NULL,
    payment_id        CHARACTER VARYING            NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    payment_short_id  CHARACTER VARYING            NOT NULL,
    CONSTRAINT payment_short_id_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_short_id_idx ON rpt.payment_short_id (invoice_id, sequence_id, change_id, created_at);


CREATE TABLE rpt.payment_cost
(
    id                BIGSERIAL                    NOT NULL,
    invoice_id        CHARACTER VARYING            NOT NULL,
    sequence_id       BIGINT                       NOT NULL,
    change_id         INT                          NOT NULL,
    payment_id        CHARACTER VARYING            NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    origin_amount     BIGINT                       NULL,
    amount            BIGINT                       NOT NULL,
    currency          CHARACTER VARYING            NOT NULL,
    CONSTRAINT payment_cost_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_cost_idx ON rpt.payment_cost (invoice_id, sequence_id, change_id, created_at);


CREATE TYPE rpt.cash_flow_account AS ENUM ('merchant', 'provider', 'system', 'external', 'wallet');
CREATE TYPE rpt.payment_change_type AS ENUM ('payment', 'refund', 'adjustment', 'payout');
CREATE TYPE rpt.adjustment_cash_flow_type AS ENUM ('new_cash_flow', 'old_cash_flow_inverse');

CREATE TABLE rpt.cash_flow(
    id                                 BIGSERIAL                      NOT NULL,
    invoice_id                         CHARACTER VARYING              NOT NULL,
    sequence_id                        BIGINT                         NOT NULL,
    change_id                          INT                            NOT NULL,
    payment_id                         CHARACTER VARYING              NOT NULL,
    refund_id                          CHARACTER VARYING              NULL,
    created_at                         TIMESTAMP WITHOUT TIME ZONE    NOT NULL,
    obj_type                           rpt.payment_change_type        NOT NULL,
    adj_flow_type                      rpt.adjustment_cash_flow_type  NULL,
    source_account_type                rpt.cash_flow_account          NOT NULL,
    source_account_type_value          CHARACTER VARYING              NOT NULL,
    source_account_id                  BIGINT                         NOT NULL,
    destination_account_type           rpt.cash_flow_account          NOT NULL,
    destination_account_type_value     CHARACTER VARYING              NOT NULL,
    destination_account_id             BIGINT                         NOT NULL,
    amount                             BIGINT                         NOT NULL,
    currency_code                      CHARACTER VARYING              NOT NULL,
    details                            CHARACTER VARYING              NULL,
    CONSTRAINT cash_flow_pkey PRIMARY KEY (id)
);
CREATE INDEX cash_flow_idx ON rpt.cash_flow (invoice_id, sequence_id, change_id, created_at);

-- delete columns

ALTER TABLE rpt.invoice DROP COLUMN invoice_status;
ALTER TABLE rpt.invoice DROP COLUMN invoice_status_details;

ALTER TABLE rpt.payment DROP COLUMN payment_status;
ALTER TABLE rpt.payment DROP COLUMN payment_operation_failure_class;
ALTER TABLE rpt.payment DROP COLUMN payment_external_failure;
ALTER TABLE rpt.payment DROP COLUMN payment_external_failure_reason;
ALTER TABLE rpt.payment DROP COLUMN payment_provider_id;
ALTER TABLE rpt.payment DROP COLUMN payment_terminal_id;
ALTER TABLE rpt.payment DROP COLUMN payment_cash_flow;
ALTER TABLE rpt.payment DROP COLUMN payment_short_id;
ALTER TABLE rpt.payment DROP COLUMN payment_amount;
ALTER TABLE rpt.payment DROP COLUMN payment_origin_amount;
ALTER TABLE rpt.payment DROP COLUMN payment_currency_code;

ALTER TABLE rpt.refund DROP COLUMN refund_status;
ALTER TABLE rpt.refund DROP COLUMN refund_operation_failure_class;
ALTER TABLE rpt.refund DROP COLUMN refund_external_failure;
ALTER TABLE rpt.refund DROP COLUMN refund_external_failure_reason;
ALTER TABLE rpt.refund DROP COLUMN refund_cash_flow;


ALTER TABLE rpt.adjustment DROP COLUMN adjustment_status;
ALTER TABLE rpt.adjustment DROP COLUMN adjustment_status_created_at;

