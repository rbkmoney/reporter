package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.batch.impl.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.mapper.InvoiceBatchMapper;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.service.InvoiceService;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentInvoiceBatchMapperImpl implements InvoiceBatchMapper<Payment, Invoice> {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    @Override
    public Payment map(InvoiceChangeMapper mapper, MapperPayload payload, List<Payment> payments) {
        throw new UnsupportedOperationException("Payment need cache");
    }

    @Override
    public Payment map(InvoiceChangeMapper mapper, MapperPayload payload, List<Payment> payments, Map<InvoiceUniqueBatchKey, Invoice> consumerCache) {
        String invoiceId = payload.getMachineEvent().getSourceId();
        String paymentId = payload.getInvoiceChange().getInvoicePaymentChange().getId();

        Payment payment = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId()).getPayment();

        if (!payments.isEmpty()) {
            Payment lastPayment = payments.get(payments.size() - 1);
            copyProperties(mapper, payment, lastPayment);
        } else if (payment.getEventType() != InvoiceEventType.INVOICE_PAYMENT_STARTED) {
            Payment lastPayment = paymentService.get(invoiceId, paymentId);
            copyProperties(mapper, payment, lastPayment);
        }

        if (payment.getEventType() == InvoiceEventType.INVOICE_PAYMENT_STARTED) {
            Invoice invoice = consumerCache.computeIfAbsent(
                    new InvoiceUniqueBatchKeyImpl(invoiceId),
                    key -> {
                        PartyData partyData = invoiceService.getPartyData(invoiceId);

                        Invoice inv = new Invoice();
                        inv.setPartyId(partyData.getPartyId());
                        inv.setPartyShopId(partyData.getPartyShopId());
                        return inv;
                    }
            );

            payment.setPartyId(invoice.getPartyId());
            payment.setPartyShopId(invoice.getPartyShopId());
        }

        return payment;
    }

    private void copyProperties(InvoiceChangeMapper mapper, Payment payment, Payment lastPayment) {
        BeanUtils.copyProperties(lastPayment, payment, mapper.getIgnoreProperties());
        if (payment.getPaymentAmount() == null) {
            payment.setPaymentAmount(lastPayment.getPaymentAmount());
        }
        if (payment.getPaymentCurrencyCode() == null) {
            payment.setPaymentCurrencyCode(lastPayment.getPaymentCurrencyCode());
        }
    }
}
