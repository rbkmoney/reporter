package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.batch.impl.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.dao.query.PaymentQueryTemplator;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.*;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.service.InvoiceService;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentQueryManager {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final PaymentQueryTemplator paymentQueryTemplator;

    public List<Query> map(InvoiceChangeMapper mapper,
                           MapperPayload payload,
                           List<Payment> payments,
                           Map<InvoiceUniqueBatchKey, Invoice> consumerCache) {
        List<Query> queries = new ArrayList<>();
        String invoiceId = payload.getMachineEvent().getSourceId();
        String paymentId = payload.getInvoiceChange().getInvoicePaymentChange().getId();

        MapperResult mapperResult = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId());
        if (mapperResult.getPayment() != null) {
            Payment payment = mapperResult.getPayment();
            if (payment.getEventType() == InvoiceEventType.INVOICE_PAYMENT_STARTED) {
                Invoice invoice = consumerCache.computeIfAbsent(
                        new InvoiceUniqueBatchKeyImpl(invoiceId),
                        key -> getPartyData(invoiceId)
                );
                payment.setPartyId(invoice.getPartyId());
                payment.setPartyShopId(invoice.getPartyShopId());
            }
            payments.add(payment);
            Query savePaymentQuery = paymentQueryTemplator.getSavePaymentQuery(payment);
            queries.add(savePaymentQuery);
        }
        if (mapperResult.getPaymentState() != null) {
            PaymentState paymentState = mapperResult.getPaymentState();
            Query savePaymentStateQuery = paymentQueryTemplator.getSavePaymentStateQuery(paymentState);
            queries.add(savePaymentStateQuery);
        }
        if (mapperResult.getPaymentCost() != null) {
            PaymentCost paymentCost = mapperResult.getPaymentCost();
            Query savePaymentCostQuery = paymentQueryTemplator.getSavePaymentCostQuery(paymentCost);
            queries.add(savePaymentCostQuery);
        }
        if (mapperResult.getPaymentRouting() != null) {
            PaymentRouting paymentRouting = mapperResult.getPaymentRouting();
            Query savePaymentRoutingQuery = paymentQueryTemplator.getSavePaymentRoutingQuery(paymentRouting);
            queries.add(savePaymentRoutingQuery);
        }
        if (mapperResult.getPaymentShortId() != null) {
            PaymentShortId paymentShortId = mapperResult.getPaymentShortId();
            Query savePaymentTerminalQuery = paymentQueryTemplator.getSavePaymentTerminalQuery(paymentShortId);
            queries.add(savePaymentTerminalQuery);
        }

        return queries;
    }

    private Invoice getPartyData(String invoiceId) {
        PartyData partyData = invoiceService.getPartyData(invoiceId);

        Invoice inv = new Invoice();
        inv.setPartyId(partyData.getPartyId());
        inv.setPartyShopId(partyData.getPartyShopId());
        return inv;
    }

}
