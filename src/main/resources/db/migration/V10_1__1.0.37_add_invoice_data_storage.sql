CREATE TYPE rpt.invoice_status AS ENUM ('UNPAID', 'PAID', 'CANCELLED', 'FULFILLED');

CREATE TABLE rpt.invoice
(
    id                 BIGSERIAL                   NOT NULL,
    invoice_id         CHARACTER VARYING           NOT NULL,
    status             rpt.invoice_status          NOT NULL,
    status_details     CHARACTER VARYING,
    party_id           CHARACTER VARYING           NOT NULL,
    party_revision     BIGINT,
    shop_id            CHARACTER VARYING           NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    product            CHARACTER VARYING           NOT NULL,
    description        CHARACTER VARYING,
    cart_json          CHARACTER VARYING,
    due                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    amount             BIGINT                      NOT NULL,
    currency_code      CHARACTER VARYING           NOT NULL,
    context_type       CHARACTER VARYING,
    context            BYTEA,
    template_id        CHARACTER VARYING,

    CONSTRAINT invoice_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX invoice_id_idx ON rpt.invoice (invoice_id);
CREATE UNIQUE INDEX invoice_created_at_idx ON rpt.invoice (created_at);
CREATE UNIQUE INDEX invoice_created_at_and_status_idx ON rpt.invoice (status, created_at);
