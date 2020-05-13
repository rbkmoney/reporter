package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.UniqueBatchKey;
import com.rbkmoney.reporter.batch.key.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.dao.query.InvoiceQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.InvoiceState;
import com.rbkmoney.reporter.mapper.InvoiceBatchMapper;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.reporter.util.MapperUtil.getInvoiceUniqueBatchKeyImpl;
import static com.rbkmoney.reporter.util.MapperUtil.getPartyData;

@Component
@RequiredArgsConstructor
public class InvoiceQueryInvoiceBatchMapperImpl implements InvoiceBatchMapper<PartyData, Void> {

    private final InvoiceService invoiceService;
    private final InvoiceQueryTemplator invoiceQueryTemplator;

    @Override
    public List<Query> map(InvoiceChangeMapper mapper, MapperPayload payload, Map<UniqueBatchKey, PartyData> producerCache, Map<UniqueBatchKey, Void> consumerCache) {
        List<Query> queryList = new ArrayList<>();

        MapperResult mapperResult = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId());

        if (mapperResult.getInvoice() != null) {
            Invoice invoice = mapperResult.getInvoice();

            fillCacheFromInvoice(invoice, producerCache);

            Query saveInvoiceQuery = invoiceQueryTemplator.getSaveInvoiceQuery(invoice);
            queryList.add(saveInvoiceQuery);
        }

        if (mapperResult.getInvoiceState() != null) {
            InvoiceState invoiceState = mapperResult.getInvoiceState();

            Query saveInvoiceStateQuery = invoiceQueryTemplator.getSaveInvoiceStateQuery(invoiceState);
            queryList.add(saveInvoiceStateQuery);
        }

        return queryList;
    }

    private void fillCacheFromInvoice(Invoice invoice, Map<UniqueBatchKey, PartyData> producerCache) {
        InvoiceUniqueBatchKeyImpl uniqueBatchKey = getInvoiceUniqueBatchKeyImpl(invoice);
        PartyData partyData = getPartyData(invoice);

        producerCache.put(uniqueBatchKey, partyData);

        invoiceService.savePartyData(uniqueBatchKey, partyData);
    }
}
