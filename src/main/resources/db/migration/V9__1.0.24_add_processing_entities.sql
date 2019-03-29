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
  sequence_id            INT                         NOT NULL,
  invoice_id             CHARACTER VARYING           NOT NULL,
  party_id               UUID                        NOT NULL,
  invoice_party_revision BIGINT,
  party_shop_id          CHARACTER VARYING           NOT NULL,
  invoice_created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  invoice_status         rpt.invoice_status          NOT NULL,
  invoice_status_details CHARACTER VARYING,
  invoice_product        CHARACTER VARYING           NOT NULL,
  invoice_description    CHARACTER VARYING,
  invoice_cart_json      CHARACTER VARYING,
  invoice_due            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  invoice_amount         BIGINT                      NOT NULL,
  invoice_currency_code  CHARACTER VARYING           NOT NULL,
  invoice_context_type   CHARACTER VARYING,
  invoice_context        BYTEA,
  invoice_template_id    CHARACTER VARYING,
  wtime                  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                BOOLEAN                     NOT NULL DEFAULT TRUE,
  CONSTRAINT invoice_pkey PRIMARY KEY (id),
  CONSTRAINT invoice_ukey
    UNIQUE (event_id, event_type, invoice_status)
);

CREATE INDEX invoice_invoice_id_event_created_at_idx ON rpt.invoice (invoice_id, event_created_at);
CREATE INDEX invoice_invoice_created_at_idx ON rpt.invoice (invoice_created_at);

-- payment

CREATE TYPE rpt.invoice_payment_status AS ENUM ('pending', 'processed', 'captured', 'cancelled', 'failed', 'refunded');
CREATE TYPE rpt.payment_tool AS ENUM ('bank_card', 'payment_terminal', 'digital_wallet');
CREATE TYPE rpt.bank_card_token_provider AS ENUM ('applepay', 'googlepay', 'samsungpay');
CREATE TYPE rpt.payment_flow AS ENUM ('instant', 'hold');
CREATE TYPE rpt.on_hold_expiration AS ENUM ('cancel', 'capture');
CREATE TYPE rpt.payment_payer_type AS ENUM ('payment_resource', 'customer', 'recurrent');
CREATE TYPE rpt.failure_class AS ENUM ('operation_timeout', 'failure');

CREATE TABLE rpt.payment
(
  id                                        BIGSERIAL                   NOT NULL,
  event_id                                  BIGINT                      NOT NULL,
  event_created_at                          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  event_type                                rpt.invoice_event_type      NOT NULL,
  sequence_id                               INT                         NOT NULL,
  invoice_id                                CHARACTER VARYING           NOT NULL,
  party_id                                  UUID                        NOT NULL,
  party_shop_id                             CHARACTER VARYING           NOT NULL,
  payment_id                                CHARACTER VARYING           NOT NULL,
  payment_created_at                        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  payment_domain_revision                   BIGINT                      NOT NULL,
  payment_party_revision                    BIGINT,
  payment_status                            rpt.invoice_payment_status  NOT NULL,
  payment_operation_failure_class           rpt.failure_class,
  payment_external_failure                  CHARACTER VARYING,
  payment_external_failure_reason           CHARACTER VARYING,
  -- Payer { UNION
  payment_payer_type                        rpt.payment_payer_type      NOT NULL,
  --  PaymentResourcePayer {
  --    DisposablePaymentResource {
  --      PaymentTool { UNION
  payment_tool                              rpt.payment_tool            NOT NULL,
  --        BankCard
  payment_bank_card_token                   CHARACTER VARYING,
  payment_bank_card_system                  CHARACTER VARYING,
  payment_bank_card_bin                     CHARACTER VARYING,
  payment_bank_card_masked_pan              CHARACTER VARYING,
  payment_bank_card_token_provider          rpt.bank_card_token_provider,
  --        PaymentTerminal
  payment_terminal_provider                 CHARACTER VARYING,
  --        DigitalWallet
  payment_digital_wallet_id                 CHARACTER VARYING,
  payment_digital_wallet_provider           CHARACTER VARYING,
  --      (PaymentTool) }
  payment_session_id                        CHARACTER VARYING,
  --      ClientInfo {
  payment_fingerprint                       CHARACTER VARYING,
  payment_ip                                CHARACTER VARYING,
  --      (ClientInfo) }
  --    (DisposablePaymentResource) }
  --    ContactInfo {
  payment_phone_number                      CHARACTER VARYING,
  payment_email                             CHARACTER VARYING,
  --    (ContactInfo) }
  --  (PaymentResourcePayer) }
  --  CustomerPayer {
  payment_customer_id                       CHARACTER VARYING,
  --    + PaymentTool {}
  --    + ContactInfo {}
  --  (CustomerPayer) }
  --  RecurrentPayer {
  --    + PaymentTool {}
  --    RecurrentParentPayment {
  payment_recurrent_payer_parent_invoice_id CHARACTER VARYING,
  payment_recurrent_payer_parent_payment_id CHARACTER VARYING,
  --      + ContactInfo {}
  --    (RecurrentParentPayment)}
  --  (RecurrentPayer) }
  -- (Payer) }
  payment_amount                            BIGINT                      NOT NULL,
  payment_origin_amount                     BIGINT                      NOT NULL,
  payment_currency_code                     CHARACTER VARYING           NOT NULL,
  payment_flow                              rpt.payment_flow            NOT NULL,
  payment_hold_on_expiration                rpt.on_hold_expiration,
  payment_hold_until                        TIMESTAMP WITHOUT TIME ZONE,
  payment_make_recurrent_flag               BOOLEAN,
  payment_context_type                      CHARACTER VARYING,
  payment_context                           BYTEA,
  payment_provider_id                       INTEGER,
  payment_terminal_id                       INTEGER,
  payment_cash_flow                         JSONB,
  payment_short_id                          CHARACTER VARYING,
  wtime                                     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                                   BOOLEAN                     NOT NULL DEFAULT TRUE,
  CONSTRAINT payment_pkey PRIMARY KEY (id),
  CONSTRAINT payment_ukey
    UNIQUE (event_id, event_type, payment_status)
);

