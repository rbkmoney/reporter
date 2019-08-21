package com.rbkmoney.reporter.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.PayoutDao;
import com.rbkmoney.reporter.dao.mapper.RecordRowMapper;
import com.rbkmoney.reporter.dao.routines.RoutinesWrapper;
import com.rbkmoney.reporter.domain.enums.PayoutEventCategory;
import com.rbkmoney.reporter.domain.enums.PayoutStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.domain.tables.records.PayoutRecord;
import com.rbkmoney.reporter.exception.DaoException;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.AggregateFunction;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.tables.Payout.PAYOUT;

@Component
public class PayoutDaoImpl extends AbstractGenericDao implements PayoutDao {

    private final RowMapper<Payout> rowMapper;

    @Autowired
    public PayoutDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
        rowMapper = new RecordRowMapper<>(PAYOUT, Payout.class);
    }

    @Override
    public Optional<Long> getLastEventId() throws DaoException {
        Query query = getDslContext().select(DSL.max(PAYOUT.EVENT_ID)).from(PAYOUT);
        return Optional.ofNullable(fetchOne(query, Long.class));
    }

    @Override
    public Long save(Payout payout) throws DaoException {
        PayoutRecord payoutRecord = getDslContext().newRecord(PAYOUT, payout);
        Query query = getDslContext().insertInto(PAYOUT)
                .set(payoutRecord)
                .onConflict(PAYOUT.EVENT_ID, PAYOUT.EVENT_TYPE, PAYOUT.PAYOUT_STATUS)
                .doNothing()
                .returning(PAYOUT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue).orElse(null);
    }

    @Override
    public Payout get(String payoutId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYOUT)
                .where(
                        PAYOUT.PAYOUT_ID.eq(payoutId)
                                .and(PAYOUT.EVENT_CATEGORY.eq(PayoutEventCategory.PAYOUT))
                )
                .orderBy(PAYOUT.ID.desc())
                .limit(1);

        return fetchOne(query, rowMapper);
    }

    @Override
    public Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException {
        String key = "funds_paid_out";
        Field<Long> paidFundsField = DSL.field("paid_funds", Long.class);
        Field<Long> cancelledFundsField = DSL.field("cancelled_funds", Long.class);
        AggregateFunction<Long> paymentAmountAggregateFunction = RoutinesWrapper.getPayoutAmount();
        AggregateFunction<Long> paymentFeeAggregateFunction = RoutinesWrapper.getPayoutFee();

        Query query = getDslContext().select(paidFundsField.minus(cancelledFundsField).as(key))
                .from(
                        getDslContext().select(paymentAmountAggregateFunction.minus(paymentFeeAggregateFunction).as(paidFundsField))
                                .from(PAYOUT)
                                .where(getPayoutAccountingDataCondition(partyId, partyShopId, currencyCode, fromTime, toTime, PayoutStatus.paid))
                )
                .innerJoin(
                        getDslContext().select(paymentAmountAggregateFunction.minus(paymentFeeAggregateFunction).as(cancelledFundsField))
                                .from(PAYOUT)
                                .where(
                                        getPayoutAccountingDataCondition(partyId, partyShopId, currencyCode, fromTime, toTime, PayoutStatus.cancelled)
                                                .and(
                                                        PAYOUT.PAYOUT_ID.in(
                                                                getDslContext().select(PAYOUT.PAYOUT_ID)
                                                                        .from(PAYOUT)
                                                                        .where(getPayoutAccountingDataCondition(partyId, partyShopId, currencyCode, fromTime, toTime, PayoutStatus.paid))
                                                        )
                                                )
                                )
                )
                .on(DSL.trueCondition());


        return Optional.ofNullable(
                fetchOne(
                        query,
                        (rs, i) -> ImmutableMap.<String, Long>builder()
                                .put(key, rs.getLong(key))
                                .build()
                )
        ).orElse(
                ImmutableMap.<String, Long>builder()
                        .put(key, 0L)
                        .build()
        );
    }

    private Condition getPayoutAccountingDataCondition(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime, PayoutStatus payoutStatus) {
        return PAYOUT.PARTY_ID.eq(UUID.fromString(partyId))
                .and(PAYOUT.PARTY_SHOP_ID.eq(partyShopId))
                .and(PAYOUT.PAYOUT_CURRENCY_CODE.eq(currencyCode))
                .and(PAYOUT.PAYOUT_STATUS.eq(payoutStatus))
                .and(fromTime.map(PAYOUT.EVENT_CREATED_AT::ge).orElse(DSL.trueCondition()))
                .and(PAYOUT.EVENT_CREATED_AT.lt(toTime));
    }
}
