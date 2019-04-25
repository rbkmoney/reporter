truncate rpt.payment restart identity cascade;
truncate rpt.adjustment restart identity cascade;
truncate rpt.payout restart identity cascade;
truncate rpt.refund restart identity cascade;

insert into rpt.payment (event_created_at, event_type, change_id, sequence_id, invoice_id, party_id, party_shop_id,
                         payment_id, payment_created_at, payment_domain_revision,
                         payment_status, payment_payer_type, payment_tool, payment_amount, payment_origin_amount,
                         payment_currency_code, payment_flow, payment_cash_flow)
values ('2017-08-23 08:30:56.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 1, 'uWIbtnV7h2',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1',
        '1', '2017-08-23 08:30:34.000000', 1, 'captured', 'payment_resource', 'bank_card', 1000, 1000, 'RUB', 'instant',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 20,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 5,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       ('2017-08-24 16:13:24.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 2, 'uAykKfsktM',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1',
        '1', '2017-08-23 12:12:53.000000', 1, 'captured', 'payment_resource', 'bank_card', 1000, 1000, 'RUB', 'instant',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 20,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 5,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       ('2017-08-30 08:30:56.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 3, 'uWIOyeV7h3',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1',
        '1', '2017-08-30 08:30:34.000000', 1, 'captured', 'payment_resource', 'bank_card', 1000, 1000, 'RUB', 'instant',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 20,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 5,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       ('2017-09-23 08:30:56.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 4, 'qWIbtnV7h2',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1',
        '1', '2017-09-23 08:30:34.000000', 1, 'captured', 'payment_resource', 'bank_card', 2000, 2000, 'RUB', 'instant',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 25,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 25,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       ('2017-09-24 16:13:24.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 5, 'qAyoGtbktM',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1',
        '1', '2017-09-23 12:12:53.000000', 1, 'captured', 'payment_resource', 'bank_card', 2000, 2000, 'RUB', 'instant',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 25,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 25,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       ('2017-09-30 08:30:56.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 6, 'qWIOyeV7h3',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1',
        '1', '2017-09-30 08:30:34.000000', 1, 'captured', 'payment_resource', 'bank_card', 2000, 2000, 'RUB', 'instant',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 25,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 25,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }');

insert into rpt.adjustment(event_created_at, event_type, change_id, sequence_id, invoice_id,
                           payment_id, party_id, party_shop_id, adjustment_id, adjustment_status,
                           adjustment_created_at, adjustment_reason, adjustment_cash_flow)
values ('2017-08-24 16:13:24', 'INVOICE_PAYMENT_ADJUSTMENT_STATUS_CHANGED', 1,
        1, 'uAykKfsktM', '1', 'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1',
        '1', 'captured', '2021-08-23 12:12:52', 'kek',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 20,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 3,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       ('2017-08-24 16:13:24', 'INVOICE_PAYMENT_ADJUSTMENT_STATUS_CHANGED', 1,
        2, 'uAykKfsktM', '1', 'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_2',
        '1', 'captured', '2021-08-23 12:12:52', 'kek',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 20,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 3,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }');

insert into rpt.payout(event_id, event_created_at, event_type, event_category, payout_id, party_id, party_shop_id,
                       contract_id, payout_created_at, payout_status, payout_amount, payout_currency_code, payout_type,
                       payout_cash_flow)
values (1013, '2017-08-28 13:06:46', 'PAYOUT_STATUS_CHANGED', 'PAYOUT', 1014, 'db79ad6c-a507-43ed-9ecf-3bbd88475b32',
        'test_shop_1',
        '1', '2017-08-28 06:00:01', 'paid', 950, 'RUB', 'bank_account',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 25,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 25,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       (1015, '2017-09-04 13:06:46', 'PAYOUT_STATUS_CHANGED', 'PAYOUT', 1016, 'db79ad6c-a507-43ed-9ecf-3bbd88475b32',
        'test_shop_1',
        '1', '2017-09-04 06:00:01', 'paid', 975, 'RUB', 'bank_account',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 20,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 5,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       (1017, '2017-09-27 13:06:46', 'PAYOUT_STATUS_CHANGED', 'PAYOUT', 1018, 'db79ad6c-a507-43ed-9ecf-3bbd88475b32',
        'test_shop_1',
        '1', '2017-09-27 06:00:01', 'paid', 1900, 'RUB', 'bank_account',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 50,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 50,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       (1018, '2017-09-27 13:06:46', 'PAYOUT_STATUS_CHANGED', 'PAYOUT', 1019, 'db79ad6c-a507-43ed-9ecf-3bbd88475b32',
        'test_shop_1',
        '1', '2017-09-27 06:00:01', 'unpaid', 300, 'RUB', 'bank_account',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 400,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 300,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       (1019, '2017-09-27 13:06:46', 'PAYOUT_STATUS_CHANGED', 'PAYOUT', 1020, 'db79ad6c-a507-43ed-9ecf-3bbd88475b32',
        'test_shop_1',
        '1', '2017-09-27 06:00:01', 'cancelled', 300, 'RUB', 'bank_account',
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 300,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 400,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }');

insert into rpt.refund(event_created_at, event_type, change_id, sequence_id, invoice_id,
                       payment_id, party_id, party_shop_id, refund_id, refund_status, refund_created_at,
                       refund_currency_code,
                       refund_amount, refund_cash_flow)
values ('2017-08-24 16:13:23', 'INVOICE_PAYMENT_REFUND_CREATED', 1, 1, 'uAykKfsktM', '1',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', '1', 'succeeded', '2017-08-23 13:06:46', 'RUB', 1000,
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       ('2017-08-24 16:13:23', 'INVOICE_PAYMENT_REFUND_CREATED', 1, 2, 'uAykKfsktM', '1',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', '1', 'succeeded', '2017-08-23 13:06:46', 'RUB', 1000,
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       ('2017-09-24 16:13:23', 'INVOICE_PAYMENT_REFUND_CREATED', 1, 3, 'qAyoGtbktM', '1',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', '1', 'succeeded', '2017-08-23 13:06:46', 'RUB', 1000,
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       ('2017-09-24 16:13:23', 'INVOICE_PAYMENT_REFUND_CREATED', 1, 4, 'qAyoGtbktM', '1',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1', '1', 'succeeded', '2017-08-23 13:06:46', 'RUB', 1000,
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }'),
       ('2017-09-24 16:13:23', 'INVOICE_PAYMENT_REFUND_CREATED', 1, 5, 'qAyoGtbktM', '1',
        'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_2', '1', 'succeeded', '2017-08-23 13:06:46', 'RUB', 1000,
        '{
          "cash_flows": [
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "ProviderCashFlowAccount",
                  "provider": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 500,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            },
            {
              "source": {
                "account_id": 13444,
                "account_type": {
                  "@type": "MerchantCashFlowAccount",
                  "merchant": "settlement"
                }
              },
              "volume": {
                "amount": 1000,
                "currency": {
                  "symbolic_code": "RUB"
                }
              },
              "destination": {
                "account_id": 6,
                "account_type": {
                  "@type": "SystemCashFlowAccount",
                  "system": "settlement"
                }
              }
            }
          ]
        }');
