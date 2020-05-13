truncate rpt.adjustment restart identity cascade;
truncate rpt.adjustment_state restart identity cascade;

insert into rpt.adjustment (party_id, party_shop_id, invoice_id, payment_id, adjustment_id, created_at, reason,
                            fee, fee_currency_code, old_fee, old_fee_currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', 'uAykKfsktM', '1', '1', '2021-08-23 12:12:52', 'kek',
        123, 'RUB', 122, 'RUB');

insert into rpt.adjustment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, adjustment_id,
                                  status)
values ('uAykKfsktM', 1, 1, '2021-08-23 12:12:52', '1', '1', 'pending');
insert into rpt.adjustment_state (invoice_id, sequence_id, change_id, event_created_at, payment_id, adjustment_id,
                                  status)
values ('uAykKfsktM', 2, 1, '2021-08-23 12:12:53', '1', '1', 'captured');
