package com.rbkmoney.reporter.batch.impl;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class InvoicingBatchManagerImpl implements InvoiceBatchManager {

    private final List<InvoiceBatchService> invoiceBatchServices;
    private final List<InvoiceChangeMapper> invoiceChangeMappers;

    private final InvoiceBatchService otherInvoiceBatchServiceImpl;

    @Override
    public InvoiceBatchService getInvoiceBatchService(InvoiceChange invoiceChange) {
        return invoiceBatchServices.stream()
                .filter(invoiceChangeType -> invoiceChangeType.isChangeType(invoiceChange))
                .findFirst()
                .orElse(otherInvoiceBatchServiceImpl);
    }

    @Override
    public InvoiceChangeMapper getInvoiceChangeMapper(InvoiceChange invoiceChange) {
        return invoiceChangeMappers.stream()
                .filter(invoiceInvoiceChangeMapper -> invoiceInvoiceChangeMapper.canMap(invoiceChange))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("No value present, invoiceChange='%s'", invoiceChange)));
    }
}
