package com.rbkmoney.reporter.batch.generator;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.BatchKeyGenerator;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.UniqueBatchKey;
import com.rbkmoney.reporter.batch.key.InvoiceUniqueBatchKeyImpl;
import org.springframework.stereotype.Service;

@Service
public class InvoiceBatchKeyGenerator implements BatchKeyGenerator {

    @Override
    public UniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent) {
        return new InvoiceUniqueBatchKeyImpl(machineEvent.getSourceId());
    }

    @Override
    public boolean isChangeType(InvoiceChange invoiceChange) {
        return invoiceChange.isSetInvoiceCreated()
                || invoiceChange.isSetInvoiceStatusChanged();
    }

    @Override
    public InvoiceBatchType getInvoiceBatchType() {
        return InvoiceBatchType.INVOICE;
    }
}