CREATE INDEX payment_payment_id_event_created_at_idx on rpt.payment (payment_id, event_created_at);
CREATE INDEX payment_payment_created_at_idx ON rpt.payment (payment_created_at);

-- adjustment

CREATE TYPE rpt.adjustment_status AS ENUM ('pending', 'captured', 'cancelled');

CREATE TABLE rpt.adjustment
(
  id                           BIGSERIAL                   NOT NULL,
  event_id                     BIGINT                      NOT NULL,
  event_created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  event_type                   rpt.invoice_event_type      NOT NULL,
  sequence_id                  INT                         NOT NULL,
  invoice_id                   CHARACTER VARYING           NOT NULL,
  payment_id                   CHARACTER VARYING           NOT NULL,
  party_id                     UUID                        NOT NULL,
  party_shop_id                CHARACTER VARYING           NOT NULL,
  adjustment_id                CHARACTER VARYING           NOT NULL,
  adjustment_status            rpt.adjustment_status       NOT NULL,
  adjustment_status_created_at TIMESTAMP WITHOUT TIME ZONE,
  adjustment_created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  adjustment_domain_revision   BIGINT,
  adjustment_reason            CHARACTER VARYING           NOT NULL,
  adjustment_cash_flow         JSONB,
  adjustment_cash_flow_inverse_old JSONB,
  adjustment_party_revision        BIGINT,
  wtime                            TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                          BOOLEAN                     NOT NULL DEFAULT TRUE,
  CONSTRAINT adjustment_pkey PRIMARY KEY (id),
  CONSTRAINT adjustment_ukey
    UNIQUE (event_id, event_type, adjustment_status)
);

CREATE INDEX adjustment_adjustment_id_event_created_at_idx on rpt.adjustment (adjustment_id, event_created_at);
CREATE INDEX adjustment_adjustment_created_at_idx ON rpt.adjustment (adjustment_created_at);

-- refund

CREATE TYPE rpt.refund_status AS ENUM ('pending', 'succeeded', 'failed');

CREATE TABLE rpt.refund
(
  id                             BIGSERIAL                   NOT NULL,
  event_id                       BIGINT                      NOT NULL,
  event_created_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  event_type                     rpt.invoice_event_type      NOT NULL,
  sequence_id                    INT                         NOT NULL,
  invoice_id                     CHARACTER VARYING           NOT NULL,
  payment_id                     CHARACTER VARYING           NOT NULL,
  party_id                       UUID                        NOT NULL,
  party_shop_id                  CHARACTER VARYING           NOT NULL,
  refund_id                      CHARACTER VARYING           NOT NULL,
  refund_status                  rpt.refund_status           NOT NULL,
  refund_operation_failure_class rpt.failure_class,
  refund_external_failure        CHARACTER VARYING,
  refund_external_failure_reason CHARACTER VARYING,
  refund_created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  refund_domain_revision         BIGINT,
  refund_party_revision          BIGINT,
  refund_currency_code           CHARACTER VARYING           NOT NULL,
  refund_amount                  BIGINT                      NOT NULL,
  refund_reason                  CHARACTER VARYING,
  refund_cash_flow               JSONB,
  wtime                          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                        BOOLEAN                     NOT NULL DEFAULT TRUE,
  CONSTRAINT refund_pkey PRIMARY KEY (id),
  CONSTRAINT refund_ukey
    UNIQUE (event_id, event_type, refund_status)
);

