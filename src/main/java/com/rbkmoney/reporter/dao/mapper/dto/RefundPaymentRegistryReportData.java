package com.rbkmoney.reporter.dao.mapper.dto;

import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.PaymentTool;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RefundPaymentRegistryReportData {

    private Long id;
    private Long eventId;
    private LocalDateTime refundEventCreatedAt;
    private LocalDateTime paymentEventCreatedAt;
    private InvoiceEventType eventType;
    private UUID partyId;
    private String partyShopId;
    private String invoiceId;
    private String paymentId;
    private PaymentTool paymentTool;
    private String paymentEmail;
    private Long refundAmount;
    private String invoiceProduct;

}
