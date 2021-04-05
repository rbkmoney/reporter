package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractDao;
import com.rbkmoney.reporter.dao.PayoutDao;
import com.rbkmoney.reporter.domain.enums.PayoutStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.domain.tables.pojos.PayoutAccount;
import com.rbkmoney.reporter.domain.tables.pojos.PayoutInternationalAccount;
import com.rbkmoney.reporter.domain.tables.pojos.PayoutState;
import com.rbkmoney.reporter.domain.tables.records.*;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.rbkmoney.reporter.domain.Tables.PAYOUT_AGGS_BY_HOUR;
import static com.rbkmoney.reporter.domain.tables.Payout.PAYOUT;
import static com.rbkmoney.reporter.domain.tables.PayoutAccount.PAYOUT_ACCOUNT;
import static com.rbkmoney.reporter.domain.tables.PayoutInternationalAccount.PAYOUT_INTERNATIONAL_ACCOUNT;
import static com.rbkmoney.reporter.domain.tables.PayoutState.PAYOUT_STATE;
import static com.rbkmoney.reporter.util.AccountingDaoUtils.getFundsAmountResult;

@Component
public class PayoutDaoImpl extends AbstractDao implements PayoutDao {

    private static final String AMOUNT_KEY = "funds_paid_out";

    @Autowired
    public PayoutDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long savePayout(Payout payout) {
        return getDslContext()
                .insertInto(PAYOUT)
                .set(getDslContext().newRecord(PAYOUT, payout))
                .onConflict(PAYOUT.PAYOUT_ID)
                .doUpdate()
                .set(getDslContext().newRecord(PAYOUT, payout))
                .returning(PAYOUT.ID)
                .fetchOne()
                .getId();
    }

    @Override
    public Payout getPayout(String payoutId) {
        PayoutRecord payoutRecord = getDslContext()
                .selectFrom(PAYOUT)
                .where(PAYOUT.PAYOUT_ID.eq(payoutId))
                .fetchOne();
        return payoutRecord == null ? null : payoutRecord.into(Payout.class);
    }

    @Override
    public void savePayoutAccountInfo(PayoutAccount payoutAccount) {
        getDslContext()
                .insertInto(PAYOUT_ACCOUNT)
                .set(getDslContext().newRecord(PAYOUT_ACCOUNT, payoutAccount))
                .onConflict(PAYOUT_ACCOUNT.EXT_PAYOUT_ID)
                .doUpdate()
                .set(getDslContext().newRecord(PAYOUT_ACCOUNT, payoutAccount))
                .execute();
    }

    @Override
    public PayoutAccount getPayoutAccount(Long extPayoutId) {
        PayoutAccountRecord payoutAccountRecord = getDslContext()
                .selectFrom(PAYOUT_ACCOUNT)
                .where(PAYOUT_ACCOUNT.EXT_PAYOUT_ID.eq(extPayoutId))
                .fetchOne();
        return payoutAccountRecord == null ? null : payoutAccountRecord.into(PayoutAccount.class);
    }

    @Override
    public void savePayoutInternationalAccountInfo(PayoutInternationalAccount internationalAccount) {
        getDslContext()
                .insertInto(PAYOUT_INTERNATIONAL_ACCOUNT)
                .set(getDslContext().newRecord(PAYOUT_INTERNATIONAL_ACCOUNT, internationalAccount))
                .onConflict(PAYOUT_INTERNATIONAL_ACCOUNT.EXT_PAYOUT_ID)
                .doUpdate()
                .set(getDslContext().newRecord(PAYOUT_INTERNATIONAL_ACCOUNT, internationalAccount))
                .execute();
    }

    @Override
    public PayoutInternationalAccount getPayoutInternationalAccount(Long extPayoutId) {
        PayoutInternationalAccountRecord accountRecord = getDslContext()
                .selectFrom(PAYOUT_INTERNATIONAL_ACCOUNT)
                .where(PAYOUT_INTERNATIONAL_ACCOUNT.EXT_PAYOUT_ID.eq(extPayoutId))
                .fetchOne();
        return accountRecord == null ? null : accountRecord.into(PayoutInternationalAccount.class);
    }

    @Override
    public Long savePayoutState(PayoutState payoutState) {
        return getDslContext()
                .insertInto(PAYOUT_STATE)
                .set(getDslContext().newRecord(PAYOUT_STATE, payoutState))
                .onConflict(PAYOUT_STATE.EXT_PAYOUT_ID, PAYOUT_STATE.EVENT_CREATED_AT, PAYOUT_STATE.STATUS)
                .doUpdate()
                .set(getDslContext().newRecord(PAYOUT_STATE, payoutState))
                .returning(PAYOUT_STATE.ID)
                .fetchOne()
                .getId();
    }

