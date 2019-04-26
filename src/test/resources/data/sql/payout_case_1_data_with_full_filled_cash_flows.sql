truncate rpt.payout restart identity cascade;

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
