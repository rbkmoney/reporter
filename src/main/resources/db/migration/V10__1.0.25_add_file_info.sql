CREATE TABLE rpt.file_info
(
  id           BIGSERIAL         NOT NULL,
  report_id    BIGINT            NOT NULL,
  file_data_id CHARACTER VARYING NOT NULL,
  CONSTRAINT file_info_pkey PRIMARY KEY (id),
  CONSTRAINT file_info_fkey FOREIGN KEY (report_id) REFERENCES rpt.report (id) ON DELETE RESTRICT ON UPDATE NO ACTION
)