    @Override
    public PayoutState getPayoutState(Long extPayoutId) {
        PayoutStateRecord payoutStateRecord = getDslContext()
                .selectFrom(PAYOUT_STATE)
                .where(PAYOUT_STATE.EXT_PAYOUT_ID.eq(extPayoutId))
                .fetchOne();
        return payoutStateRecord == null ? null : payoutStateRecord.into(PayoutState.class);
    }

    @Override
    public Optional<Long> getLastEventId() {
        return Optional.ofNullable(
                getDslContext()
                        .select(DSL.max(PAYOUT_STATE.EVENT_ID))
                        .from(PAYOUT_STATE).fetchOne()
                        .value1()
        );

    }

    @Override
    public Long getFundsPayOutAmount(String partyId,
                                     String shopId,
                                     String currencyCode,
                                     Optional<LocalDateTime> fromTime,
                                     LocalDateTime toTime) {
        LocalDateTime reportFromTime = fromTime.orElse(LocalDateTime.now());
        LocalDateTime fromTimeTruncHour = reportFromTime.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime toTimeTruncHour = toTime.truncatedTo(ChronoUnit.HOURS);

        var youngPayoutShopAccountingQuery = getPayoutShopAccountingReportData(
                partyId, shopId, currencyCode, reportFromTime, fromTimeTruncHour.plusHours(1L)
        );
        var payoutAggByHourShopAccountingQuery = getPayoutAggByHourShopAccountingQuery(
                partyId, shopId, currencyCode, fromTimeTruncHour, toTimeTruncHour
        );
        var oldPayoutShopAccountingQuery = getPayoutShopAccountingReportData(
                partyId, shopId, currencyCode, toTimeTruncHour, toTime
        );
        var fundsPayOutAmountResult = getDslContext()
                .select(DSL.sum(DSL.field(AMOUNT_KEY, Long.class)).as(AMOUNT_KEY))
                .from(
                        youngPayoutShopAccountingQuery
                                .unionAll(payoutAggByHourShopAccountingQuery)
                                .unionAll(oldPayoutShopAccountingQuery)
                )
                .fetchOne();
        return getFundsAmountResult(fundsPayOutAmountResult);
    }

    private SelectConditionStep<Record1<BigDecimal>> getPayoutAggByHourShopAccountingQuery(String partyId,
                                                                                           String partyShopId,
                                                                                           String currencyCode,
                                                                                           LocalDateTime fromTime,
                                                                                           LocalDateTime toTime) {
        return getDslContext()
                .select(DSL.sum(PAYOUT_AGGS_BY_HOUR.AMOUNT).as(AMOUNT_KEY))
                .from(PAYOUT_AGGS_BY_HOUR)
                .where(PAYOUT_AGGS_BY_HOUR.CREATED_AT.greaterThan(fromTime))
                .and(PAYOUT_AGGS_BY_HOUR.CREATED_AT.lessThan(toTime))
                .and(PAYOUT_AGGS_BY_HOUR.CURRENCY_CODE.eq(currencyCode))
                .and(PAYOUT_AGGS_BY_HOUR.PARTY_ID.eq(partyId))
                .and(PAYOUT_AGGS_BY_HOUR.SHOP_ID.eq(partyShopId));
    }

    private SelectConditionStep<Record1<BigDecimal>> getPayoutShopAccountingReportData(String partyId,
                                                                                       String partyShopId,
                                                                                       String currencyCode,
                                                                                       LocalDateTime fromTime,
                                                                                       LocalDateTime toTime) {
        return getDslContext()
                .select(DSL.sum(PAYOUT.AMOUNT).as(AMOUNT_KEY))
                .from(PAYOUT_STATE)
                .join(PAYOUT).on(PAYOUT.PAYOUT_ID.eq(PAYOUT_STATE.PAYOUT_ID))
                .where(PAYOUT_STATE.EVENT_CREATED_AT.greaterOrEqual(fromTime))
                .and(PAYOUT_STATE.EVENT_CREATED_AT.lessThan(toTime))
                .and(PAYOUT_STATE.STATUS.eq(PayoutStatus.paid))
                .and(PAYOUT.CURRENCY_CODE.eq(currencyCode))
                .and(PAYOUT.PARTY_ID.eq(partyId))
                .and(PAYOUT.SHOP_ID.eq(partyShopId));
    }

}
