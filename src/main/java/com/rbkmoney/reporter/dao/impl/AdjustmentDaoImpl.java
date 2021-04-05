package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractDao;
import com.rbkmoney.reporter.dao.AdjustmentDao;
import com.rbkmoney.reporter.domain.enums.AdjustmentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.records.AdjustmentRecord;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Cursor;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.rbkmoney.reporter.domain.Tables.ADJUSTMENT_AGGS_BY_HOUR;
import static com.rbkmoney.reporter.domain.tables.Adjustment.ADJUSTMENT;
import static com.rbkmoney.reporter.util.AccountingDaoUtils.getFundsAmountResult;

@Component
public class AdjustmentDaoImpl extends AbstractDao implements AdjustmentDao {

    private static final String AMOUNT_KEY = "funds_adjusted";

    @Autowired
    public AdjustmentDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long saveAdjustment(Adjustment adjustment) {
        return getDslContext()
                .insertInto(ADJUSTMENT)
                .set(getDslContext().newRecord(ADJUSTMENT, adjustment))
                .onConflict(ADJUSTMENT.INVOICE_ID, ADJUSTMENT.PAYMENT_ID, ADJUSTMENT.ADJUSTMENT_ID)
                .doUpdate()
                .set(getDslContext().newRecord(ADJUSTMENT, adjustment))
                .returning(ADJUSTMENT.ID)
                .fetchOne()
                .getId();
    }

    @Override
    public List<Adjustment> getAdjustmentsByState(LocalDateTime dateFrom,
                                                  LocalDateTime dateTo,
                                                  List<AdjustmentStatus> statuses) {
        Result<AdjustmentRecord> records = getDslContext()
                .selectFrom(ADJUSTMENT)
                .where(ADJUSTMENT.STATUS_CREATED_AT.greaterThan(dateFrom)
                        .and(ADJUSTMENT.STATUS_CREATED_AT.lessThan(dateTo))
                        .and(ADJUSTMENT.STATUS.in(statuses)))
                .fetch();
        return records == null || records.isEmpty() ? new ArrayList<>() : records.into(Adjustment.class);
    }

    @Override
    public Cursor<AdjustmentRecord> getAdjustmentCursor(String partyId,
                                                        String shopId,
                                                        LocalDateTime fromTime,
                                                        LocalDateTime toTime) {
        return getDslContext()
                .selectFrom(ADJUSTMENT)
                .where(ADJUSTMENT.STATUS_CREATED_AT.greaterThan(fromTime))
                .and(ADJUSTMENT.STATUS_CREATED_AT.lessThan(toTime))
                .and(ADJUSTMENT.PARTY_ID.eq(partyId))
                .and(ADJUSTMENT.SHOP_ID.eq(shopId))
                .and(ADJUSTMENT.STATUS.eq(AdjustmentStatus.captured))
                .fetchLazy();
    }

    @Override
    public Long getFundsAdjustedAmount(String partyId,
                                       String shopId,
                                       String currencyCode,
                                       Optional<LocalDateTime> fromTime,
                                       LocalDateTime toTime) {
        LocalDateTime reportFromTime = fromTime.orElse(LocalDateTime.now());
        LocalDateTime fromTimeTruncHour = reportFromTime.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime toTimeTruncHour = toTime.truncatedTo(ChronoUnit.HOURS);

        var youngAdjustmentFundsQuery = getAdjustmentShopAccountingReportData(
                partyId, shopId, currencyCode, reportFromTime, fromTimeTruncHour.plusHours(1L)
        );
        var adjustmentAggByHourShopAccountingQuery = getAdjustmentAggByHourShopAccountingQuery(
                partyId, shopId, currencyCode, fromTimeTruncHour, toTimeTruncHour
        );
        var oldAdjustmentFundsQuery = getAdjustmentShopAccountingReportData(
                partyId, shopId, currencyCode, toTimeTruncHour, toTime
        );
        var fundsAdjustedAmountResult = getDslContext()
                .select(DSL.sum(DSL.field(AMOUNT_KEY, Long.class)).as(AMOUNT_KEY))
                .from(
                        youngAdjustmentFundsQuery
                                .unionAll(adjustmentAggByHourShopAccountingQuery)
                                .unionAll(oldAdjustmentFundsQuery)
                )
                .fetchOne();
        return getFundsAmountResult(fundsAdjustedAmountResult);
    }

    private SelectConditionStep<Record1<BigDecimal>> getAdjustmentAggByHourShopAccountingQuery(String partyId,
                                                                                    String partyShopId,
                                                                                    String currencyCode,
                                                                                    LocalDateTime fromTime,
                                                                                    LocalDateTime toTime) {
        return getDslContext()
                .select(DSL.sum(ADJUSTMENT_AGGS_BY_HOUR.AMOUNT).as(AMOUNT_KEY))
                .from(ADJUSTMENT_AGGS_BY_HOUR)
                .where(ADJUSTMENT_AGGS_BY_HOUR.CREATED_AT.greaterThan(fromTime))
                .and(ADJUSTMENT_AGGS_BY_HOUR.CREATED_AT.lessThan(toTime))
                .and(ADJUSTMENT_AGGS_BY_HOUR.CURRENCY_CODE.eq(currencyCode))
                .and(ADJUSTMENT_AGGS_BY_HOUR.PARTY_ID.eq(partyId))
                .and(ADJUSTMENT_AGGS_BY_HOUR.SHOP_ID.eq(partyShopId));
    }


    private SelectConditionStep<Record1<BigDecimal>> getAdjustmentShopAccountingReportData(String partyId,
                                                                                String partyShopId,
                                                                                String currencyCode,
                                                                                LocalDateTime fromTime,
                                                                                LocalDateTime toTime) {
        return getDslContext()
                .select(DSL.sum(ADJUSTMENT.AMOUNT).as(AMOUNT_KEY))
                .from(ADJUSTMENT)
                .where(ADJUSTMENT.STATUS_CREATED_AT.greaterOrEqual(fromTime))
                .and(ADJUSTMENT.STATUS_CREATED_AT.lessThan(toTime))
                .and(ADJUSTMENT.STATUS.eq(AdjustmentStatus.captured))
                .and(ADJUSTMENT.CURRENCY_CODE.eq(currencyCode))
                .and(ADJUSTMENT.PARTY_ID.eq(partyId))
                .and(ADJUSTMENT.SHOP_ID.eq(partyShopId));
    }

}
