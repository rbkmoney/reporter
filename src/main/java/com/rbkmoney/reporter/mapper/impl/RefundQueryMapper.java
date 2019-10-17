package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.dao.query.RefundQueryTemplator;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.PaymentCost;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.domain.tables.pojos.RefundState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.service.PaymentService;
import com.rbkmoney.reporter.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RefundQueryMapper {

    private final PaymentService paymentService;
    private final RefundService refundService;
    private final RefundQueryTemplator refundQueryTemplator;

    public List<Query> map(InvoiceChangeMapper mapper,
                       MapperPayload payload,
                       List<Refund> refunds,
                       Map<InvoiceUniqueBatchKey, Payment> paymentCashe,
                           Map<InvoiceUniqueBatchKey, PaymentCost> paymentCostCashe) {
        List<Query> queries = new ArrayList<>();

        String invoiceId = payload.getMachineEvent().getSourceId();
        String paymentId = payload.getInvoiceChange().getInvoicePaymentChange().getId();
        MapperResult mapperResult = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId());

        if (mapperResult.getRefund() != null) {
            Refund refund = mapperResult.getRefund();
            if (refund.getEventType() == InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED) {
                Payment payment = paymentCashe.computeIfAbsent(
                        new PaymentInvoiceUniqueBatchKey(invoiceId, paymentId),
                        key -> getPaymentPartyData(invoiceId, paymentId)
                );
                refund.setPartyId(payment.getPartyId());
                refund.setPartyShopId(payment.getPartyShopId());

                PaymentCost paymentCost = paymentCostCashe.computeIfAbsent(
                        new PaymentInvoiceUniqueBatchKey(invoiceId, paymentId),
                        key -> paymentService.getPaymentCost(invoiceId, paymentId)
                );
                if (refund.getRefundAmount() == null) {
                    refund.setRefundAmount(paymentCost.getAmount());
                }
                if (refund.getRefundCurrencyCode() == null) {
                    refund.setRefundCurrencyCode(paymentCost.getCurrency());
                }
            }

            Query saveRefundQuery = refundQueryTemplator.getSaveRefundQuery(refund);
            queries.add(saveRefundQuery);
        }
        if (mapperResult.getRefundState() != null) {
            RefundState refundState = mapperResult.getRefundState();
            Query saveRefundStateQuery = refundQueryTemplator.getSaveRefundStateQuery(refundState);

            queries.add(saveRefundStateQuery);
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
