-- invoice
CREATE TYPE rpt.invoice_status AS ENUM ('unpaid', 'paid', 'cancelled', 'fulfilled');

CREATE TABLE rpt.invoice
(
    id             BIGSERIAL                   NOT NULL,
    party_id       UUID                        NOT NULL,
    party_shop_id  CHARACTER VARYING           NOT NULL,
    invoice_id     CHARACTER VARYING           NOT NULL,
    party_revision BIGINT,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    product        CHARACTER VARYING           NOT NULL,
    description    CHARACTER VARYING,
    cart_json      CHARACTER VARYING,
    due            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    amount         BIGINT                      NOT NULL,
    currency_code  CHARACTER VARYING           NOT NULL,
    context_type   CHARACTER VARYING,
    context        BYTEA,
    template_id    CHARACTER VARYING,
    CONSTRAINT invoice_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX invoice_id_idx ON rpt.invoice (invoice_id);
CREATE UNIQUE INDEX invoice_created_at_idx ON rpt.invoice (created_at);

CREATE TABLE rpt.invoice_state
(
    id               BIGSERIAL                   NOT NULL,
    invoice_id       CHARACTER VARYING           NOT NULL,
    sequence_id      BIGINT                      NOT NULL,
    change_id        INT                         NOT NULL,
    event_created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status           rpt.invoice_status          NOT NULL,
    status_details   CHARACTER VARYING           NULL,
    CONSTRAINT invoice_state_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX invoice_status_idx ON rpt.invoice_state (invoice_id, sequence_id, change_id);

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
    id                                BIGSERIAL                   NOT NULL,
    party_id                          UUID                        NOT NULL,
    party_shop_id                     CHARACTER VARYING           NOT NULL,
    invoice_id                        CHARACTER VARYING           NOT NULL,
    payment_id                        CHARACTER VARYING           NOT NULL,
    created_at                        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    domain_revision                   BIGINT                      NOT NULL,
    party_revision                    BIGINT,
    payer_type                        rpt.payment_payer_type      NOT NULL,
    tool                              rpt.payment_tool            NOT NULL,
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
    phone_number                      CHARACTER VARYING,
    email                             CHARACTER VARYING,
    customer_id                       CHARACTER VARYING,
    recurrent_payer_parent_invoice_id CHARACTER VARYING,
    recurrent_payer_parent_payment_id CHARACTER VARYING,
    flow                              rpt.payment_flow            NOT NULL,
    hold_on_expiration                rpt.on_hold_expiration,
    hold_until                        TIMESTAMP WITHOUT TIME ZONE,
    make_recurrent_flag               BOOLEAN,
    context_type                      CHARACTER VARYING,
    context                           BYTEA,
    CONSTRAINT payment_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_id_idx on rpt.payment (invoice_id, payment_id);
CREATE UNIQUE INDEX payment_created_at_idx ON rpt.payment (created_at);

CREATE TABLE rpt.payment_state
(
    id                      BIGSERIAL                   NOT NULL,
    invoice_id              CHARACTER VARYING           NOT NULL,
    sequence_id             BIGINT                      NOT NULL,
    change_id               INT                         NOT NULL,
    event_created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_id              CHARACTER VARYING           NOT NULL,
    status                  rpt.invoice_payment_status  NOT NULL,
    operation_failure_class rpt.failure_class           NULL,
    external_failure        CHARACTER VARYING           NULL,
    external_failure_reason CHARACTER VARYING           NULL,
    CONSTRAINT payment_status_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_status_idx ON rpt.payment_state (invoice_id, sequence_id, change_id);

CREATE TABLE rpt.payment_routing
(
    id               BIGSERIAL                   NOT NULL,
    invoice_id       CHARACTER VARYING           NOT NULL,
    sequence_id      BIGINT                      NOT NULL,
    change_id        INT                         NOT NULL,
    event_created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_id       CHARACTER VARYING           NOT NULL,
    provider_id      INTEGER                     NOT NULL,
    terminal_id      INTEGER                     NOT NULL,
    CONSTRAINT payment_route_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_route_idx ON rpt.payment_routing (invoice_id, sequence_id, change_id);

CREATE TABLE rpt.payment_terminal_receipt
(
    id               BIGSERIAL                   NOT NULL,
    invoice_id       CHARACTER VARYING           NOT NULL,
    sequence_id      BIGINT                      NOT NULL,
    change_id        INT                         NOT NULL,
    event_created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_id       CHARACTER VARYING           NOT NULL,
    payment_short_id CHARACTER VARYING           NOT NULL,
    CONSTRAINT payment_terminal_receipt_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_terminal_receipt_idx ON rpt.payment_terminal_receipt (invoice_id, sequence_id, change_id);

CREATE TABLE rpt.payment_cost
(
    id               BIGSERIAL                   NOT NULL,
    invoice_id       CHARACTER VARYING           NOT NULL,
    sequence_id      BIGINT                      NOT NULL,
    change_id        INT                         NOT NULL,
    event_created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_id       CHARACTER VARYING           NOT NULL,
    amount           BIGINT                      NOT NULL,
    origin_amount    BIGINT,
    currency_code    CHARACTER VARYING           NOT NULL,
    CONSTRAINT payment_cost_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_cost_idx ON rpt.payment_cost (invoice_id, sequence_id, change_id);

CREATE TABLE rpt.payment_fee
(
    id                         BIGSERIAL                   NOT NULL,
    invoice_id                 CHARACTER VARYING           NOT NULL,
    sequence_id                BIGINT                      NOT NULL,
    change_id                  INT                         NOT NULL,
    event_created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_id                 CHARACTER VARYING           NOT NULL,
    fee                        BIGINT,
    fee_currency_code          CHARACTER VARYING,
    provider_fee               BIGINT,
    provider_fee_currency_code CHARACTER VARYING,
    external_fee               BIGINT,
    external_fee_currency_code CHARACTER VARYING,
    CONSTRAINT payment_fee_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payment_fee_idx ON rpt.payment_fee (invoice_id, sequence_id, change_id);

-- adjustment
CREATE TYPE rpt.adjustment_status AS ENUM ('pending', 'captured', 'cancelled', 'processed');

CREATE TABLE rpt.adjustment
(
    id                             BIGSERIAL                   NOT NULL,
    party_id                       UUID                        NOT NULL,
    party_shop_id                  CHARACTER VARYING           NOT NULL,
    invoice_id                     CHARACTER VARYING           NOT NULL,
    payment_id                     CHARACTER VARYING           NOT NULL,
    adjustment_id                  CHARACTER VARYING           NOT NULL,
    created_at                     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    domain_revision                BIGINT,
    reason                         CHARACTER VARYING           NOT NULL,
    party_revision                 BIGINT,
    fee                            BIGINT,
    fee_currency_code              CHARACTER VARYING,
    provider_fee                   BIGINT,
    provider_fee_currency_code     CHARACTER VARYING,
    external_fee                   BIGINT,
    external_fee_currency_code     CHARACTER VARYING,
    old_fee                        BIGINT,
    old_fee_currency_code          CHARACTER VARYING,
    old_provider_fee               BIGINT,
    old_provider_fee_currency_code CHARACTER VARYING,
    old_external_fee               BIGINT,
    old_external_fee_currency_code CHARACTER VARYING,
    CONSTRAINT adjustment_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX adjustment_id_idx on rpt.adjustment (invoice_id, payment_id, adjustment_id);
CREATE UNIQUE INDEX adjustment_created_at_idx ON rpt.adjustment (created_at);

CREATE TABLE rpt.adjustment_state
(
    id                BIGSERIAL                   NOT NULL,
    invoice_id        CHARACTER VARYING           NOT NULL,
    sequence_id       BIGINT                      NOT NULL,
    change_id         INT                         NOT NULL,
    event_created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_id        CHARACTER VARYING           NOT NULL,
    adjustment_id     CHARACTER VARYING           NOT NULL,
    status            rpt.adjustment_status       NOT NULL,
    status_created_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT adjustment_status_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX adjustment_status_idx ON rpt.adjustment_state (invoice_id, sequence_id, change_id);

-- refund
CREATE TYPE rpt.refund_status AS ENUM ('pending', 'succeeded', 'failed');

CREATE TABLE rpt.refund
(
    id                         BIGSERIAL                   NOT NULL,
    party_id                   UUID                        NOT NULL,
    party_shop_id              CHARACTER VARYING           NOT NULL,
    invoice_id                 CHARACTER VARYING           NOT NULL,
    payment_id                 CHARACTER VARYING           NOT NULL,
    refund_id                  CHARACTER VARYING           NOT NULL,
    created_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    domain_revision            BIGINT,
    party_revision             BIGINT,
    amount                     BIGINT                      NOT NULL,
    currency_code              CHARACTER VARYING           NOT NULL,
    reason                     CHARACTER VARYING,
    fee                        BIGINT,
    fee_currency_code          CHARACTER VARYING,
    provider_fee               BIGINT,
    provider_fee_currency_code CHARACTER VARYING,
    external_fee               BIGINT,
    external_fee_currency_code CHARACTER VARYING,
    CONSTRAINT refund_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX refund_id_idx on rpt.refund (invoice_id, payment_id, refund_id);
CREATE UNIQUE INDEX refund_created_at_idx ON rpt.refund (created_at);

CREATE TABLE rpt.refund_state
(
    id                      BIGSERIAL                   NOT NULL,
    invoice_id              CHARACTER VARYING           NOT NULL,
    sequence_id             BIGINT                      NOT NULL,
    change_id               INT                         NOT NULL,
    event_created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_id              CHARACTER VARYING           NOT NULL,
    refund_id               CHARACTER VARYING           NOT NULL,
    status                  rpt.refund_status           NOT NULL,
    operation_failure_class rpt.failure_class           NULL,
    external_failure        CHARACTER VARYING           NULL,
    external_failure_reason CHARACTER VARYING           NULL,
    CONSTRAINT refund_status_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX refund_status_idx ON rpt.refund_state (invoice_id, sequence_id, change_id);

-- payout

CREATE TYPE rpt.payout_status AS ENUM ('unpaid', 'paid', 'cancelled', 'confirmed');
CREATE TYPE rpt.payout_type AS ENUM ('bank_card', 'bank_account', 'wallet');
CREATE TYPE rpt.payout_account_type AS ENUM ('RUSSIAN_PAYOUT_ACCOUNT', 'INTERNATIONAL_PAYOUT_ACCOUNT');

CREATE TABLE rpt.payout
(
    id                                                    BIGSERIAL                   NOT NULL,
    party_id                                              UUID                        NOT NULL,
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
    status           rpt.payout_status           NOT NULL,
    cancel_details   CHARACTER VARYING,
    CONSTRAINT payout_status_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX payout_status_idx ON rpt.payout_state (payout_id, event_id, event_created_at);

-- add cash flow aggregate functions

create or replace function rpt.is_cash_flow_contains_amount_by_types(data_with_cash_flows jsonb,
                                                                     source_account_type text,
                                                                     source_account_type_value text,
                                                                     destination_account_type text,
                                                                     destination_account_type_value text) returns boolean
    immutable
    parallel safe
    language plpgsql
as
$$
declare
    cash_flow jsonb;
begin
    if data_with_cash_flows is null then
        return false;
    end if;
    for cash_flow in select * from jsonb_array_elements(data_with_cash_flows -> 'cash_flows')
        loop
            if cash_flow @>
               concat('{"source":{"account_type":{"', source_account_type, '":"', source_account_type_value, '"}}}')::jsonb
                and cash_flow @> concat('{"destination":{"account_type":{"', destination_account_type, '":"',
                                        destination_account_type_value, '"}}}')::jsonb
                and ((cash_flow ->> 'volume')::jsonb ->> 'amount' is not null) then
                return true;
            end if;
        end loop;
    return false;
end;
$$;

create or replace function rpt.is_cash_flow_contains_amount_by_types(data_with_cash_flows jsonb,
                                                                     source_account_type text,
                                                                     destination_account_type text) returns boolean
    immutable
    parallel safe
    language plpgsql
as
$$
declare
    cash_flow jsonb;
begin
    if data_with_cash_flows is null then
        return false;
    end if;
    for cash_flow in select * from jsonb_array_elements(data_with_cash_flows -> 'cash_flows')
        loop
            if (((cash_flow ->> 'source')::jsonb ->> 'account_type')::jsonb ->> source_account_type is not null)
                and (((cash_flow ->> 'destination')::jsonb ->> 'account_type')::jsonb ->>
                     destination_account_type is not null)
                and ((cash_flow ->> 'volume')::jsonb ->> 'amount' is not null) then
                return true;
            end if;
        end loop;
    return false;
end;
$$;

create or replace function rpt.get_cash_flow_value(data_with_cash_flows jsonb, source_account_type text,
                                                   source_account_type_value text, destination_account_type text,
                                                   destination_account_type_value text,
                                                   default_value bigint) returns bigint
    immutable
    parallel safe
    language plpgsql
as
$$
begin
    if (rpt.is_cash_flow_contains_amount_by_types(data_with_cash_flows, source_account_type, source_account_type_value,
                                                  destination_account_type,
                                                  destination_account_type_value) is not true) then
        return default_value;
    end if;
    return (
        select sum(cast(((cash_flows_array ->> 'volume')::jsonb ->> 'amount') as bigint))
        from jsonb_array_elements(data_with_cash_flows -> 'cash_flows') as cash_flows_array
        where cash_flows_array @>
              concat('{"source":{"account_type":{"', source_account_type, '":"', source_account_type_value, '"}}}')::jsonb
          and cash_flows_array @> concat('{"destination":{"account_type":{"', destination_account_type, '":"',
                                         destination_account_type_value, '"}}}')::jsonb
          and ((cash_flows_array ->> 'volume')::jsonb ->> 'amount' is not null)
    );
end;
$$;

create or replace function rpt.get_cash_flow_value(data_with_cash_flows jsonb, source_account_type text,
                                                   destination_account_type text, default_value bigint) returns bigint
    immutable
    parallel safe
    language plpgsql
as
$$
begin
    if (rpt.is_cash_flow_contains_amount_by_types(data_with_cash_flows, source_account_type,
                                                  destination_account_type) is not true) then
        return default_value;
    end if;
    return (
        select sum(cast(((cash_flows_array ->> 'volume')::jsonb ->> 'amount') as bigint))
        from jsonb_array_elements(data_with_cash_flows -> 'cash_flows') as cash_flows_array
        where (((cash_flows_array ->> 'source')::jsonb ->> 'account_type')::jsonb ->> source_account_type is not null)
          and (((cash_flows_array ->> 'destination')::jsonb ->> 'account_type')::jsonb ->>
               destination_account_type is not null)
          and ((cash_flows_array ->> 'volume')::jsonb ->> 'amount' is not null)
    );
end;
$$;

create or replace function rpt.get_cash_flow_amount(data_with_cash_flows jsonb, default_value bigint) returns bigint
    immutable
    parallel safe
    language plpgsql
as
$$
declare
    source_account_type            text := 'provider';
    source_account_type_value      text := 'settlement';
    destination_account_type       text := 'merchant';
    destination_account_type_value text := 'settlement';
begin
    return rpt.get_cash_flow_value(data_with_cash_flows, source_account_type, source_account_type_value,
                                   destination_account_type, destination_account_type_value, default_value);
end;
$$;

create or replace function rpt.get_cash_flow_fee(data_with_cash_flows jsonb, default_value bigint) returns bigint
    immutable
    parallel safe
    language plpgsql
as
$$
declare
    source_account_type            text := 'merchant';
    source_account_type_value      text := 'settlement';
    destination_account_type       text := 'system';
    destination_account_type_value text := 'settlement';
begin
    return rpt.get_cash_flow_value(data_with_cash_flows, source_account_type, source_account_type_value,
                                   destination_account_type, destination_account_type_value, default_value);
end;
$$;

create or replace function rpt.get_cash_flow_provider_fee(data_with_cash_flows jsonb, default_value bigint) returns bigint
    immutable
    parallel safe
    language plpgsql
as
$$
declare
    source_account_type      text := 'system';
    destination_account_type text := 'provider';
begin
    return rpt.get_cash_flow_value(data_with_cash_flows, source_account_type, destination_account_type, default_value);
end;
$$;
create or replace function rpt.get_cash_flow_external_fee(data_with_cash_flows jsonb, default_value bigint) returns bigint
    immutable
    parallel safe
    language plpgsql
as
$$
declare
    source_account_type      text := 'system';
    destination_account_type text := 'external';
begin
    return rpt.get_cash_flow_value(data_with_cash_flows, source_account_type, destination_account_type, default_value);
end;
$$;

create or replace function rpt.get_cash_flow_fee(data_with_cash_flows jsonb, default_value bigint) returns bigint
    immutable
    parallel safe
    language plpgsql
as
$$
declare
    source_account_type            text := 'merchant';
    source_account_type_value      text := 'settlement';
    destination_account_type       text := 'system';
    destination_account_type_value text := 'settlement';
begin
    return rpt.get_cash_flow_value(data_with_cash_flows, source_account_type, source_account_type_value,
                                   destination_account_type, destination_account_type_value, default_value);
end;
$$;

create or replace function rpt.sum_finalfunc(sum_value bigint) returns bigint
    immutable
    parallel safe
    language plpgsql
as
$$
begin
    return sum_value;
end;
$$;
