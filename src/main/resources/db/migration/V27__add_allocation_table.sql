CREATE TABLE rpt.allocation
(
    id                                BIGSERIAL                   NOT NULL,
    payment_id                        CHARACTER VARYING           NOT NULL,
    party_id                          CHARACTER VARYING           NOT NULL,
    shop_id                           CHARACTER VARYING           NOT NULL,
    amount                            BIGINT                      NOT NULL,
    currency_code                     CHARACTER VARYING           NOT NULL,

    fee_party_id                      CHARACTER VARYING,
    fee_shop_id                       CHARACTER VARYING,
    total_amount                      BIGINT,
    total_currency_code               CHARACTER VARYING,
    fee_amount                        BIGINT,
    fee_currency_code                 CHARACTER VARYING,
    parts                             DECIMAL,

    status                            rpt.invoice_payment_status  NOT NULL,
    status_created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at                        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT allocation_pkey PRIMARY KEY (id),
    FOREIGN KEY (payment_id) REFERENCES rpt.payment (id)
);
CREATE INDEX allocation_payment_id_idx ON rpt.allocation (payment_id);
CREATE INDEX allocation_created_at_idx ON rpt.allocation (created_at);
CREATE INDEX allocation_created_at_and_status_idx ON rpt.allocation (status, created_at);
