package com.rbkmoney.reporter.dsl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

public class InvoicesQuery {

    @JsonProperty("merchant_id")
    String merchantId;

    @JsonProperty("shop_id")
    String shopId;

    @JsonProperty("invoice_id")
    String invoiceId;

    @JsonProperty("from_time")
    Instant fromTime;

    @JsonProperty("to_time")
    Instant toTime;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Instant getFromTime() {
        return fromTime;
    }

    public void setFromTime(Instant fromTime) {
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
        InvoicesQuery that = (InvoicesQuery) o;
        return Objects.equals(merchantId, that.merchantId) &&
                Objects.equals(shopId, that.shopId) &&
                Objects.equals(invoiceId, that.invoiceId) &&
                Objects.equals(fromTime, that.fromTime) &&
                Objects.equals(toTime, that.toTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merchantId, shopId, invoiceId, fromTime, toTime);
    }

    @Override
    public String toString() {
        return "InvoicesQuery{" +
                "merchantId='" + merchantId + '\'' +
                ", shopId='" + shopId + '\'' +
                ", invoiceId='" + invoiceId + '\'' +
                ", fromTime=" + fromTime +
                ", toTime=" + toTime +
                '}';
    }
}