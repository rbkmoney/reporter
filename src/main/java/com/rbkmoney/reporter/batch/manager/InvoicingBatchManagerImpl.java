package com.rbkmoney.reporter.batch.manager;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.reporter.batch.InvoiceBatchManager;
import com.rbkmoney.reporter.batch.BatchKeyGenerator;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class InvoicingBatchManagerImpl implements InvoiceBatchManager {

    private final List<BatchKeyGenerator> batchKeyGenerators;
    private final List<InvoiceChangeMapper> invoiceChangeMappers;

    private final BatchKeyGenerator otherBatchKeyGenerator;

    @Override
    public BatchKeyGenerator getInvoiceBatchService(InvoiceChange invoiceChange) {
        return batchKeyGenerators.stream()
                .filter(invoiceChangeType -> invoiceChangeType.isChangeType(invoiceChange))
                .findFirst()
                .orElse(otherBatchKeyGenerator);
    }

    @Override
    public InvoiceChangeMapper getInvoiceChangeMapper(InvoiceChange invoiceChange) {
        return invoiceChangeMappers.stream()
                .filter(invoiceInvoiceChangeMapper -> invoiceInvoiceChangeMapper.canMap(invoiceChange))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("No value present, invoiceChange='%s'", invoiceChange))
                );
    }
}
