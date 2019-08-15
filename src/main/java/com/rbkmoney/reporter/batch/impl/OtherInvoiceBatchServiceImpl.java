package com.rbkmoney.reporter.batch.impl;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.batch.InvoiceBatchService;
import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.batch.InvoiceUniqueBatchKey;
import org.springframework.stereotype.Service;

@Service
public class OtherInvoiceBatchServiceImpl implements InvoiceBatchService {

    @Override
    public boolean isChangeType(InvoiceChange invoiceChange) {
        return false;
    }

    @Override
    public InvoiceBatchType getInvoiceBatchType() {
        return InvoiceBatchType.OTHER;
    }

    @Override
    public InvoiceUniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent) {
        return null;
    }
}
