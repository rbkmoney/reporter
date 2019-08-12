package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.batch.InvoiceBatchType;
import com.rbkmoney.reporter.dao.AbstractGenericDao;
import com.rbkmoney.reporter.dao.BatchDao;
import com.rbkmoney.reporter.dao.InvoiceDao;
import com.rbkmoney.reporter.dao.mapper.RecordRowMapper;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.records.InvoiceRecord;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import static com.rbkmoney.reporter.domain.tables.Invoice.INVOICE;

@Component
public class InvoiceDaoImpl extends AbstractGenericDao implements InvoiceDao, BatchDao {

    private final RowMapper<Invoice> rowMapper;

    @Autowired
    public InvoiceDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
        rowMapper = new RecordRowMapper<>(INVOICE, Invoice.class);
    }

    @Override
    public Long save(Invoice invoice) throws DaoException {
        InvoiceRecord invoiceRecord = getDslContext().newRecord(INVOICE, invoice);
        Query query = getDslContext().insertInto(INVOICE)
                .set(invoiceRecord)
                .onConflict(INVOICE.INVOICE_ID, INVOICE.SEQUENCE_ID, INVOICE.CHANGE_ID)
                .doUpdate()
                .set(invoiceRecord)
                .returning(INVOICE.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Invoice get(String invoiceId) throws DaoException {
        Query query = getDslContext().selectFrom(INVOICE)
                .where(INVOICE.INVOICE_ID.eq(invoiceId))
                .orderBy(INVOICE.ID.desc())
                .limit(1);

        return fetchOne(query, rowMapper);
    }

    @Override
    public boolean isInvoiceChangeType(InvoiceBatchType invoiceChangeTypeEnum) {
        return invoiceChangeTypeEnum.equals(InvoiceBatchType.INVOICE);
    }

    @Override
    public Query getSaveEventQuery(MapperResult entity) {
        InvoiceRecord invoiceRecord = getDslContext().newRecord(INVOICE, entity.getInvoice());
        return getDslContext().insertInto(INVOICE)
                .set(invoiceRecord)
                .onConflict(INVOICE.INVOICE_ID, INVOICE.SEQUENCE_ID, INVOICE.CHANGE_ID)
                .doUpdate()
                .set(invoiceRecord);
    }
}
