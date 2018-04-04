package com.rbkmoney.reporter.model;

import com.rbkmoney.damsel.domain.CategoryType;

import java.util.Date;

/**
 * Created by tolkonepiu on 17/07/2017.
 */
public class PartyModel {

    private String merchantId;

    private String merchantName;

    private String merchantContractId;

    private Date merchantContractSignedAt;

    private String merchantRepresentativePosition;

    private String merchantRepresentativeFullName;

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

    public Date getMerchantContractSignedAt() {
        return merchantContractSignedAt;
    }

    public void setMerchantContractSignedAt(Date merchantContractSignedAt) {
        this.merchantContractSignedAt = merchantContractSignedAt;
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

    @Override
    public String toString() {
        return "PartyModel{" +
                "merchantId='" + merchantId + '\'' +
                ", merchantName='" + merchantName + '\'' +
                ", merchantContractId='" + merchantContractId + '\'' +
                ", merchantContractSignedAt=" + merchantContractSignedAt +
                ", merchantRepresentativePosition='" + merchantRepresentativePosition + '\'' +
                ", merchantRepresentativeFullName='" + merchantRepresentativeFullName + '\'' +
                '}';
    }
}
