package com.rbkmoney.reporter.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.rbkmoney.reporter.dao.AbstractGenericDao;
import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.dao.mapper.PaymentRegistryReportDataRowMapper;
import com.rbkmoney.reporter.dao.mapper.RecordRowMapper;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.dao.routines.RoutinesWrapper;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.records.PaymentRecord;
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

@Component
public class PaymentDaoImpl extends AbstractGenericDao implements PaymentDao {

    private final RowMapper<Payment> paymentRowMapper;
    private final PaymentRegistryReportDataRowMapper reportDataRowMapper;

    @Autowired
    public PaymentDaoImpl(DataSource dataSource) {
        super(dataSource);
        paymentRowMapper = new RecordRowMapper<>(PAYMENT, Payment.class);
        reportDataRowMapper = new PaymentRegistryReportDataRowMapper();
    }

    @Override
    public Long save(Payment payment) throws DaoException {
        PaymentRecord paymentRecord = getDslContext().newRecord(PAYMENT, payment);
        Query query = getDslContext().insertInto(PAYMENT)
                .set(paymentRecord)
                .onConflict(PAYMENT.INVOICE_ID, PAYMENT.PAYMENT_ID, PAYMENT.SEQUENCE_ID)
                .doUpdate()
                .set(paymentRecord)
                .returning(PAYMENT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Payment get(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().selectFrom(PAYMENT)
                .where(
                        PAYMENT.INVOICE_ID.eq(invoiceId)
                                .and(PAYMENT.PAYMENT_ID.eq(paymentId))
                                .and(PAYMENT.CURRENT)
                );
        return fetchOne(query, paymentRowMapper);
    }

    @Override
    public Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException {
        String amountKey = "funds_acquired";
        String feeKey = "fee_charged";
        Query query = getDslContext().select(
                RoutinesWrapper.getPaymentAmount().as(amountKey),
                RoutinesWrapper.getPaymentFee().as(feeKey)
        )
                .from(PAYMENT)
                .where(
                        PAYMENT.PARTY_ID.eq(UUID.fromString(partyId))
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
                                .put(amountKey, rs.getLong(amountKey))
                                .put(feeKey, rs.getLong(feeKey))
                                .build()
                )
        ).orElse(
                ImmutableMap.<String, Long>builder()
                        .put(amountKey, 0L)
                        .put(feeKey, 0L)
                        .build()
        );
    }

    @Override
    public List<PaymentRegistryReportData> getPaymentRegistryReportData(String partyId, String partyShopId, LocalDateTime fromTime, LocalDateTime toTime) throws DaoException {
        Query query = getDslContext()
                .select(
                        PAYMENT.ID,
                        PAYMENT.EVENT_CREATED_AT,
                        PAYMENT.EVENT_TYPE,
                        PAYMENT.PARTY_ID,
                        PAYMENT.PARTY_SHOP_ID,
                        PAYMENT.INVOICE_ID,
                        PAYMENT.PAYMENT_ID,
                        PAYMENT.PAYMENT_TOOL,
                        PAYMENT.PAYMENT_EMAIL,
                        RoutinesWrapper.getPaymentCashFlowAmount(),
                        RoutinesWrapper.getPaymentCashFlowFee(),
                        RoutinesWrapper.getPaymentCashFlowProviderFee(),
                        RoutinesWrapper.getPaymentCashFlowExternalFee(),
                        INVOICE.INVOICE_PRODUCT
                )
                .from(PAYMENT)
                .leftJoin(INVOICE)
                .on(
                        INVOICE.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(INVOICE.PARTY_SHOP_ID.eq(partyShopId))
                                .and(INVOICE.INVOICE_ID.eq(PAYMENT.INVOICE_ID))
                                .and(INVOICE.EVENT_CREATED_AT.ge(fromTime))
                                .and(INVOICE.EVENT_CREATED_AT.lt(toTime))
                )
                .where(
                        PAYMENT.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(PAYMENT.PARTY_SHOP_ID.eq(partyShopId))
                                .and(PAYMENT.EVENT_CREATED_AT.ge(fromTime))
                                .and(PAYMENT.EVENT_CREATED_AT.lt(toTime))

                )
                .orderBy(PAYMENT.ID);
        return fetch(query, reportDataRowMapper);
    }

    @Override
    public void updateNotCurrent(String invoiceId, String paymentId) throws DaoException {
        Query query = getDslContext().update(PAYMENT).set(PAYMENT.CURRENT, false)
                .where(
                        PAYMENT.INVOICE_ID.eq(invoiceId)
                                .and(PAYMENT.PAYMENT_ID.eq(paymentId))
                                .and(PAYMENT.CURRENT)
                );
        executeOne(query);
    }
}
