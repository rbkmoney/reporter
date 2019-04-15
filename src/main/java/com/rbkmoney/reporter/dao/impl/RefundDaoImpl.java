package com.rbkmoney.reporter.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.rbkmoney.reporter.dao.AbstractGenericDao;
import com.rbkmoney.reporter.dao.RefundDao;
import com.rbkmoney.reporter.dao.mapper.RecordRowMapper;
import com.rbkmoney.reporter.dao.mapper.RefundPaymentRegistryReportDataRowMapper;
import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
import com.rbkmoney.reporter.dao.routines.RoutinesWrapper;
import com.rbkmoney.reporter.domain.enums.RefundStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.domain.tables.records.RefundRecord;
import com.rbkmoney.reporter.exception.DaoException;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.tables.Invoice.INVOICE;
import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;
import static com.rbkmoney.reporter.domain.tables.Refund.REFUND;

@Component
public class RefundDaoImpl extends AbstractGenericDao implements RefundDao {

    private final RowMapper<Refund> refundRowMapper;
    private final RefundPaymentRegistryReportDataRowMapper reportDataRowMapper;

    @Autowired
    public RefundDaoImpl(DataSource dataSource) {
        super(dataSource);
        refundRowMapper = new RecordRowMapper<>(REFUND, Refund.class);
        reportDataRowMapper = new RefundPaymentRegistryReportDataRowMapper();
    }

    @Override
    public Optional<Long> getLastEventId() throws DaoException {
        Query query = getDslContext().select(DSL.max(REFUND.EVENT_ID)).from(REFUND);
        return Optional.ofNullable(fetchOne(query, Long.class));
    }

    @Override
    public Long save(Refund refund) throws DaoException {
        RefundRecord record = getDslContext().newRecord(REFUND, refund);
        Query query = getDslContext().insertInto(REFUND)
                .set(record)
                .onConflict(REFUND.EVENT_ID, REFUND.EVENT_TYPE, REFUND.REFUND_STATUS)
                .doUpdate()
                .set(record)
                .returning(REFUND.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Refund get(String invoiceId, String paymentId, String refundId) throws DaoException {
        Query query = getDslContext().selectFrom(REFUND)
                .where(
                        REFUND.INVOICE_ID.eq(invoiceId)
                                .and(REFUND.PAYMENT_ID.eq(paymentId))
                                .and(REFUND.REFUND_ID.eq(refundId))
                                .and(REFUND.CURRENT)
                );
        return fetchOne(query, refundRowMapper);
    }

    @Override
    public Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException {
        String key = "funds_refunded";
        Query query = getDslContext().select(
                RoutinesWrapper.getRefundAmount().minus(RoutinesWrapper.getRefundFee()).as(key)

        )
                .from(REFUND)
                .where(
                        REFUND.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(REFUND.PARTY_SHOP_ID.eq(partyShopId))
                                .and(REFUND.REFUND_CURRENCY_CODE.eq(currencyCode))
                                .and(REFUND.REFUND_STATUS.eq(RefundStatus.succeeded))
                                .and(fromTime.map(REFUND.EVENT_CREATED_AT::ge).orElse(DSL.trueCondition()))
                                .and(REFUND.EVENT_CREATED_AT.lt(toTime))
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
    public List<RefundPaymentRegistryReportData> getRefundPaymentRegistryReportData(String partyId, String partyShopId, LocalDateTime fromTime, LocalDateTime toTime) throws DaoException {
        Query query = getDslContext()
                .select(
                        REFUND.ID,
                        REFUND.EVENT_ID,
                        REFUND.EVENT_CREATED_AT,
                        PAYMENT.EVENT_CREATED_AT,
                        REFUND.EVENT_TYPE,
                        REFUND.PARTY_ID,
                        REFUND.PARTY_SHOP_ID,
                        REFUND.INVOICE_ID,
                        REFUND.PAYMENT_ID,
                        PAYMENT.PAYMENT_TOOL,
                        PAYMENT.PAYMENT_EMAIL,
                        RoutinesWrapper.getRefundCashFlowAmount(),
                        INVOICE.INVOICE_PRODUCT
                )
                .from(REFUND)
                .leftJoin(PAYMENT)
                .on(
                        PAYMENT.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(PAYMENT.PARTY_SHOP_ID.eq(partyShopId))
                                .and(PAYMENT.EVENT_CREATED_AT.ge(fromTime))
                                .and(PAYMENT.EVENT_CREATED_AT.lt(toTime))
                                .and(REFUND.INVOICE_ID.eq(PAYMENT.INVOICE_ID))
                                .and(REFUND.PAYMENT_ID.eq(PAYMENT.PAYMENT_ID))
                )
                .leftJoin(INVOICE)
                .on(
                        INVOICE.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(INVOICE.PARTY_SHOP_ID.eq(partyShopId))
                                .and(INVOICE.EVENT_CREATED_AT.ge(fromTime))
                                .and(INVOICE.EVENT_CREATED_AT.lt(toTime))
                                .and(REFUND.INVOICE_ID.eq(INVOICE.INVOICE_ID))
                )
                .where(
                        REFUND.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(REFUND.PARTY_SHOP_ID.eq(partyShopId))
                                .and(REFUND.EVENT_CREATED_AT.ge(fromTime))
                                .and(REFUND.EVENT_CREATED_AT.lt(toTime))
                )
                .orderBy(REFUND.ID);
        return fetch(query, reportDataRowMapper);
    }

    @Override
    public void updateNotCurrent(String invoiceId, String paymentId, String refundId) throws DaoException {
        Query query = getDslContext().update(REFUND).set(REFUND.CURRENT, false)
                .where(
                        REFUND.INVOICE_ID.eq(invoiceId)
                                .and(REFUND.PAYMENT_ID.eq(paymentId))
                                .and(REFUND.REFUND_ID.eq(refundId))
                                .and(REFUND.CURRENT)
                );
        executeOne(query);
    }
}
