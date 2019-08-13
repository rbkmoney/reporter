package com.rbkmoney.reporter.batch;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;

public interface InvoiceBatchService {

    boolean isChangeType(InvoiceChange invoiceChange);

    boolean isCreatedChange(InvoiceChange invoiceChange);

    InvoiceBatchType getInvoiceBatchType();

    InvoiceUniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent);

}
