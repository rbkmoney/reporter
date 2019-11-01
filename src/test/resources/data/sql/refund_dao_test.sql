truncate rpt.invoice restart identity cascade;
truncate rpt.payment restart identity cascade;
truncate rpt.payment_state restart identity cascade;
truncate rpt.refund restart identity cascade;
truncate rpt.refund_state restart identity cascade;

-- test getShopAccountingReportData

--  status
insert into rpt.refund (party_id, party_shop_id, invoice_id, payment_id, refund_id, created_at,
                        amount, currency_code, fee, fee_currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', 'uAykKfsktM', '1', '1', '2021-08-23 12:12:52', 123,
        'RUB', 112, 'RUB');

insert into rpt.refund_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, refund_id, status)
values ('uAykKfsktM', 1, 1, '2021-08-23 12:12:52', '1', '1', 'pending');
--

insert into rpt.refund (party_id, party_shop_id, invoice_id, payment_id, refund_id, created_at,
                        amount, currency_code, fee, fee_currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', 'uAykKfsktN', '1', '1', '2021-08-23 12:12:53', 123,
        'RUB', 112, 'RUB');

insert into rpt.refund_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, refund_id, status)
values ('uAykKfsktN', 1, 1, '2021-08-23 12:12:52', '1', '1', 'pending');

insert into rpt.refund_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, refund_id, status)
values ('uAykKfsktN', 1, 2, '2021-08-23 12:12:53', '1', '1', 'succeeded');

-- party_id
insert into rpt.refund (party_id, party_shop_id, invoice_id, payment_id, refund_id, created_at,
                        amount, currency_code, fee, fee_currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b33', 'test_shop_1', 'uAykKfsktR', '1', '1', '2021-08-23 12:12:54', 123,
        'RUB', 112, 'RUB');

insert into rpt.refund_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, refund_id, status)
values ('uAykKfsktR', 1, 1, '2021-08-23 12:12:52', '1', '1', 'succeeded');
--

insert into rpt.refund (party_id, party_shop_id, invoice_id, payment_id, refund_id, created_at,
                        amount, currency_code, fee, fee_currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', 'uAykKfsktJ', '1', '1', '2021-08-23 12:12:55', 123,
        'RUB', 112, 'RUB');

insert into rpt.refund_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, refund_id, status)
values ('uAykKfsktJ', 1, 2, '2021-08-23 12:12:53', '1', '1', 'succeeded');

-- test getRefundPaymentRegistryReportData

insert into rpt.invoice(party_id, party_shop_id, invoice_id, created_at, product, due, amount, currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktW', '2021-08-23 12:12:52', 'asd',
        '2021-08-23 12:12:52', 123, 'RUB');

insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktW', '1', '2021-08-23 12:12:51', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktW', 1, 1, '2021-08-23 12:12:53', '1', 'captured');

insert into rpt.refund (party_id, party_shop_id, invoice_id, payment_id, refund_id, created_at,
                        amount, currency_code, fee, fee_currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktW', '1', '1', '2021-08-23 12:12:56', 123,
        'RUB', 112, 'RUB');

insert into rpt.refund_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, refund_id, status)
values ('uAykKfsktW', 2, 1, '2021-08-23 12:12:53', '1', '1', 'succeeded');

--  status
insert into rpt.refund (party_id, party_shop_id, invoice_id, payment_id, refund_id, created_at,
                        amount, currency_code, fee, fee_currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktT', '1', '1', '2021-08-23 12:12:57', 123,
        'RUB', 112, 'RUB');

insert into rpt.refund_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, refund_id, status)
values ('uAykKfsktT', 1, 1, '2021-08-23 12:12:53', '1', '1', 'pending');
--

-- status
insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktQ', '1', '2021-08-23 12:12:52', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktQ', 1, 1, '2021-08-23 12:12:53', '1', 'pending');
--

insert into rpt.payment(party_id, party_shop_id, invoice_id, payment_id, created_at, domain_revision, payer_type, tool,
                        flow)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktW', '2', '2021-08-23 12:12:53', 1,
        'payment_resource', 'bank_card', 'instant');

insert into rpt.payment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, status)
values ('uAykKfsktW', 3, 1, '2021-08-23 12:12:53', '2', 'captured');

insert into rpt.refund (party_id, party_shop_id, invoice_id, payment_id, refund_id, created_at,
                        amount, currency_code, fee, fee_currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b35', 'test_shop_1', 'uAykKfsktW', '2', '2', '2021-08-23 12:12:58', 124,
        'RUB', 112, 'RUB');

insert into rpt.refund_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, refund_id, status)
values ('uAykKfsktW', 4, 1, '2021-08-23 12:12:53', '2', '2', 'succeeded');
