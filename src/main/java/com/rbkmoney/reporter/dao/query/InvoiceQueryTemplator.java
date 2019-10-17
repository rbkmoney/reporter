package com.rbkmoney.reporter.dao.query;

import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.InvoiceState;
import org.jooq.Query;

public interface InvoiceQueryTemplator {

    Query getSaveInvoiceQuery(Invoice invoice);

    Query getSaveInvoiceStateQuery(InvoiceState invoiceState);

}
