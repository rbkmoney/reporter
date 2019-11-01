truncate rpt.payout restart identity cascade;
truncate rpt.payout_state restart identity cascade;

insert into rpt.payout (party_id, party_shop_id, payout_id, contract_id, created_at, amount, fee, currency_code, type)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', '1', '1', '2021-08-23 12:12:52', 100, 98, 'RUB',
        'bank_card');

insert into rpt.payout_state (event_id, event_created_at, payout_id, status)
values (1, '2021-08-23 12:12:52', '1', 'paid');

insert into rpt.payout (party_id, party_shop_id, payout_id, contract_id, created_at, amount, fee, currency_code, type)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', '2', '1', '2021-08-23 12:12:53', 100, 99, 'RUB',
        'bank_card');

insert into rpt.payout_state (event_id, event_created_at, payout_id, status)
values (1, '2021-08-23 12:12:52', '2', 'cancelled');

insert into rpt.payout (party_id, party_shop_id, payout_id, contract_id, created_at, amount, fee, currency_code, type)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', '3', '1', '2021-08-23 12:12:54', 120, 99, 'RUB',
        'bank_card');

insert into rpt.payout_state (event_id, event_created_at, payout_id, status)
values (1, '2021-08-23 12:12:52', '3', 'confirmed');
