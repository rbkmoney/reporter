package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.dao.query.AdjustmentQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.AdjustmentState;
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
public class AdjustmentQueryInvoiceBatchMapperImpl implements InvoiceBatchMapper<Void, PaymentPartyData> {

    private final PaymentService paymentService;
    private final AdjustmentQueryTemplator adjustmentQueryTemplator;

    @Override
    public List<Query> map(InvoiceChangeMapper mapper, MapperPayload payload, Map<InvoiceUniqueBatchKey, Void> producerCache, Map<InvoiceUniqueBatchKey, PaymentPartyData> consumerCache) {
        List<Query> queries = new ArrayList<>();

        MapperResult mapperResult = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId());

        if (mapperResult.getAdjustment() != null) {
            Adjustment adjustment = mapperResult.getAdjustment();

            fillAdjustmentFromCache(adjustment, getPaymentInvoiceUniqueBatchKey(adjustment), consumerCache);

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

    private void fillAdjustmentFromCache(Adjustment adjustment, PaymentInvoiceUniqueBatchKey uniqueBatchKey, Map<InvoiceUniqueBatchKey, PaymentPartyData> consumerCache) {
        PaymentPartyData paymentPartyData = consumerCache.computeIfAbsent(uniqueBatchKey, key -> paymentService.getPaymentPartyData(uniqueBatchKey));

        adjustment.setPartyId(paymentPartyData.getPartyId());
        adjustment.setPartyShopId(paymentPartyData.getPartyShopId());
    }
}
