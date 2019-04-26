truncate rpt.adjustment restart identity cascade;
truncate rpt.payment restart identity cascade;

insert into rpt.adjustment(event_created_at, event_type, change_id, sequence_id, invoice_id,
                           payment_id, party_id, party_shop_id, adjustment_id, adjustment_status,
                           adjustment_created_at, adjustment_reason, adjustment_cash_flow)
values ('2017-08-24 16:13:24', 'INVOICE_PAYMENT_ADJUSTMENT_STATUS_CHANGED',
        1, 1, 'uAykKfsktM', '1', 'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'test_shop_1',
        '1', 'captured', '2021-08-23 12:12:52', 'kek',
        '{"cash_flows": [{"source": {"account_id": 13444, "account_type": {"@type": "ProviderCashFlowAccount", "provider": "settlement"}}, "volume": {"amount": 500, "currency": {"symbolic_code": "RUB"}}, "destination": {"account_id": 6, "account_type": {"@type": "MerchantCashFlowAccount", "merchant": "settlement"}}}, {"source": {"account_id": 13444, "account_type": {"@type": "ProviderCashFlowAccount", "provider": "settlement"}}, "volume": {"amount": 500, "currency": {"symbolic_code": "RUB"}}, "destination": {"account_id": 6, "account_type": {"@type": "MerchantCashFlowAccount", "merchant": "settlement"}}}, {"source": {"account_id": 13444, "account_type": {"@type": "MerchantCashFlowAccount", "merchant": "settlement"}}, "volume": {"amount": 20, "currency": {"symbolic_code": "RUB"}}, "destination": {"account_id": 6, "account_type": {"@type": "SystemCashFlowAccount", "system": "settlement"}}}, {"source": {"account_id": 13444, "account_type": {"@type": "MerchantCashFlowAccount", "merchant": "settlement"}}, "volume": {"amount": 3, "currency": {"symbolic_code": "RUB"}}, "destination": {"account_id": 6, "account_type": {"@type": "SystemCashFlowAccount", "system": "settlement"}}}]}');

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
       ('2017-08-24 16:13:24.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 11, 'uAykKfsktM',
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
       ('2017-08-30 08:30:56.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 12, 'uWIOyeV7h3',
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
       ('2017-09-23 08:30:56.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 13, 'qWIbtnV7h2',
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
       ('2017-09-24 16:13:24.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 14, 'qAyoGtbktM',
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
       ('2017-09-30 08:30:56.000000', 'INVOICE_PAYMENT_STATUS_CHANGED', 1, 15, 'qWIOyeV7h3',
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
