package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.PayoutDao;
import com.rbkmoney.reporter.domain.enums.PayoutStatus;
import com.rbkmoney.reporter.exception.DaoException;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.tables.Payout.PAYOUT;
import static com.rbkmoney.reporter.domain.tables.PayoutState.PAYOUT_STATE;

@Component
public class PayoutDaoImpl extends AbstractGenericDao implements PayoutDao {

    @Autowired
    public PayoutDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Optional<Long> getLastEventId() throws DaoException {
        Query query = getDslContext().select(DSL.max(PAYOUT_STATE.EVENT_ID)).from(PAYOUT_STATE);
        return Optional.ofNullable(fetchOne(query, Long.class));
    }

    @Override
    public Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException {
        String key = "funds_paid_out";
        Field<Long> paidFundsField = DSL.field("paid_funds", Long.class);
        Field<Long> cancelledFundsField = DSL.field("cancelled_funds", Long.class);

        Query query = getDslContext().select(paidFundsField.minus(DSL.coalesce(cancelledFundsField, 0)).as(key))
                .from(selectFundsByStatus(partyId, partyShopId, currencyCode, fromTime, toTime, paidFundsField, PayoutStatus.paid))
                .join(selectFundsByStatus(partyId, partyShopId, currencyCode, fromTime, toTime, cancelledFundsField, PayoutStatus.cancelled))
                .on();

        return Optional.ofNullable(fetchOne(query, (rs, i) -> Map.of(key, rs.getLong(key)))).orElse(Map.of(key, 0L));
    }

    private SelectConditionStep<Record1<BigDecimal>> selectFundsByStatus(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime, Field<Long> cancelledFundsField, PayoutStatus payoutStatus) {
        return getDslContext().select(DSL.sum(PAYOUT.AMOUNT.minus(PAYOUT.FEE)).as(cancelledFundsField))
                .from(PAYOUT)
                .innerJoin(PAYOUT_STATE)
                .on(

                        PAYOUT_STATE.PAYOUT_ID.eq(PAYOUT.PAYOUT_ID)
                                .and(PAYOUT_STATE.STATUS.eq(payoutStatus))
                                .and(fromTime.map(PAYOUT_STATE.EVENT_CREATED_AT::ge).orElse(DSL.trueCondition()))
                                .and(PAYOUT_STATE.EVENT_CREATED_AT.lt(toTime))
                )
                .where(
                        PAYOUT.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(PAYOUT.PARTY_SHOP_ID.eq(partyShopId))
                                .and(PAYOUT.CURRENCY_CODE.eq(currencyCode))
                );
    }
}
