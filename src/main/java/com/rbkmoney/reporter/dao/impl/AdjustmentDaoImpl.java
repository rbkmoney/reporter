package com.rbkmoney.reporter.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.rbkmoney.reporter.dao.AbstractGenericDao;
import com.rbkmoney.reporter.dao.AdjustmentDao;
import com.rbkmoney.reporter.dao.mapper.RecordRowMapper;
import com.rbkmoney.reporter.dao.routines.RoutinesWrapper;
import com.rbkmoney.reporter.domain.enums.AdjustmentStatus;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.records.AdjustmentRecord;
import com.rbkmoney.reporter.exception.DaoException;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.tables.Adjustment.ADJUSTMENT;
import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;

@Component
public class AdjustmentDaoImpl extends AbstractGenericDao implements AdjustmentDao {

    private final RowMapper<Adjustment> adjustmentRowMapper;

    @Autowired
    public AdjustmentDaoImpl(DataSource dataSource) {
        super(dataSource);
        adjustmentRowMapper = new RecordRowMapper<>(ADJUSTMENT, Adjustment.class);
    }

    @Override
    public Long save(Adjustment adjustment) throws DaoException {
        AdjustmentRecord record = getDslContext().newRecord(ADJUSTMENT, adjustment);
        Query query = getDslContext().insertInto(ADJUSTMENT)
                .set(record)
                .onConflict(ADJUSTMENT.INVOICE_ID, ADJUSTMENT.PAYMENT_ID, ADJUSTMENT.ADJUSTMENT_ID, ADJUSTMENT.SEQUENCE_ID)
                .doUpdate()
                .set(record)
                .returning(ADJUSTMENT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Adjustment get(String invoiceId, String paymentId, String adjustmentId) throws DaoException {
        Query query = getDslContext().selectFrom(ADJUSTMENT)
                .where(
                        ADJUSTMENT.INVOICE_ID.eq(invoiceId)
                                .and(ADJUSTMENT.PAYMENT_ID.eq(paymentId))
                                .and(ADJUSTMENT.ADJUSTMENT_ID.eq(adjustmentId))
                                .and(ADJUSTMENT.CURRENT)
                );
        return fetchOne(query, adjustmentRowMapper);
    }

    @Override
    public Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException {
        String key = "funds_adjusted";
        Query query = getDslContext().select(
                RoutinesWrapper.getPaymentFee().minus(RoutinesWrapper.getAdjustmentFee()).as(key)
        )
                .from(ADJUSTMENT)
                .join(PAYMENT)
                .on(
                        ADJUSTMENT.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(ADJUSTMENT.PARTY_SHOP_ID.eq(partyShopId))
                                .and(ADJUSTMENT.INVOICE_ID.eq(PAYMENT.INVOICE_ID))
                                .and(ADJUSTMENT.PAYMENT_ID.eq(PAYMENT.PAYMENT_ID))
                                .and(ADJUSTMENT.ADJUSTMENT_STATUS.eq(AdjustmentStatus.captured))
                                .and(fromTime.map(ADJUSTMENT.EVENT_CREATED_AT::ge).orElse(DSL.trueCondition()))
                                .and(ADJUSTMENT.EVENT_CREATED_AT.lt(toTime))
                                .and(PAYMENT.PARTY_ID.eq(UUID.fromString(partyId)))
                                .and(PAYMENT.PARTY_SHOP_ID.eq(partyShopId))
                                .and(PAYMENT.PAYMENT_CURRENCY_CODE.eq(currencyCode))
                                .and(PAYMENT.PAYMENT_STATUS.eq(InvoicePaymentStatus.captured))
                                .and(PAYMENT.EVENT_TYPE.eq(InvoiceEventType.INVOICE_PAYMENT_STATUS_CHANGED))
                                .and(fromTime.map(PAYMENT.EVENT_CREATED_AT::ge).orElse(DSL.trueCondition()))
                                .and(PAYMENT.EVENT_CREATED_AT.lt(toTime))
                );
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

    @Override
    public void updateNotCurrent(String invoiceId, String paymentId, String adjustmentId) throws DaoException {
        Query query = getDslContext().update(ADJUSTMENT).set(ADJUSTMENT.CURRENT, false)
                .where(
                        ADJUSTMENT.INVOICE_ID.eq(invoiceId)
                                .and(ADJUSTMENT.PAYMENT_ID.eq(paymentId))
                                .and(ADJUSTMENT.ADJUSTMENT_ID.eq(adjustmentId))
                                .and(ADJUSTMENT.CURRENT)
                );
        executeOne(query);
    }
}
