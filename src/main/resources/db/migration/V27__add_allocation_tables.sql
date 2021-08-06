CREATE TABLE IF NOT EXISTS rpt.allocation_payment
(
    id                                BIGSERIAL                   NOT NULL,
    invoice_id                        CHARACTER VARYING           NOT NULL,
    payment_id                        CHARACTER VARYING           NOT NULL,
    ext_allocation_id                 CHARACTER VARYING           NOT NULL,
    party_id                          CHARACTER VARYING           NOT NULL,
    shop_id                           CHARACTER VARYING           NOT NULL,
    amount                            BIGINT                      NOT NULL,
    currency_code                     CHARACTER VARYING           NOT NULL,
    status                            rpt.invoice_payment_status  NOT NULL,
    status_created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at                        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT allocation_payment_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS allocation_payment_complex_id_idx
ON rpt.allocation_payment (invoice_id, payment_id, ext_allocation_id);
CREATE INDEX IF NOT EXISTS allocation_payment_created_at_idx ON rpt.allocation_payment (created_at);
CREATE INDEX IF NOT EXISTS allocation_payment_created_at_and_status_idx ON rpt.allocation_payment (status, created_at);

CREATE TABLE IF NOT EXISTS rpt.allocation_payment_details
(
    id                    BIGSERIAL NOT NULL,
    allocation_payment_id BIGINT    NOT NULL,
    fee_party_id          CHARACTER VARYING,
    fee_shop_id           CHARACTER VARYING,
    total_amount          BIGINT,
    total_currency_code   CHARACTER VARYING,
    fee_amount            BIGINT,
    fee_currency_code     CHARACTER VARYING,
    parts                 DECIMAL,
    CONSTRAINT allocation_payment_details_pkey PRIMARY KEY (id),
    FOREIGN KEY (allocation_payment_id) REFERENCES rpt.allocation_payment (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS allocation_payment_details_allocation_payment_id_idx
ON rpt.allocation_payment_details (allocation_payment_id);

ALTER TABLE rpt.payment ADD COLUMN IF NOT EXISTS allocation_payment BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS rpt.allocation_refund
(
    id                                BIGSERIAL                   NOT NULL,
    invoice_id                        CHARACTER VARYING           NOT NULL,
    payment_id                        CHARACTER VARYING           NOT NULL,
    refund_id                         CHARACTER VARYING           NOT NULL,
    ext_allocation_id                 CHARACTER VARYING           NOT NULL,
    party_id                          CHARACTER VARYING           NOT NULL,
    shop_id                           CHARACTER VARYING           NOT NULL,
    amount                            BIGINT                      NOT NULL,
    currency_code                     CHARACTER VARYING           NOT NULL,
    status                            rpt.invoice_payment_status  NOT NULL,
    status_created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at                        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT allocation_refund_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS allocation_refund_complex_id_idx
ON rpt.allocation_refund (invoice_id, payment_id, refund_id, ext_allocation_id);
CREATE INDEX IF NOT EXISTS allocation_refund_created_at_idx ON rpt.allocation_refund (created_at);
CREATE INDEX IF NOT EXISTS allocation_refund_created_at_and_status_idx ON rpt.allocation_refund (status, created_at);

CREATE TABLE IF NOT EXISTS rpt.allocation_refund_details
(
    id                    BIGSERIAL NOT NULL,
    allocation_refund_id  BIGINT    NOT NULL,
    fee_party_id          CHARACTER VARYING,
    fee_shop_id           CHARACTER VARYING,
    total_amount          BIGINT,
    total_currency_code   CHARACTER VARYING,
    fee_amount            BIGINT,
    fee_currency_code     CHARACTER VARYING,
    parts                 DECIMAL,
    CONSTRAINT allocation_refund_details_pkey PRIMARY KEY (id),
    FOREIGN KEY (allocation_refund_id) REFERENCES rpt.allocation_refund (id)
    );
CREATE UNIQUE INDEX IF NOT EXISTS allocation_refund_details_allocation_refund_id_idx
ON rpt.allocation_refund_details (allocation_refund_id);

ALTER TABLE rpt.refund ADD COLUMN IF NOT EXISTS allocation_refund BOOLEAN NOT NULL DEFAULT FALSE;
