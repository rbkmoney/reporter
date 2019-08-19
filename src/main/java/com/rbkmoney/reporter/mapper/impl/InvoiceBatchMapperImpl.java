package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.mapper.InvoiceBatchMapper;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InvoiceBatchMapperImpl implements InvoiceBatchMapper<Invoice, Void> {

    private final InvoiceService invoiceService;

    @Override
    public Invoice map(InvoiceChangeMapper mapper, MapperPayload payload, List<Invoice> invoices) {
        String invoiceId = payload.getMachineEvent().getSourceId();

        Invoice invoice = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId()).getInvoice();

        if (!invoices.isEmpty()) {
            Invoice lastInvoice = invoices.get(invoices.size() - 1);
            BeanUtils.copyProperties(lastInvoice, invoice, mapper.getIgnoreProperties());
        } else if (invoice.getEventType() != InvoiceEventType.INVOICE_CREATED) {
            Invoice lastInvoice = invoiceService.get(invoiceId);
            BeanUtils.copyProperties(lastInvoice, invoice, mapper.getIgnoreProperties());
        }

        return invoice;
    }

    @Override
    public Invoice map(InvoiceChangeMapper mapper, MapperPayload payload, List<Invoice> invoices, Map<InvoiceUniqueBatchKey, Void> consumerCache) {
        throw new UnsupportedOperationException("Invoice does not need cache");
    }
}
