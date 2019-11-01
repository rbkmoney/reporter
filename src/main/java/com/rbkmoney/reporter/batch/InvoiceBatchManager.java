package com.rbkmoney.reporter.batch;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;

public interface InvoiceBatchManager {

    InvoiceBatchService getInvoiceBatchService(InvoiceChange invoiceChange);

    InvoiceChangeMapper getInvoiceChangeMapper(InvoiceChange invoiceChange);

}
