truncate rpt.invoice restart identity cascade;
truncate rpt.payment restart identity cascade;
truncate rpt.payment_state restart identity cascade;
truncate rpt.payment_cost restart identity cascade;
truncate rpt.payment_routing restart identity cascade;
truncate rpt.payment_fee restart identity cascade;
truncate rpt.payment_terminal_receipt restart identity cascade;

-- test getPaymentPartyData

insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', 'uAykKfsktM', '1', '2021-08-23 12:12:52', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_cost (invoice_id, sequence_id, change_id, event_created_at, payment_id, amount, currency_code)
values ('uAykKfsktM', 1, 1, '2021-08-23 12:12:53', '1', 1234, 'RUB');
-- invalid invoice_id for payment + payment_cost
insert into rpt.payment_cost (invoice_id, sequence_id, change_id, event_created_at, payment_id, amount, currency_code)
values ('uAykKfsktN', 1, 1, '2021-08-23 12:12:53', '1', 123, 'RUB');

--  test getShopAccountingReportData

-- invalid status for payment + payment_state + payment_cost
insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b33', 'test_shop_1', 'uAykKfsktX', '1', '2021-08-23 12:12:53', 1,
        'payment_resource', 'bank_card', 'instant');
insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktX', 1, 1, '2021-08-23 12:12:53', '1', 'pending');

insert into rpt.payment_cost (invoice_id, sequence_id, change_id, event_created_at, payment_id, amount, currency_code)
values ('uAykKfsktX', 1, 1, '2021-08-23 12:12:53', '1', 121, 'RUB');
--

-- invalid party_id for payment + payment_state + payment_cost
insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b34', 'test_shop_1', 'uAykKfsktC', '1', '2021-08-23 12:12:54', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktC', 1, 1, '2021-08-23 12:12:53', '1', 'captured');

insert into rpt.payment_cost (invoice_id, sequence_id, change_id, event_created_at, payment_id, amount, currency_code)
values ('uAykKfsktC', 1, 1, '2021-08-23 12:12:53', '1', 122, 'RUB');
--

insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b33', 'test_shop_1', 'uAykKfsktZ', '1', '2021-08-23 12:12:55', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktZ', 1, 1, '2021-08-23 12:12:53', '1', 'captured');

insert into rpt.payment_cost (invoice_id, sequence_id, change_id, event_created_at, payment_id, amount, currency_code)
values ('uAykKfsktZ', 1, 1, '2021-08-23 12:12:53', '1', 123, 'RUB');

insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b33', 'test_shop_1', 'uAykKfsktS', '1', '2021-08-23 12:12:56', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktS', 1, 1, '2021-08-23 12:12:53', '1', 'captured');

insert into rpt.payment_cost (invoice_id, sequence_id, change_id, event_created_at, payment_id, amount, currency_code)
values ('uAykKfsktS', 1, 1, '2021-08-23 12:12:53', '1', 124, 'RUB');

insert into rpt.payment_fee (invoice_id, sequence_id, change_id, event_created_at, payment_id, fee, fee_currency_code)
values ('uAykKfsktS', 1, 1, '2021-08-23 12:12:53', '1', 100, 'RUB');

--  test getPaymentRegistryReportData

insert into rpt.invoice(party_id, party_shop_id, invoice_id, created_at, product, due, amount, currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktW', '2021-08-23 12:12:52', 'asd',
        '2021-08-23 12:12:52', 123, 'RUB');

insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktW', '1', '2021-08-23 12:12:57', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktW', 1, 1, '2021-08-23 12:12:53', '1', 'captured');

insert into rpt.payment_cost (invoice_id, sequence_id, change_id, event_created_at, payment_id, amount, currency_code)
values ('uAykKfsktW', 1, 1, '2021-08-23 12:12:53', '1', 124, 'RUB');

insert into rpt.payment_fee (invoice_id, sequence_id, change_id, event_created_at, payment_id, fee, fee_currency_code)
values ('uAykKfsktW', 1, 1, '2021-08-23 12:12:53', '1', 100, 'RUB');

insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktE', '1', '2021-08-23 12:12:58', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktE', 1, 1, '2021-08-23 12:12:53', '1', 'captured');

insert into rpt.payment_cost (invoice_id, sequence_id, change_id, event_created_at, payment_id, amount, currency_code)
values ('uAykKfsktE', 1, 1, '2021-08-23 12:12:53', '1', 125, 'RUB');

insert into rpt.payment_fee (invoice_id, sequence_id, change_id, event_created_at, payment_id, provider_fee,
                             provider_fee_currency_code)
values ('uAykKfsktE', 1, 1, '2021-08-23 12:12:53', '1', 101, 'RUB');

-- invalid party_id for payment + payment_state + payment_cost
insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b36', 'test_shop_1', 'uAykKfsktR', '1', '2021-08-23 12:12:59', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktR', 1, 1, '2021-08-23 12:12:53', '1', 'captured');

insert into rpt.payment_cost (invoice_id, sequence_id, change_id, event_created_at, payment_id, amount, currency_code)
values ('uAykKfsktR', 1, 1, '2021-08-23 12:12:53', '1', 125, 'RUB');

insert into rpt.payment_fee (invoice_id, sequence_id, change_id, event_created_at, payment_id, provider_fee,
                             provider_fee_currency_code)
values ('uAykKfsktR', 1, 1, '2021-08-23 12:12:53', '1', 101, 'RUB');
--

-- invalid status for payment + payment_state + payment_cost + payment_fee
insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktI', '1', '2021-08-23 12:13:59', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktI', 1, 1, '2021-08-23 12:12:53', '1', 'pending');

insert into rpt.payment_cost (invoice_id, sequence_id, change_id, event_created_at, payment_id, amount, currency_code)
values ('uAykKfsktI', 1, 1, '2021-08-23 12:12:53', '1', 125, 'RUB');

insert into rpt.payment_fee (invoice_id, sequence_id, change_id, event_created_at, payment_id, provider_fee,
                             provider_fee_currency_code)
values ('uAykKfsktI', 1, 1, '2021-08-23 12:12:53', '1', 101, 'RUB');
--
