package com.rbkmoney.reporter.dao.query.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.query.PayoutQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.domain.tables.pojos.PayoutState;
import com.rbkmoney.reporter.domain.tables.records.PayoutRecord;
import com.rbkmoney.reporter.domain.tables.records.PayoutStateRecord;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import static com.rbkmoney.reporter.domain.Tables.PAYOUT;
import static com.rbkmoney.reporter.domain.Tables.PAYOUT_STATE;

@Component
public class PayoutQueryTemplatorImpl extends AbstractGenericDao implements PayoutQueryTemplator {

    public PayoutQueryTemplatorImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Query getSavePayoutQuery(Payout payout) {
        PayoutRecord payoutRecord = getDslContext().newRecord(PAYOUT, payout);
        return getDslContext().insertInto(PAYOUT)
                .set(payoutRecord)
                .onConflict(PAYOUT.PAYOUT_ID)
                .doNothing();
    }

    @Override
    public Query getSavePayoutStateQuery(PayoutState payoutState) {
        PayoutStateRecord adjustmentStateRecord = getDslContext().newRecord(PAYOUT_STATE, payoutState);
        return getDslContext().insertInto(PAYOUT_STATE)
                .set(adjustmentStateRecord)
                .onConflict(PAYOUT_STATE.PAYOUT_ID, PAYOUT_STATE.EVENT_ID, PAYOUT_STATE.EVENT_CREATED_AT)
                .doNothing();
    }
}
