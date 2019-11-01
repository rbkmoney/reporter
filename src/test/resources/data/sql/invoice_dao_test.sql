truncate rpt.invoice restart identity cascade;

insert into rpt.invoice(party_id, party_shop_id, invoice_id, created_at, product, due, amount, currency_code)
values ('db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', 'uAykKfsktM', '2021-08-23 12:12:52', 'asd',
        '2021-08-23 12:12:52', 123, 'RUB');
