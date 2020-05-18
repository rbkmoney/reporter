CREATE TYPE rpt.invoice_payment_status AS ENUM ('PENDING', 'PROCESSED', 'CAPTURED', 'CANCELLED', 'FAILED', 'REFUNDED');
CREATE TYPE rpt.payment_tool AS ENUM ('BANK_CARD', 'PAYMENT_TERMINAL', 'DIGITAL_WALLET');
CREATE TYPE rpt.bank_card_token_provider AS ENUM ('APPLEPAY', 'GOOGLEPAY', 'SAMSUNGPAY');
CREATE TYPE rpt.payment_flow AS ENUM ('INSTANT', 'HOLD');
CREATE TYPE rpt.on_hold_expiration AS ENUM ('CANCEL', 'CAPTURE');
CREATE TYPE rpt.payment_payer_type AS ENUM ('PAYMENT_RESOURCE', 'CUSTOMER', 'RECURRENT');
CREATE TYPE rpt.failure_class AS ENUM ('OPERATION_TIMEOUT', 'FAILURE');

CREATE TABLE rpt.payment
(
    id                                BIGSERIAL                   NOT NULL,
    party_id                          CHARACTER VARYING           NOT NULL,
    shop_id                           CHARACTER VARYING           NOT NULL,
    invoice_id                        CHARACTER VARYING           NOT NULL,
    payment_id                        CHARACTER VARYING           NOT NULL,
    created_at                        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payer_type                        rpt.payment_payer_type      NOT NULL,
    tool                              rpt.payment_tool            NOT NULL,
    status                            rpt.invoice_payment_status  NOT NULL,
    status_created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    phone_number                      CHARACTER VARYING,
    email                             CHARACTER VARYING,
    flow                              rpt.payment_flow            NOT NULL,
    context_type                      CHARACTER VARYING,
    context                           BYTEA,
    CONSTRAINT payment_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_id_idx on rpt.payment (invoice_id, payment_id);
CREATE UNIQUE INDEX payment_created_at_idx ON rpt.payment (created_at);
CREATE UNIQUE INDEX payment_created_at_and_status_idx ON rpt.payment (status, created_at);

CREATE TABLE rpt.payment_additional_info
(
    ext_payment_id                    BIGINT                   NOT NULL,
    domain_revision                   BIGINT                   NOT NULL,
    party_revision                    BIGINT, --необходим ли тут bigint?
    bank_card_token                   CHARACTER VARYING,
    bank_card_system                  CHARACTER VARYING,
    bank_card_bin                     CHARACTER VARYING,
    bank_card_masked_pan              CHARACTER VARYING,
    bank_card_token_provider          rpt.bank_card_token_provider,
    terminal_provider                 CHARACTER VARYING,
    digital_wallet_id                 CHARACTER VARYING,
    digital_wallet_provider           CHARACTER VARYING,
    session_id                        CHARACTER VARYING,
    fingerprint                       CHARACTER VARYING,
    ip                                CHARACTER VARYING,
    customer_id                       CHARACTER VARYING,
    recurrent_payer_parent_invoice_id CHARACTER VARYING,
    recurrent_payer_parent_payment_id CHARACTER VARYING,
    hold_on_expiration                rpt.on_hold_expiration,
    hold_until                        TIMESTAMP WITHOUT TIME ZONE,
    make_recurrent_flag               BOOLEAN,
    operation_failure_class           rpt.failure_class           NULL,
    external_failure                  CHARACTER VARYING           NULL,
    external_failure_reason           CHARACTER VARYING           NULL,
    payment_short_id                  CHARACTER VARYING           NULL,
    provider_id                       INTEGER                     NOT NULL,
    terminal_id                       INTEGER                     NOT NULL,
    CONSTRAINT payment_additional_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_additional_id_idx on rpt.payment (invoice_id, payment_id);

-- как для частичной оплаты это будет выглядеть? Несколько записей cost или одна в итоге?
CREATE TABLE rpt.payment_cost
(
    id               BIGSERIAL                   NOT NULL,
    invoice_id       CHARACTER VARYING           NOT NULL,
    sequence_id      BIGINT                      NOT NULL,
    change_id        INT                         NOT NULL,
    event_created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_id       CHARACTER VARYING           NOT NULL,
    ext_payment_id   BIGINT                      NOT NULL,
    amount           BIGINT                      NOT NULL,
    origin_amount    BIGINT,
    currency_code    CHARACTER VARYING           NOT NULL,
    CONSTRAINT payment_cost_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_cost_idx ON rpt.payment_cost (ext_payment_id, event_created_at);

-- fee в итоге будет один на платеж?
CREATE TABLE rpt.payment_fee
(
    id                         BIGSERIAL                   NOT NULL,
    invoice_id                 CHARACTER VARYING           NOT NULL,
    sequence_id                BIGINT                      NOT NULL,
    change_id                  INT                         NOT NULL,
    event_created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_id                 CHARACTER VARYING           NOT NULL,
    ext_payment_id             BIGINT                      NOT NULL,
    currency_code              CHARACTER VARYING,
    fee                        BIGINT,
    provider_fee               BIGINT,
    external_fee               BIGINT,
    CONSTRAINT payment_fee_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_fee_idx ON rpt.payment_fee (ext_payment_id, event_created_at);