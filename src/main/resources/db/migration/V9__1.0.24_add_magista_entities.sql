-- invoice

CREATE TYPE rpt.invoice_event_type AS ENUM ('INVOICE_CREATED', 'INVOICE_STATUS_CHANGED',
  'INVOICE_PAYMENT_STARTED', 'INVOICE_PAYMENT_STATUS_CHANGED', 'INVOICE_PAYMENT_ADJUSTMENT_CREATED',
  'INVOICE_PAYMENT_ADJUSTMENT_STATUS_CHANGED', 'INVOICE_PAYMENT_REFUND_CREATED', 'INVOICE_PAYMENT_REFUND_STATUS_CHANGED',
  'INVOICE_PAYMENT_ADJUSTED', 'PAYMENT_TERMINAL_RECIEPT', 'INVOICE_PAYMENT_ROUTE_CHANGED',
  'INVOICE_PAYMENT_CASH_FLOW_CHANGED');

CREATE TYPE rpt.invoice_status AS ENUM ('unpaid', 'paid', 'cancelled', 'fulfilled');

CREATE TABLE rpt.invoice
(
  id                     BIGSERIAL                   NOT NULL,
  event_id               BIGINT                      NOT NULL,
  event_created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  event_type             rpt.invoice_event_type      NOT NULL,
  sequence_id            CHARACTER VARYING           NOT NULL,
  invoice_id             CHARACTER VARYING           NOT NULL,
  invoice_status         rpt.invoice_status          NOT NULL,
  invoice_status_details CHARACTER VARYING,
  invoice_product        CHARACTER VARYING           NOT NULL,
  invoice_description    CHARACTER VARYING,
  invoice_amount         BIGINT                      NOT NULL,
  invoice_currency_code  CHARACTER VARYING           NOT NULL,
  invoice_due            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  invoice_created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  invoice_party_revision BIGINT,
  invoice_template_id    CHARACTER VARYING,
  invoice_cart_json      CHARACTER VARYING,
  invoice_context_type   CHARACTER VARYING,
  invoice_context        BYTEA,
  party_id               UUID                        NOT NULL,
  party_shop_id          CHARACTER VARYING           NOT NULL,
  wtime                  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                BOOLEAN                     NOT NULL DEFAULT TRUE,
  CONSTRAINT invoice_pkey PRIMARY KEY (id),
  CONSTRAINT invoice_ukey
    UNIQUE (event_id, event_type, invoice_status)
);

CREATE INDEX invoice_invoice_id_event_created_at_idx ON rpt.invoice (invoice_id, event_created_at);
CREATE INDEX invoice_invoice_created_at_idx ON rpt.invoice (invoice_created_at);

-- adjustment

CREATE TYPE rpt.adjustment_status AS ENUM ('pending', 'captured', 'cancelled');

CREATE TABLE rpt.adjustment
(
  id                            BIGSERIAL                   NOT NULL,
  event_id                      BIGINT                      NOT NULL,
  event_created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  event_type                    rpt.invoice_event_type      NOT NULL,
  sequence_id                   CHARACTER VARYING           NOT NULL,
  invoice_id                    CHARACTER VARYING           NOT NULL,
  payment_id                    CHARACTER VARYING           NOT NULL,
  adjustment_id                 CHARACTER VARYING           NOT NULL,
  party_id                      CHARACTER VARYING           NOT NULL,
  party_shop_id                 CHARACTER VARYING           NOT NULL,
  adjustment_status             rpt.adjustment_status       NOT NULL,
  adjustment_status_created_at  TIMESTAMP WITHOUT TIME ZONE,
  adjustment_created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  adjustment_reason             CHARACTER VARYING           NOT NULL,
  adjustment_provider_cash_flow JSONB,
  adjustment_external_cash_flow JSONB,
  adjustment_domain_revision    BIGINT,
  wtime                         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                       BOOLEAN                     NOT NULL DEFAULT TRUE,
  CONSTRAINT adjustment_pkey PRIMARY KEY (id),
  CONSTRAINT adjustment_ukey
    UNIQUE (event_id, event_type, adjustment_status)
);

CREATE INDEX adjustment_adjustment_id_event_created_at_idx on rpt.adjustment (adjustment_id, event_created_at);
CREATE INDEX adjustment_adjustment_created_at_idx ON rpt.adjustment (adjustment_created_at);

-- refund

CREATE TYPE rpt.refund_status AS ENUM ('pending', 'succeeded', 'failed');

CREATE TYPE rpt.failure_class AS ENUM ('operation_timeout', 'failure');

