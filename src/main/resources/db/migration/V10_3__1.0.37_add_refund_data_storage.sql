CREATE TYPE rpt.refund_status AS ENUM ('PENDING', 'SUCCEEDED', 'FAILED');

CREATE TABLE rpt.refund
(
    id                         BIGSERIAL                   NOT NULL,
    party_id                   CHARACTER VARYING           NOT NULL,
    shop_id                    CHARACTER VARYING           NOT NULL,
    invoice_id                 CHARACTER VARYING           NOT NULL,
    payment_id                 CHARACTER VARYING           NOT NULL,
    refund_id                  CHARACTER VARYING           NOT NULL,
    created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    amount                     BIGINT                      NOT NULL,
    currency_code              CHARACTER VARYING           NOT NULL,
    reason                     CHARACTER VARYING,
    status                     rpt.refund_status           NOT NULL,
    status_created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    fee                        BIGINT,
    provider_fee               BIGINT,
    external_fee               BIGINT,
    CONSTRAINT refund_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX refund_id_idx on rpt.refund (invoice_id, payment_id, refund_id);
CREATE UNIQUE INDEX refund_created_at_idx ON rpt.refund (created_at);
CREATE UNIQUE INDEX refund_created_at_and_status_idx ON rpt.refund (status, created_at);


CREATE TABLE rpt.refund_additional_info
(
    ext_refund_id                         BIGINT                   NOT NULL,
    operation_failure_class               rpt.failure_class           NULL,
    external_failure                      CHARACTER VARYING           NULL,
    external_failure_reason               CHARACTER VARYING           NULL,
    domain_revision                       BIGINT,
    party_revision                        BIGINT,
    CONSTRAINT refund_additional_pkey PRIMARY KEY (ext_refund_id)
)
