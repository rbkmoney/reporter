package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.query.InvoiceQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.InvoiceState;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.mapper.MapperResult;
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
public class InvoiceQueryMapper {

    private final InvoiceQueryTemplator invoiceQueryTemplator;

    public List<Query> map(InvoiceChangeMapper mapper, MapperPayload payload, List<Invoice> invoices) throws Exception {
        String invoiceId = payload.getMachineEvent().getSourceId();
        log.info(" {}", invoiceId);

        List<Query> queryList = new ArrayList<>();
        MapperResult mapperResult = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId());
        if (mapperResult.getInvoice() != null) {
            Invoice invoice = mapperResult.getInvoice();
            invoices.add(invoice);
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

    public Invoice map(InvoiceChangeMapper mapper,
                       MapperPayload payload,
                       List<Invoice> invoices,
                       Map<InvoiceUniqueBatchKey, Void> consumerCache) {
        throw new UnsupportedOperationException("Invoice does not need cache");
    }

}
