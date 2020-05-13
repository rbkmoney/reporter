package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.UniqueBatchKey;
import com.rbkmoney.reporter.batch.key.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.dao.query.RefundQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.domain.tables.pojos.RefundState;
import com.rbkmoney.reporter.mapper.InvoiceBatchMapper;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.reporter.util.MapperUtil.getPaymentInvoiceUniqueBatchKey;

@Component
@RequiredArgsConstructor
public class RefundQueryInvoiceBatchMapperImpl implements InvoiceBatchMapper<Void, PaymentPartyData> {

    private final PaymentService paymentService;
    private final RefundQueryTemplator refundQueryTemplator;

    @Override
    public List<Query> map(InvoiceChangeMapper mapper, MapperPayload payload, Map<UniqueBatchKey, Void> producerCache, Map<UniqueBatchKey, PaymentPartyData> consumerCache) {
        List<Query> queries = new ArrayList<>();

        MapperResult mapperResult = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId());

        if (mapperResult.getRefund() != null) {
            Refund refund = mapperResult.getRefund();

            fillRefundFromCache(refund, getPaymentInvoiceUniqueBatchKey(refund), consumerCache);

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

    private void fillRefundFromCache(Refund refund, PaymentInvoiceUniqueBatchKey uniqueBatchKey, Map<UniqueBatchKey, PaymentPartyData> consumerCache) {
        PaymentPartyData paymentPartyData = consumerCache.computeIfAbsent(uniqueBatchKey, key -> paymentService.getPaymentPartyData(uniqueBatchKey));

        refund.setPartyId(paymentPartyData.getPartyId());
        refund.setPartyShopId(paymentPartyData.getPartyShopId());

        if (refund.getAmount() == null) {
            refund.setAmount(paymentPartyData.getPaymentAmount());
            refund.setCurrencyCode(paymentPartyData.getPaymentCurrencyCode());
        }
    }
}
