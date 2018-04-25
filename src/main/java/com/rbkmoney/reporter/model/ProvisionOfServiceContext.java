package com.rbkmoney.reporter.model;

import java.time.LocalDateTime;

public class ProvisionOfServiceContext {

    String representativePosition;

    String representativeFullName;

    boolean needReference;

    String representativeDocument;

    String legalAgreementId;

    LocalDateTime legalAgreementSignedAt;

    LocalDateTime legalAgreementValidUntil;

    Long openingBalance;

    public String getRepresentativePosition() {
        return representativePosition;
    }

    public void setRepresentativePosition(String representativePosition) {
        this.representativePosition = representativePosition;
    }

    public String getRepresentativeFullName() {
        return representativeFullName;
    }

    public void setRepresentativeFullName(String representativeFullName) {
        this.representativeFullName = representativeFullName;
    }

    public boolean isNeedReference() {
        return needReference;
    }

    public void setNeedReference(boolean needReference) {
        this.needReference = needReference;
    }

    public String getRepresentativeDocument() {
        return representativeDocument;
    }

    public void setRepresentativeDocument(String representativeDocument) {
        this.representativeDocument = representativeDocument;
    }

    public String getLegalAgreementId() {
        return legalAgreementId;
    }

    public void setLegalAgreementId(String legalAgreementId) {
        this.legalAgreementId = legalAgreementId;
    }

    public LocalDateTime getLegalAgreementSignedAt() {
        return legalAgreementSignedAt;
    }

    public void setLegalAgreementSignedAt(LocalDateTime legalAgreementSignedAt) {
        this.legalAgreementSignedAt = legalAgreementSignedAt;
    }

    public LocalDateTime getLegalAgreementValidUntil() {
        return legalAgreementValidUntil;
    }

    public void setLegalAgreementValidUntil(LocalDateTime legalAgreementValidUntil) {
        this.legalAgreementValidUntil = legalAgreementValidUntil;
    }

    public Long getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(Long openingBalance) {
        this.openingBalance = openingBalance;
    }
}
