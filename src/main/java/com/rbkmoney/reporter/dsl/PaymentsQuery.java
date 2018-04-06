package com.rbkmoney.reporter.dsl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rbkmoney.damsel.merch_stat.InvoicePaymentStatus;

import java.time.Instant;

public class PaymentsQuery {

    @JsonProperty("merchant_id")
    String merchantId;

    @JsonProperty("contract_id")
    String contractId;

    @JsonProperty("invoice_id")
    String invoiceId;

    @JsonProperty("payment_id")
    String paymentId;

    @JsonProperty("from_time")
    Instant fromTime;

    @JsonProperty("to_time")
    Instant toTime;

    @JsonProperty("payment_status")
    String paymentStatus;

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

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentsQuery that = (PaymentsQuery) o;

        if (merchantId != null ? !merchantId.equals(that.merchantId) : that.merchantId != null) return false;
        if (contractId != null ? !contractId.equals(that.contractId) : that.contractId != null) return false;
        if (invoiceId != null ? !invoiceId.equals(that.invoiceId) : that.invoiceId != null) return false;
        if (paymentId != null ? !paymentId.equals(that.paymentId) : that.paymentId != null) return false;
        if (fromTime != null ? !fromTime.equals(that.fromTime) : that.fromTime != null) return false;
        if (toTime != null ? !toTime.equals(that.toTime) : that.toTime != null) return false;
        return paymentStatus != null ? paymentStatus.equals(that.paymentStatus) : that.paymentStatus == null;
    }

    @Override
    public int hashCode() {
        int result = merchantId != null ? merchantId.hashCode() : 0;
        result = 31 * result + (contractId != null ? contractId.hashCode() : 0);
        result = 31 * result + (fromTime != null ? fromTime.hashCode() : 0);
        result = 31 * result + (invoiceId != null ? invoiceId.hashCode() : 0);
        result = 31 * result + (paymentId != null ? paymentId.hashCode() : 0);
        result = 31 * result + (toTime != null ? toTime.hashCode() : 0);
        result = 31 * result + (paymentStatus != null ? paymentStatus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PaymentsQuery{" +
                "merchantId='" + merchantId + '\'' +
                ", contractId='" + contractId + '\'' +
                ", invoiceId='" + invoiceId + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", fromTime=" + fromTime +
                ", toTime=" + toTime +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}
