package com.rbkmoney.reporter.model;

import java.util.Date;

/**
 * Created by tolkonepiu on 14/07/2017.
 */
public class ShopAccounting {

    private String merchantId;
    private String merchantName;
    private String merchantContractId;
    private Date merchantContractCreatedAt;
    private String merchantRepresentativePosition;
    private String merchantRepresentativeFullName;
    private String shopId;
    private String currencyCode;
    private double fundsAcquired;
    private double feeCharged;
    private double openingBalance;
    private double closingBalance;
    private Date fromTime;
    private Date toTime;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantContractId() {
        return merchantContractId;
    }

    public void setMerchantContractId(String merchantContractId) {
        this.merchantContractId = merchantContractId;
    }

    public Date getMerchantContractCreatedAt() {
        return merchantContractCreatedAt;
    }

    public void setMerchantContractCreatedAt(Date merchantContractCreatedAt) {
        this.merchantContractCreatedAt = merchantContractCreatedAt;
    }

    public String getMerchantRepresentativePosition() {
        return merchantRepresentativePosition;
    }

    public void setMerchantRepresentativePosition(String merchantRepresentativePosition) {
        this.merchantRepresentativePosition = merchantRepresentativePosition;
    }

    public String getMerchantRepresentativeFullName() {
        return merchantRepresentativeFullName;
    }

    public void setMerchantRepresentativeFullName(String merchantRepresentativeFullName) {
        this.merchantRepresentativeFullName = merchantRepresentativeFullName;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public double getFundsAcquired() {
        return fundsAcquired;
    }

    public void setFundsAcquired(double fundsAcquired) {
        this.fundsAcquired = fundsAcquired;
    }

    public double getFeeCharged() {
        return feeCharged;
    }

    public void setFeeCharged(double feeCharged) {
        this.feeCharged = feeCharged;
    }

    public double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public double getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(double closingBalance) {
        this.closingBalance = closingBalance;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopAccounting that = (ShopAccounting) o;

        if (Double.compare(that.fundsAcquired, fundsAcquired) != 0) return false;
        if (Double.compare(that.feeCharged, feeCharged) != 0) return false;
        if (Double.compare(that.openingBalance, openingBalance) != 0) return false;
        if (Double.compare(that.closingBalance, closingBalance) != 0) return false;
        if (merchantId != null ? !merchantId.equals(that.merchantId) : that.merchantId != null) return false;
        if (merchantName != null ? !merchantName.equals(that.merchantName) : that.merchantName != null) return false;
        if (merchantContractId != null ? !merchantContractId.equals(that.merchantContractId) : that.merchantContractId != null)
            return false;
        if (merchantContractCreatedAt != null ? !merchantContractCreatedAt.equals(that.merchantContractCreatedAt) : that.merchantContractCreatedAt != null)
            return false;
        if (merchantRepresentativePosition != null ? !merchantRepresentativePosition.equals(that.merchantRepresentativePosition) : that.merchantRepresentativePosition != null)
            return false;
        if (merchantRepresentativeFullName != null ? !merchantRepresentativeFullName.equals(that.merchantRepresentativeFullName) : that.merchantRepresentativeFullName != null)
            return false;
        if (shopId != null ? !shopId.equals(that.shopId) : that.shopId != null) return false;
        if (currencyCode != null ? !currencyCode.equals(that.currencyCode) : that.currencyCode != null) return false;
        if (fromTime != null ? !fromTime.equals(that.fromTime) : that.fromTime != null) return false;
        return toTime != null ? toTime.equals(that.toTime) : that.toTime == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = merchantId != null ? merchantId.hashCode() : 0;
        result = 31 * result + (merchantName != null ? merchantName.hashCode() : 0);
        result = 31 * result + (merchantContractId != null ? merchantContractId.hashCode() : 0);
        result = 31 * result + (merchantContractCreatedAt != null ? merchantContractCreatedAt.hashCode() : 0);
        result = 31 * result + (merchantRepresentativePosition != null ? merchantRepresentativePosition.hashCode() : 0);
        result = 31 * result + (merchantRepresentativeFullName != null ? merchantRepresentativeFullName.hashCode() : 0);
        result = 31 * result + (shopId != null ? shopId.hashCode() : 0);
        result = 31 * result + (currencyCode != null ? currencyCode.hashCode() : 0);
        temp = Double.doubleToLongBits(fundsAcquired);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(feeCharged);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(openingBalance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(closingBalance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (fromTime != null ? fromTime.hashCode() : 0);
        result = 31 * result + (toTime != null ? toTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ShopAccounting{" +
                "merchantId='" + merchantId + '\'' +
                ", merchantName='" + merchantName + '\'' +
                ", merchantContractId='" + merchantContractId + '\'' +
                ", merchantContractCreatedAt=" + merchantContractCreatedAt +
                ", merchantRepresentativePosition='" + merchantRepresentativePosition + '\'' +
                ", merchantRepresentativeFullName='" + merchantRepresentativeFullName + '\'' +
                ", shopId='" + shopId + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", fundsAcquired=" + fundsAcquired +
                ", feeCharged=" + feeCharged +
                ", openingBalance=" + openingBalance +
                ", closingBalance=" + closingBalance +
                ", fromTime=" + fromTime +
                ", toTime=" + toTime +
                '}';
    }
}
