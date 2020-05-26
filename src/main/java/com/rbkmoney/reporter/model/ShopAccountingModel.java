package com.rbkmoney.reporter.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class ShopAccountingModel {

    @NotNull
    private String merchantId;

    @NotNull
    private String shopId;

    @NotNull
    private String currencyCode;

    @Min(0)
    private long fundsAcquired;

    @Min(0)
    private long feeCharged;

    private long fundsAdjusted;

    @Min(0)
    private long fundsPaidOut;

    @Min(0)
    private long fundsRefunded;

    @Min(0)
    private long fundsReturned;

    public long getAvailableFunds() {
        return fundsAcquired + fundsAdjusted - feeCharged - fundsPaidOut - fundsRefunded - fundsReturned;
    }

}
