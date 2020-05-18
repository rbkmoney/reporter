CREATE TYPE rpt.payout_status AS ENUM ('UNPAID', 'PAID', 'CANCELLED', 'CONFIRMED');
CREATE TYPE rpt.payout_type AS ENUM ('BANK_CARD', 'BANK_ACCOUNT', 'WALLET');
CREATE TYPE rpt.payout_account_type AS ENUM ('RUSSIAN_PAYOUT_ACCOUNT', 'INTERNATIONAL_PAYOUT_ACCOUNT');

CREATE TABLE rpt.payout
(
    id                                                    BIGSERIAL                   NOT NULL,
    party_id                                              CHARACTER VARYING           NOT NULL,
    party_shop_id                                         CHARACTER VARYING           NOT NULL,
    payout_id                                             CHARACTER VARYING           NOT NULL,
    contract_id                                           CHARACTER VARYING           NOT NULL,
    created_at                                            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    amount                                                BIGINT                      NOT NULL,
    fee                                                   BIGINT                      NOT NULL,
    currency_code                                         CHARACTER VARYING           NOT NULL,
    type                                                  rpt.payout_type             NOT NULL,
    wallet_id                                             CHARACTER VARYING,
    account_type                                          rpt.payout_account_type,
    account_bank_id                                       CHARACTER VARYING,
    account_bank_corr_id                                  CHARACTER VARYING,
    account_bank_local_code                               CHARACTER VARYING,
    account_bank_name                                     CHARACTER VARYING,
    account_purpose                                       CHARACTER VARYING,
    account_inn                                           CHARACTER VARYING,
    account_legal_agreement_id                            CHARACTER VARYING,
    account_legal_agreement_signed_at                     TIMESTAMP WITHOUT TIME ZONE,
    account_trading_name                                  CHARACTER VARYING,
    account_legal_name                                    CHARACTER VARYING,
    account_actual_address                                CHARACTER VARYING,
    account_registered_address                            CHARACTER VARYING,
    account_registered_number                             CHARACTER VARYING,
    account_bank_iban                                     CHARACTER VARYING,
    account_bank_number                                   CHARACTER VARYING,
    account_bank_address                                  CHARACTER VARYING,
    account_bank_bic                                      CHARACTER VARYING,
    account_bank_aba_rtn                                  CHARACTER VARYING,
    account_bank_country_code                             CHARACTER VARYING,
    international_correspondent_account_bank_account      CHARACTER VARYING,
    international_correspondent_account_bank_number       CHARACTER VARYING,
    international_correspondent_account_bank_iban         CHARACTER VARYING,
    international_correspondent_account_bank_name         CHARACTER VARYING,
    international_correspondent_account_bank_address      CHARACTER VARYING,
    international_correspondent_account_bank_bic          CHARACTER VARYING,
    international_correspondent_account_bank_aba_rtn      CHARACTER VARYING,
    international_correspondent_account_bank_country_code CHARACTER VARYING,
    summary                                               CHARACTER VARYING,
    CONSTRAINT payout_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payout_id_idx on rpt.payout (payout_id);
CREATE UNIQUE INDEX payout_created_at_idx ON rpt.payout (created_at);

CREATE TABLE rpt.payout_state
(
    id               BIGSERIAL                   NOT NULL,
    event_id         BIGINT                      NOT NULL,
    event_created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payout_id        CHARACTER VARYING           NOT NULL,
    ext_payout_id    CHARACTER VARYING           NOT NULL,
    status           rpt.payout_status           NOT NULL,
    cancel_details   CHARACTER VARYING,
    CONSTRAINT payout_status_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payout_status_idx ON rpt.payout_state (ext_payout_id, event_created_at, status);
CREATE UNIQUE INDEX payout_status_by_date_idx ON rpt.payout_state (event_created_at, status);
