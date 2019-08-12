package com.rbkmoney.reporter.batch.impl;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.dao.BatchDao;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoicingBatchManagerImpl implements InvoiceBatchManager {

    private final List<InvoiceBatchService> invoiceBatchServices;
    private final List<BatchDao> batchDaos;
    private final List<InvoiceChangeMapper> invoiceChangeMappers;

    @Override
    public InvoiceBatchService getInvoiceBatchService(InvoiceChange invoiceChange) {
        return invoiceBatchServices.stream()
                .filter(invoiceChangeType -> invoiceChangeType.isChangeType(invoiceChange))
                .findFirst()
                .orElseThrow();
    }

    @Override
    public BatchDao getBatchDao(InvoiceBatchType invoiceChangeTypeEnum) {
        return batchDaos.stream()
                .filter(dao -> dao.isInvoiceChangeType(invoiceChangeTypeEnum))
                .findFirst()
                .orElseThrow();
    }

    @Override
    public InvoiceChangeMapper getInvoiceChangeMapper(InvoiceChange invoiceChange) {
        return invoiceChangeMappers.stream()
                .filter(invoiceInvoiceChangeMapper -> invoiceInvoiceChangeMapper.canMap(invoiceChange))
                .findFirst()
                .orElseThrow();
    }
}
