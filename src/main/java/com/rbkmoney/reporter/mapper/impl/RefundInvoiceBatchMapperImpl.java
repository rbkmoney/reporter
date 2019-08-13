package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
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
        Refund refund = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId()).getRefund();

        if (!refunds.isEmpty()) {
            Refund lastRefund = refunds.get(refunds.size() - 1);
            BeanUtils.copyProperties(lastRefund, refund, mapper.getIgnoreProperties());
        } else if (refund.getEventType() != InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED) {
            Refund lastRefund = refundService.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId());
            BeanUtils.copyProperties(lastRefund, refund, mapper.getIgnoreProperties());
        }

        if (refund.getEventType() == InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED) {
            Payment payment = consumerCache.computeIfAbsent(getInvoiceCacheKey(refund), key -> paymentService.get(refund.getInvoiceId(), refund.getPaymentId()));

            refund.setPartyId(payment.getPartyId());
            refund.setPartyShopId(payment.getPartyShopId());

            InvoicePaymentRefund invoicePaymentRefund = getInvoicePaymentRefund(payload);
            if (!invoicePaymentRefund.isSetCash()) {
                refund.setRefundAmount(payment.getPaymentAmount());
                refund.setRefundCurrencyCode(payment.getPaymentCurrencyCode());
            }
        }
        return refund;
    }

    private InvoicePaymentRefund getInvoicePaymentRefund(MapperPayload payload) {
        return payload.getInvoiceChange().getInvoicePaymentChange()
                .getPayload().getInvoicePaymentRefundChange()
                .getPayload().getInvoicePaymentRefundCreated().getRefund();
    }

    private InvoiceUniqueBatchKey getInvoiceCacheKey(Refund refund) {
        return new PaymentInvoiceUniqueBatchKey(refund.getInvoiceId(), refund.getPaymentId());
    }
}
