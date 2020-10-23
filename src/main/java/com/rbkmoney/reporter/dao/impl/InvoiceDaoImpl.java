package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractDao;
import com.rbkmoney.reporter.dao.InvoiceDao;
import com.rbkmoney.reporter.domain.enums.InvoiceStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.InvoiceAdditionalInfo;
import com.rbkmoney.reporter.domain.tables.records.InvoiceAdditionalInfoRecord;
import com.rbkmoney.reporter.domain.tables.records.InvoiceRecord;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.InsertOnDuplicateSetMoreStep;
import org.jooq.InsertResultStep;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.rbkmoney.reporter.domain.tables.Invoice.INVOICE;
import static com.rbkmoney.reporter.domain.tables.InvoiceAdditionalInfo.INVOICE_ADDITIONAL_INFO;

@Component
public class InvoiceDaoImpl extends AbstractDao implements InvoiceDao {

    @Autowired
    public InvoiceDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long saveInvoice(Invoice invoice) {
        InsertResultStep<InvoiceRecord> insertResultStep = getDslContext()
                .insertInto(INVOICE)
                .set(getDslContext().newRecord(INVOICE, invoice))
                .onConflict(INVOICE.INVOICE_ID)
                .doUpdate()
                .set(getDslContext().newRecord(INVOICE, invoice))
                .returning(INVOICE.ID);
        return insertResultStep.fetchOne().getId();
    }

    @Override
    public InvoiceRecord getInvoice(String invoiceId) {
        return getDslContext()
                .selectFrom(INVOICE)
                .where(INVOICE.INVOICE_ID.eq(invoiceId))
                .fetchOne();
    }

    @Override
    public List<Invoice> getInvoices(String partyId,
                               String shopId,
                               Optional<LocalDateTime> fromTime,
                               LocalDateTime toTime) {
        SelectConditionStep<InvoiceRecord> conditionStep = getDslContext()
                .selectFrom(INVOICE)
                .where(fromTime.map(INVOICE.CREATED_AT::ge).orElse(DSL.trueCondition()))
                .and(INVOICE.CREATED_AT.lt(toTime))
                .and(INVOICE.PARTY_ID.eq(partyId))
                .and(INVOICE.SHOP_ID.eq(shopId));
        return conditionStep.fetch().into(Invoice.class);
    }

    @Override
    public List<Invoice> getInvoicesByState(LocalDateTime dateFrom, LocalDateTime dateTo, List<InvoiceStatus> statuses) {
        return getDslContext()
                .selectFrom(INVOICE)
                .where(INVOICE.CREATED_AT.greaterThan(dateFrom)
                        .and(INVOICE.CREATED_AT.lessThan(dateTo))
                        .and(INVOICE.STATUS.in(statuses)))
                .fetch().into(Invoice.class);
    }

    @Override
    public void saveAdditionalInvoiceInfo(InvoiceAdditionalInfo invoiceAdditionalInfo) {
        InsertOnDuplicateSetMoreStep<InvoiceAdditionalInfoRecord> step = getDslContext()
                .insertInto(INVOICE_ADDITIONAL_INFO)
                .set(getDslContext().newRecord(INVOICE_ADDITIONAL_INFO, invoiceAdditionalInfo))
                .onDuplicateKeyUpdate()
                .set(getDslContext().newRecord(INVOICE_ADDITIONAL_INFO, invoiceAdditionalInfo));
        step.execute();
    }

    @Override
    public InvoiceAdditionalInfo getInvoiceAdditionalInfo(Long extInvoiceId) {
        return getDslContext()
                .selectFrom(INVOICE_ADDITIONAL_INFO)
                .where(INVOICE_ADDITIONAL_INFO.EXT_INVOICE_ID.eq(extInvoiceId))
                .fetchOne()
                .into(InvoiceAdditionalInfo.class);
    }
}
