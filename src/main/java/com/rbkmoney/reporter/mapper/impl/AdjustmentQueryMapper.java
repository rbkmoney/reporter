package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.dao.query.AdjustmentQueryTemplator;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.AdjustmentState;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdjustmentQueryMapper {

    private final AdjustmentQueryTemplator adjustmentQueryTemplator;
    private final PaymentService paymentService;

    public List<Query> map(InvoiceChangeMapper mapper,
                           MapperPayload payload,
                           List<Adjustment> adjustments,
                           Map<InvoiceUniqueBatchKey, Payment> consumerCache) {
            List<Query> queries = new ArrayList<>();

        String invoiceId = payload.getMachineEvent().getSourceId();
        String paymentId = payload.getInvoiceChange().getInvoicePaymentChange().getId();
        MapperResult mapperResult = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId());

        if (mapperResult.getAdjustment() != null) {
            Adjustment adjustment = mapperResult.getAdjustment();
            if (adjustment.getEventType() == InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED) {
                Payment payment = consumerCache.computeIfAbsent(
                        new PaymentInvoiceUniqueBatchKey(invoiceId, paymentId),
                        key -> getPaymentPartyData(invoiceId, paymentId)
                );
                adjustment.setPartyId(payment.getPartyId());
                adjustment.setPartyShopId(payment.getPartyShopId());
            }
            Query saveAdjustmentQuery = adjustmentQueryTemplator.getSaveAdjustmentQuery(adjustment);
            queries.add(saveAdjustmentQuery);
        }
        if (mapperResult.getAdjustmentState() != null) {
            AdjustmentState adjustmentState = mapperResult.getAdjustmentState();
            Query saveAdjustmentStateQuery = adjustmentQueryTemplator.getSaveAdjustmentStateQuery(adjustmentState);
            queries.add(saveAdjustmentStateQuery);
        }

        return queries;
    }

    private Payment getPaymentPartyData(String invoiceId, String paymentId) {
        PaymentPartyData paymentPartyData = paymentService.getPaymentPartyData(invoiceId, paymentId);
        Payment pmnt = new Payment();
        pmnt.setPartyId(paymentPartyData.getPartyId());
        pmnt.setPartyShopId(paymentPartyData.getPartyShopId());
        return pmnt;
    }

}
