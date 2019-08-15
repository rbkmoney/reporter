package com.rbkmoney.reporter.batch;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.reporter.dao.BatchDao;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;

public interface InvoiceBatchManager {

    InvoiceBatchService getInvoiceBatchService(InvoiceChange invoiceChange);

    BatchDao getBatchDao(InvoiceBatchType invoiceBatchType);

    InvoiceChangeMapper getInvoiceChangeMapper(InvoiceChange invoiceChange);

}