CREATE INDEX refund_refund_id_event_created_at_idx on rpt.refund (refund_id, event_created_at);
CREATE INDEX refund_refund_created_at_idx ON rpt.refund (refund_created_at);

-- payout

CREATE TYPE rpt.payout_event_category AS ENUM ('PAYOUT');
CREATE TYPE rpt.payout_event_type AS ENUM ('PAYOUT_CREATED', 'PAYOUT_STATUS_CHANGED');
CREATE TYPE rpt.payout_status AS ENUM ('unpaid', 'paid', 'cancelled', 'confirmed');
CREATE TYPE rpt.payout_type AS ENUM ('bank_card', 'bank_account', 'wallet');
CREATE TYPE rpt.payout_account_type AS ENUM ('RUSSIAN_PAYOUT_ACCOUNT', 'INTERNATIONAL_PAYOUT_ACCOUNT');

CREATE TABLE rpt.payout
(
  id                                                           BIGSERIAL                   NOT NULL,
  event_id                                                     BIGINT                      NOT NULL,
  event_created_at                                             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  event_type                                                   rpt.payout_event_type       NOT NULL,
  event_category                                               rpt.payout_event_category   NOT NULL,
  sequence_id                                                  INT                         NOT NULL,
  payout_id                                                    CHARACTER VARYING           NOT NULL,
  party_id                                                     UUID                        NOT NULL,
  party_shop_id                                                CHARACTER VARYING           NOT NULL,
  contract_id                                                  CHARACTER VARYING           NOT NULL,
  payout_created_at                                            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  payout_status                                                rpt.payout_status           NOT NULL,
  payout_amount                                                BIGINT                      NOT NULL,
  payout_fee                                                   BIGINT,
  payout_currency_code                                         CHARACTER VARYING           NOT NULL,
  payout_cash_flow                                             JSONB,
  payout_type                                                  rpt.payout_type             NOT NULL,
  payout_wallet_id                                             CHARACTER VARYING,
  payout_account_type                                          rpt.payout_account_type,
  payout_account_bank_id                                       CHARACTER VARYING,
  payout_account_bank_corr_id                                  CHARACTER VARYING,
  payout_account_bank_local_code                               CHARACTER VARYING,
  payout_account_bank_name                                     CHARACTER VARYING,
  payout_account_purpose                                       CHARACTER VARYING,
  payout_account_inn                                           CHARACTER VARYING,
  payout_account_legal_agreement_id                            CHARACTER VARYING,
  payout_account_legal_agreement_signed_at                     TIMESTAMP WITHOUT TIME ZONE,
  payout_account_trading_name                                  CHARACTER VARYING,
  payout_account_legal_name                                    CHARACTER VARYING,
  payout_account_actual_address                                CHARACTER VARYING,
  payout_account_registered_address                            CHARACTER VARYING,
  payout_account_registered_number                             CHARACTER VARYING,
  payout_account_bank_iban                                     CHARACTER VARYING,
  payout_account_bank_number                                   CHARACTER VARYING,
  payout_account_bank_address                                  CHARACTER VARYING,
  payout_account_bank_bic                                      CHARACTER VARYING,
  payout_account_bank_aba_rtn                                  CHARACTER VARYING,
  payout_account_bank_country_code                             CHARACTER VARYING,
  payout_cancel_details                                        CHARACTER VARYING,
  payout_international_correspondent_account_bank_account      CHARACTER VARYING,
  payout_international_correspondent_account_bank_number       CHARACTER VARYING,
  payout_international_correspondent_account_bank_iban         CHARACTER VARYING,
  payout_international_correspondent_account_bank_name         CHARACTER VARYING,
  payout_international_correspondent_account_bank_address      CHARACTER VARYING,
  payout_international_correspondent_account_bank_bic          CHARACTER VARYING,
  payout_international_correspondent_account_bank_aba_rtn      CHARACTER VARYING,
  payout_international_correspondent_account_bank_country_code CHARACTER VARYING,
  payout_summary                                               JSONB,
  wtime                                                        TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                                                      BOOLEAN                     NOT NULL DEFAULT TRUE,
  CONSTRAINT payout_pkey PRIMARY KEY (id),
  CONSTRAINT payout_ukey
    UNIQUE (event_id, event_type, payout_status)
);