CREATE TABLE rpt.refund
(
  id                             BIGSERIAL                   NOT NULL,
  event_id                       BIGINT                      NOT NULL,
  event_created_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  event_type                     rpt.invoice_event_type      NOT NULL,
  sequence_id                    CHARACTER VARYING           NOT NULL,
  invoice_id                     CHARACTER VARYING           NOT NULL,
  payment_id                     CHARACTER VARYING           NOT NULL,
  refund_id                      CHARACTER VARYING           NOT NULL,
  party_id                       CHARACTER VARYING           NOT NULL,
  party_shop_id                  CHARACTER VARYING           NOT NULL,
  refund_status                  rpt.refund_status           NOT NULL,
  refund_operation_failure_class rpt.failure_class,
  refund_external_failure        CHARACTER VARYING,
  refund_external_failure_reason CHARACTER VARYING,
  refund_created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  refund_reason                  CHARACTER VARYING,
  refund_currency_code           CHARACTER VARYING           NOT NULL,
  refund_amount                  BIGINT                      NOT NULL,
  refund_provider_cash_flow      JSONB,
  refund_external_cash_flow      JSONB,
  refund_domain_revision         BIGINT,
  wtime                          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                        BOOLEAN                     NOT NULL DEFAULT TRUE,
  CONSTRAINT refund_pkey PRIMARY KEY (id),
  CONSTRAINT refund_ukey
    UNIQUE (event_id, event_type, refund_status)
);

CREATE INDEX refund_refund_id_event_created_at_idx on rpt.refund (refund_id, event_created_at);
CREATE INDEX refund_refund_created_at_idx ON rpt.refund (refund_created_at);

-- payment

CREATE TYPE rpt.invoice_payment_status AS ENUM ('pending', 'processed', 'captured', 'cancelled', 'failed', 'refunded');
CREATE TYPE rpt.payment_tool AS ENUM ('bank_card', 'payment_terminal', 'digital_wallet');
CREATE TYPE rpt.bank_card_token_provider AS ENUM ('applepay', 'googlepay', 'samsungpay');
CREATE TYPE rpt.payment_flow AS ENUM ('instant', 'hold');
CREATE TYPE rpt.on_hold_expiration AS ENUM ('cancel', 'capture');
CREATE TYPE rpt.payment_payer_type AS ENUM ('payment_resource', 'customer', 'recurrent');

CREATE TABLE rpt.payment
(
  id                              BIGSERIAL                   NOT NULL,
  event_id                        BIGINT                      NOT NULL,
  event_created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  event_type                      rpt.invoice_event_type      NOT NULL,
  sequence_id                     CHARACTER VARYING           NOT NULL,
  invoice_id                      CHARACTER VARYING           NOT NULL,
  payment_id                      CHARACTER VARYING           NOT NULL,
  payment_status                  rpt.invoice_payment_status  NOT NULL,
  payment_operation_failure_class rpt.failure_class,
  payment_external_failure        CHARACTER VARYING,
  payment_external_failure_reason CHARACTER VARYING,
  payment_provider_cash_flow      JSONB,
  payment_external_cash_flow      JSONB,
  payment_domain_revision         BIGINT                      NOT NULL,
  payment_short_id                CHARACTER VARYING,
  payment_provider_id             INTEGER,
  payment_terminal_id             INTEGER,
  payment_amount                  BIGINT                      NOT NULL,
  payment_currency_code           CHARACTER VARYING           NOT NULL,
  payment_origin_amount           BIGINT                      NOT NULL,
  payment_customer_id             CHARACTER VARYING,
  payment_tool                              rpt.payment_tool            NOT NULL,
  payment_bank_card_masked_pan              CHARACTER VARYING,
  payment_bank_card_bin                     CHARACTER VARYING,
  payment_bank_card_token                   CHARACTER VARYING,
  payment_bank_card_system                  CHARACTER VARYING,
  payment_bank_card_token_provider          rpt.bank_card_token_provider,
  payment_terminal_provider                 CHARACTER VARYING,
  payment_digital_wallet_id                 CHARACTER VARYING,
  payment_digital_wallet_provider           CHARACTER VARYING,
  payment_flow                              rpt.payment_flow            NOT NULL,
  payment_hold_on_expiration                rpt.on_hold_expiration,
  payment_hold_until                        TIMESTAMP WITHOUT TIME ZONE,
  payment_session_id                        CHARACTER VARYING,
  payment_fingerprint                       CHARACTER VARYING,
  payment_ip                                CHARACTER VARYING,
  payment_phone_number                      CHARACTER VARYING,
  payment_email                             CHARACTER VARYING,
  payment_created_at                        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  payment_party_revision                    BIGINT,
  payment_context_type                      CHARACTER VARYING,
  payment_context                           BYTEA,
  payment_make_recurrent_flag               BOOLEAN,
  payment_recurrent_payer_parent_invoice_id CHARACTER VARYING,
  payment_recurrent_payer_parent_payment_id CHARACTER VARYING,
  payment_payer_type              rpt.payment_payer_type      NOT NULL,
  party_id                        UUID                        NOT NULL,
  party_shop_id                   CHARACTER VARYING           NOT NULL,
  wtime                           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                         BOOLEAN                     NOT NULL DEFAULT TRUE,
  CONSTRAINT payment_pkey PRIMARY KEY (id),
  CONSTRAINT payment_ukey
    UNIQUE (event_id, event_type, payment_status)
);

CREATE INDEX payment_payment_id_event_created_at_idx on rpt.payment (payment_id, event_created_at);
CREATE INDEX payment_payment_created_at_idx ON rpt.payment (payment_created_at);
