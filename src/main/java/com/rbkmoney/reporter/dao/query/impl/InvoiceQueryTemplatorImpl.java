package com.rbkmoney.reporter.dao.query.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.query.InvoiceQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.InvoiceState;
import com.rbkmoney.reporter.domain.tables.records.InvoiceRecord;
import com.rbkmoney.reporter.domain.tables.records.InvoiceStateRecord;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static com.rbkmoney.reporter.domain.tables.Invoice.INVOICE;
import static com.rbkmoney.reporter.domain.tables.InvoiceState.INVOICE_STATE;

@Component
public class InvoiceQueryTemplatorImpl extends AbstractGenericDao implements InvoiceQueryTemplator {

    public InvoiceQueryTemplatorImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Query getSaveInvoiceQuery(Invoice invoice) {
        InvoiceRecord invoiceRecord = getDslContext().newRecord(INVOICE, invoice);
        return getDslContext().insertInto(INVOICE)
                .set(invoiceRecord)
                .onConflict(INVOICE.INVOICE_ID, INVOICE.SEQUENCE_ID, INVOICE.CHANGE_ID)
                .doNothing();
    }

    @Override
    public Query getSaveInvoiceStateQuery(InvoiceState invoiceState) {
        InvoiceStateRecord invoiceStateRecord = getDslContext().newRecord(INVOICE_STATE, invoiceState);
        return getDslContext().insertInto(INVOICE_STATE)
                .set(invoiceStateRecord)
                .onConflict(INVOICE.INVOICE_ID, INVOICE.SEQUENCE_ID, INVOICE.CHANGE_ID)
                .doNothing();
    }

}
