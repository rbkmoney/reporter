package com.rbkmoney.reporter.util;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinalCashFlow {

    @JsonProperty("cash_flow")
    private List<FinalCashFlowPosting> cashFlows;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"source", "destination", "volume", "details"})
    public static class FinalCashFlowPosting {

        private FinalCashFlowAccount source;
        private FinalCashFlowAccount destination;
        private Cash volume;
        private String details;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({"account_type", "account_id"})
        public static class FinalCashFlowAccount {

            @JsonProperty("account_type")
            private CashFlowAccount accountType;
            @JsonProperty("account_id")
            private Long accountId;

            @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
            @JsonSubTypes({
                    @JsonSubTypes.Type(value = CashFlowAccount.MerchantCashFlowAccount.class, name = "MerchantCashFlowAccount"),
                    @JsonSubTypes.Type(value = CashFlowAccount.ProviderCashFlowAccount.class, name = "ProviderCashFlowAccount"),
                    @JsonSubTypes.Type(value = CashFlowAccount.SystemCashFlowAccount.class, name = "SystemCashFlowAccount"),
                    @JsonSubTypes.Type(value = CashFlowAccount.ExternalCashFlowAccount.class, name = "ExternalCashFlowAccount"),
                    @JsonSubTypes.Type(value = CashFlowAccount.WalletCashFlowAccount.class, name = "WalletCashFlowAccount"),
            })
            public static abstract class CashFlowAccount {

                public interface CashFlowAccountType {

                }

                @Data
                @ToString(callSuper = true)
                @EqualsAndHashCode(callSuper = true)
                @JsonIgnoreProperties(ignoreUnknown = true)
                @JsonInclude(JsonInclude.Include.NON_NULL)
                public static class MerchantCashFlowAccount extends CashFlowAccount {

                    private MerchantCashFlowAccountType merchant;

                    public enum MerchantCashFlowAccountType implements CashFlowAccountType {

                        @JsonProperty("settlement")
                        SETTLEMENT,
                        @JsonProperty("guarantee")
                        GUARANTEE,
                        @JsonProperty("payout")
                        PAYOUT

                    }
                }

                @Data
                @ToString(callSuper = true)
                @EqualsAndHashCode(callSuper = true)
                @JsonIgnoreProperties(ignoreUnknown = true)
                @JsonInclude(JsonInclude.Include.NON_NULL)
                public static class ProviderCashFlowAccount extends CashFlowAccount {

                    private ProviderCashFlowAccountType provider;

                    public enum ProviderCashFlowAccountType implements CashFlowAccountType {

                        @JsonProperty("settlement")
                        SETTLEMENT

                    }
                }

                @Data
                @ToString(callSuper = true)
                @EqualsAndHashCode(callSuper = true)
                @JsonIgnoreProperties(ignoreUnknown = true)
                @JsonInclude(JsonInclude.Include.NON_NULL)
                public static class SystemCashFlowAccount extends CashFlowAccount {

                    private SystemCashFlowAccountType system;

                    public enum SystemCashFlowAccountType implements CashFlowAccountType {

                        @JsonProperty("settlement")
                        SETTLEMENT,
                        @JsonProperty("subagent")
                        SUBAGENT

                    }
                }

                @Data
                @ToString(callSuper = true)
                @EqualsAndHashCode(callSuper = true)
                @JsonIgnoreProperties(ignoreUnknown = true)
                @JsonInclude(JsonInclude.Include.NON_NULL)
                public static class ExternalCashFlowAccount extends CashFlowAccount {

                    private ExternalCashFlowAccountType external;

                    public enum ExternalCashFlowAccountType implements CashFlowAccountType {

                        @JsonProperty("income")
                        INCOME,
                        @JsonProperty("outcome")
                        OUTCOME

                    }
                }

                @Data
                @ToString(callSuper = true)
                @EqualsAndHashCode(callSuper = true)
                @JsonIgnoreProperties(ignoreUnknown = true)
                @JsonInclude(JsonInclude.Include.NON_NULL)
                public static class WalletCashFlowAccount extends CashFlowAccount {

                    private WalletCashFlowAccountType wallet;

                    public enum WalletCashFlowAccountType implements CashFlowAccountType {

                        @JsonProperty("sender_source")
                        SENDER_SOURCE,
                        @JsonProperty("sender_settlement")
                        SENDER_SETTLEMENT,
                        @JsonProperty("receiver_settlement")
                        RECEIVER_SETTLEMENT,
                        @JsonProperty("receiver_destination")
                        RECEIVER_DESTINATION

                    }
                }
            }
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({"amount", "currency"})
        public static class Cash {

            private Long amount;
            private CurrencyRef currency;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class CurrencyRef {

                @JsonProperty("symbolic_code")
                private String symbolicCode;

            }
        }
    }
}
