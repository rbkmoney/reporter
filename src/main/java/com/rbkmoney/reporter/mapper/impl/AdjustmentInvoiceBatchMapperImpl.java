package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.mapper.InvoiceBatchMapper;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.service.AdjustmentService;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdjustmentInvoiceBatchMapperImpl implements InvoiceBatchMapper<Adjustment, Payment> {

    private final PaymentService paymentService;
    private final AdjustmentService adjustmentService;

    @Override
    public Adjustment map(InvoiceChangeMapper mapper, MapperPayload payload, List<Adjustment> adjustments) {
        throw new UnsupportedOperationException("Adjustment need cache");
    }

    @Override
    public Adjustment map(InvoiceChangeMapper mapper, MapperPayload payload, List<Adjustment> adjustments, Map<InvoiceUniqueBatchKey, Payment> consumerCache) {
        String invoiceId = payload.getMachineEvent().getSourceId();
        String paymentId = payload.getInvoiceChange().getInvoicePaymentChange().getId();
        String adjustmentId = payload.getInvoiceChange().getInvoicePaymentChange().getPayload().getInvoicePaymentAdjustmentChange().getId();

        Adjustment adjustment = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId()).getAdjustment();

        if (!adjustments.isEmpty()) {
            Adjustment lastAdjustment = adjustments.get(adjustments.size() - 1);
            BeanUtils.copyProperties(lastAdjustment, adjustment, mapper.getIgnoreProperties());
        } else if (adjustment.getEventType() != InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED) {
            Adjustment lastAdjustment = adjustmentService.get(invoiceId, paymentId, adjustmentId);
            BeanUtils.copyProperties(lastAdjustment, adjustment, mapper.getIgnoreProperties());
        }

        if (adjustment.getEventType() == InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED) {
            Payment payment = consumerCache.computeIfAbsent(
                    new PaymentInvoiceUniqueBatchKey(invoiceId, paymentId),
                    key -> paymentService.get(invoiceId, paymentId)
            );

            adjustment.setPartyId(payment.getPartyId());
            adjustment.setPartyShopId(payment.getPartyShopId());
        }

        return adjustment;
    }
}
