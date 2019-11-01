package com.rbkmoney.reporter.dao.query.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.query.RefundQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.domain.tables.pojos.RefundState;
import com.rbkmoney.reporter.domain.tables.records.RefundRecord;
import com.rbkmoney.reporter.domain.tables.records.RefundStateRecord;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import static com.rbkmoney.reporter.domain.Tables.REFUND;
import static com.rbkmoney.reporter.domain.Tables.REFUND_STATE;

@Component
public class RefundQueryTemplatorImpl extends AbstractGenericDao implements RefundQueryTemplator {

    public RefundQueryTemplatorImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Query getSaveRefundQuery(Refund refund) {
        RefundRecord refundRecord = getDslContext().newRecord(REFUND, refund);
        return getDslContext().insertInto(REFUND)
                .set(refundRecord)
                .onConflict(REFUND.INVOICE_ID, REFUND.PAYMENT_ID, REFUND.REFUND_ID)
                .doNothing();
    }

    @Override
    public Query getSaveRefundStateQuery(RefundState refundState) {
        RefundStateRecord refundStateRecord = getDslContext().newRecord(REFUND_STATE, refundState);
        return getDslContext().insertInto(REFUND_STATE)
                .set(refundStateRecord)
                .onConflict(REFUND_STATE.INVOICE_ID, REFUND_STATE.SEQUENCE_ID, REFUND_STATE.CHANGE_ID)
                .doNothing();
    }
}
