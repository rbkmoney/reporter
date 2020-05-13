package com.rbkmoney.reporter.batch.generator;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.BatchKeyGenerator;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.UniqueBatchKey;
import org.springframework.stereotype.Service;

@Service
public class OtherBatchKeyGenerator implements BatchKeyGenerator {

    @Override
    public UniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent) {
        return null;
    }

    @Override
    public boolean isChangeType(InvoiceChange invoiceChange) {
        return false;
    }

    @Override
    public InvoiceBatchType getInvoiceBatchType() {
        return InvoiceBatchType.OTHER;
    }
}
