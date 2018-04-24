CREATE TABLE rpt.contract_meta (
  party_id               CHARACTER VARYING           NOT NULL,
  contract_id            CHARACTER VARYING           NOT NULL,
  wtime                  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  last_event_id          BIGINT                      NOT NULL,
  schedule_id            INT,
  calendar_id            INT,
  last_report_created_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT contract_meta_pkey PRIMARY KEY (party_id, contract_id)
);

-- alter table rpt.pos_report_meta
--   drop column last_report_created_at;



