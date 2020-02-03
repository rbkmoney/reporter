package com.rbkmoney.reporter.dao.mapper.dto;

import com.rbkmoney.reporter.domain.enums.PaymentTool;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentRegistryReportData {

    private Long id;
    private LocalDateTime eventCreatedAt;
    private UUID partyId;
    private String partyShopId;
    private String invoiceId;
    private String paymentId;
    private PaymentTool paymentTool;
    private String paymentEmail;
    private Long paymentAmount;
    private Long paymentFee;
    private Long paymentExternalFee;
    private Long paymentProviderFee;
    private String invoiceProduct;
    private String currencySymbolicCode;

}
