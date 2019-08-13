package com.rbkmoney.reporter.batch.impl;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import org.springframework.stereotype.Service;

@Service
public class InvoiceBatchServiceImpl implements InvoiceBatchService {

    @Override
    public boolean isChangeType(InvoiceChange invoiceChange) {
        return invoiceChange.isSetInvoiceCreated()
                || invoiceChange.isSetInvoiceStatusChanged();
    }

    @Override
    public boolean isCreatedChange(InvoiceChange invoiceChange) {
        return invoiceChange.isSetInvoiceCreated();
    }

    @Override
    public InvoiceBatchType getInvoiceBatchType() {
        return InvoiceBatchType.INVOICE;
    }

    @Override
    public InvoiceUniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent) {
        return new InvoiceUniqueBatchKeyImpl(machineEvent.getSourceId());
    }
}
