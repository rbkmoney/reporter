package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.mapper.InvoiceBatchMapper;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.service.PaymentService;
import com.rbkmoney.reporter.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RefundInvoiceBatchMapperImpl implements InvoiceBatchMapper<Refund, Payment> {

    private final PaymentService paymentService;
    private final RefundService refundService;

    @Override
    public Refund map(InvoiceChangeMapper mapper, MapperPayload payload, List<Refund> refunds) {
        throw new UnsupportedOperationException("Refund need cache");
    }

    @Override
    public Refund map(InvoiceChangeMapper mapper, MapperPayload payload, List<Refund> refunds, Map<InvoiceUniqueBatchKey, Payment> consumerCache) {
        String invoiceId = payload.getMachineEvent().getSourceId();
        String paymentId = payload.getInvoiceChange().getInvoicePaymentChange().getId();
        String refundId = payload.getInvoiceChange().getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getId();

        Refund refund = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId()).getRefund();

        if (!refunds.isEmpty()) {
            Refund lastRefund = refunds.get(refunds.size() - 1);
            BeanUtils.copyProperties(lastRefund, refund, mapper.getIgnoreProperties());
        } else if (refund.getEventType() != InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED) {
            Refund lastRefund = refundService.get(invoiceId, paymentId, refundId);
            BeanUtils.copyProperties(lastRefund, refund, mapper.getIgnoreProperties());
        }

        if (refund.getEventType() == InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED) {
            Payment payment;
            InvoiceUniqueBatchKey uniqueBatchKey = new PaymentInvoiceUniqueBatchKey(invoiceId, paymentId);
            if (consumerCache.containsKey(uniqueBatchKey)) {
                payment = consumerCache.get(uniqueBatchKey);
            } else {
                PaymentPartyData paymentPartyData = paymentService.getPaymentPartyData(invoiceId, paymentId);

                payment = new Payment();
                payment.setPartyId(paymentPartyData.getPartyId());
                payment.setPartyShopId(paymentPartyData.getPartyShopId());
                payment.setPaymentAmount(paymentPartyData.getPaymentAmount());
                payment.setPaymentCurrencyCode(paymentPartyData.getPaymentCurrencyCode());

                consumerCache.put(uniqueBatchKey, payment);
            }

            refund.setPartyId(payment.getPartyId());
            refund.setPartyShopId(payment.getPartyShopId());
            if (refund.getRefundAmount() == null) {
                refund.setRefundAmount(payment.getPaymentAmount());
            }
            if (refund.getRefundCurrencyCode() == null) {
                refund.setRefundCurrencyCode(payment.getPaymentCurrencyCode());
            }
        }
        return refund;
    }
}
