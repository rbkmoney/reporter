package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.AdjustmentDao;
import com.rbkmoney.reporter.domain.enums.AdjustmentStatus;
import com.rbkmoney.reporter.exception.DaoException;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.tables.Adjustment.ADJUSTMENT;
import static com.rbkmoney.reporter.domain.tables.AdjustmentState.ADJUSTMENT_STATE;

@Component
public class AdjustmentDaoImpl extends AbstractGenericDao implements AdjustmentDao {

    @Autowired
    public AdjustmentDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Map<String, Long> getShopAccountingReportData(String partyId,
                                                         String partyShopId,
                                                         String currencyCode,
                                                         Optional<LocalDateTime> fromTime,
                                                         LocalDateTime toTime) throws DaoException {
        String key = "funds_adjusted";

        Query query = getDslContext().select(DSL.sum(DSL.coalesce(ADJUSTMENT.FEE, 0L)).minus(DSL.sum(DSL.coalesce(ADJUSTMENT.OLD_FEE, 0L))).as(key))
                .from(ADJUSTMENT)
                .innerJoin(ADJUSTMENT_STATE)
                .on(
                        ADJUSTMENT_STATE.INVOICE_ID.eq(ADJUSTMENT.INVOICE_ID)
                                .and(ADJUSTMENT_STATE.PAYMENT_ID.eq(ADJUSTMENT.PAYMENT_ID))
                                .and(ADJUSTMENT_STATE.STATUS.eq(AdjustmentStatus.captured))
                                .and(fromTime.map(ADJUSTMENT_STATE.EVENT_CREATED_AT::ge).orElse(DSL.trueCondition()))
                                .and(ADJUSTMENT_STATE.EVENT_CREATED_AT.lt(toTime))
                )
                .where(
                        ADJUSTMENT.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(ADJUSTMENT.PARTY_SHOP_ID.eq(partyShopId))
                                .and(ADJUSTMENT.FEE_CURRENCY_CODE.eq(currencyCode))
                                .and(ADJUSTMENT.OLD_FEE_CURRENCY_CODE.eq(currencyCode))
                );

        return Optional.ofNullable(fetchOne(query, (rs, i) -> Map.of(key, rs.getLong(key)))).orElse(Map.of(key, 0L));
    }
}
