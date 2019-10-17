package com.rbkmoney.reporter.dao.query.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.query.AdjustmentQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.AdjustmentState;
import com.rbkmoney.reporter.domain.tables.records.AdjustmentRecord;
import com.rbkmoney.reporter.domain.tables.records.AdjustmentStateRecord;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static com.rbkmoney.reporter.domain.Tables.ADJUSTMENT_STATE;
import static com.rbkmoney.reporter.domain.tables.Adjustment.ADJUSTMENT;
import static com.rbkmoney.reporter.domain.tables.Invoice.INVOICE;

@Component
public class AdjustmentQueryTemplatorImpl extends AbstractGenericDao implements AdjustmentQueryTemplator {

    public AdjustmentQueryTemplatorImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Query getSaveAdjustmentQuery(Adjustment adjustment) {
        AdjustmentRecord adjustmentRecord = getDslContext().newRecord(ADJUSTMENT, adjustment);
        return getDslContext().insertInto(ADJUSTMENT)
                .set(adjustmentRecord)
                .onConflict(INVOICE.INVOICE_ID, INVOICE.SEQUENCE_ID, INVOICE.CHANGE_ID)
                .doNothing();
    }

    @Override
    public Query getSaveAdjustmentStateQuery(AdjustmentState adjustmentState) {
        AdjustmentStateRecord adjustmentStateRecord = getDslContext().newRecord(ADJUSTMENT_STATE, adjustmentState);
        return getDslContext().insertInto(ADJUSTMENT_STATE)
                .set(adjustmentStateRecord)
                .onConflict(INVOICE.INVOICE_ID, INVOICE.SEQUENCE_ID, INVOICE.CHANGE_ID)
                .doNothing();
    }

}
