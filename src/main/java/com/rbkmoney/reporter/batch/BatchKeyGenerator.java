package com.rbkmoney.reporter.batch;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;

public interface BatchKeyGenerator {

    UniqueBatchKey getInvoiceUniqueBatchKey(InvoiceChange invoiceChange, MachineEvent machineEvent);

    boolean isChangeType(InvoiceChange invoiceChange);

    InvoiceBatchType getInvoiceBatchType();

}
