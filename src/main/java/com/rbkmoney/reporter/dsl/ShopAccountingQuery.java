package com.rbkmoney.reporter.dsl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by tolkonepiu on 14/07/2017.
 */
public class ShopAccountingQuery {

    @JsonProperty("merchant_id")
    String merchantId;

    @JsonProperty("contract_id")
    String contractId;

    @JsonProperty("currency_code")
    String currencyCode;

    @JsonProperty("from_time")
    Optional<Instant> fromTime;

    @JsonProperty("to_time")
    Instant toTime;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Optional<Instant> getFromTime() {
        return fromTime;
    }

    public void setFromTime(Optional<Instant> fromTime) {
        this.fromTime = fromTime;
    }

    public Instant getToTime() {
        return toTime;
    }

    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShopAccountingQuery that = (ShopAccountingQuery) o;
        return Objects.equals(merchantId, that.merchantId) &&
                Objects.equals(contractId, that.contractId) &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(fromTime, that.fromTime) &&
                Objects.equals(toTime, that.toTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merchantId, contractId, currencyCode, fromTime, toTime);
    }

    @Override
    public String toString() {
        return "ShopAccountingQuery{" +
                "merchantId='" + merchantId + '\'' +
                ", contractId='" + contractId + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", fromTime=" + fromTime +
                ", toTime=" + toTime +
                '}';
    }
}
