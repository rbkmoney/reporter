package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.dao.RefundDao;
import com.rbkmoney.reporter.dao.mapper.RefundPaymentRegistryReportDataRowMapper;
import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.enums.RefundStatus;
import com.rbkmoney.reporter.exception.DaoException;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.Tables.INVOICE;
import static com.rbkmoney.reporter.domain.Tables.PAYMENT_STATE;
import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;
import static com.rbkmoney.reporter.domain.tables.Refund.REFUND;
import static com.rbkmoney.reporter.domain.tables.RefundState.REFUND_STATE;

@Component
public class RefundDaoImpl extends AbstractGenericDao implements RefundDao {

    private final RefundPaymentRegistryReportDataRowMapper reportDataRowMapper;

    @Autowired
    public RefundDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
        reportDataRowMapper = new RefundPaymentRegistryReportDataRowMapper();
    }

    @Override
    public Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException {
        String key = "funds_refunded";

        Query query = getDslContext().select(DSL.sum(REFUND.AMOUNT).minus(DSL.sum(DSL.coalesce(REFUND.FEE, 0L))).as(key))
                .from(REFUND)
                .innerJoin(REFUND_STATE)
                .on(
                        REFUND_STATE.INVOICE_ID.eq(REFUND.INVOICE_ID)
                                .and(REFUND_STATE.PAYMENT_ID.eq(REFUND.PAYMENT_ID))
                                .and(REFUND_STATE.STATUS.eq(RefundStatus.succeeded))
                                .and(fromTime.map(REFUND_STATE.EVENT_CREATED_AT::ge).orElse(DSL.trueCondition()))
                                .and(REFUND_STATE.EVENT_CREATED_AT.lt(toTime))
                )
                .where(
                        REFUND.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(REFUND.PARTY_SHOP_ID.eq(partyShopId))
                                .and(REFUND.FEE_CURRENCY_CODE.eq(currencyCode))
                );

        return Optional.ofNullable(fetchOne(query, (rs, i) -> Map.of(key, rs.getLong(key)))).orElse(Map.of(key, 0L));
    }

    @Override
    public List<RefundPaymentRegistryReportData> getRefundPaymentRegistryReportData(String partyId, String partyShopId, LocalDateTime fromTime, LocalDateTime toTime) throws DaoException {
        Query query = getDslContext().select(
                REFUND.ID,
                REFUND_STATE.EVENT_CREATED_AT,
                PAYMENT_STATE.EVENT_CREATED_AT,
                REFUND.PARTY_ID,
                REFUND.PARTY_SHOP_ID,
                REFUND.INVOICE_ID,
                REFUND.PAYMENT_ID,
                PAYMENT.TOOL,
                PAYMENT.EMAIL,
                REFUND.AMOUNT,
                INVOICE.PRODUCT,
                REFUND.REASON,
                REFUND.CURRENCY_CODE
        )
                .from(REFUND)
                .innerJoin(REFUND_STATE)
                .on(
                        REFUND_STATE.INVOICE_ID.eq(REFUND.INVOICE_ID)
                                .and(REFUND_STATE.PAYMENT_ID.eq(REFUND.PAYMENT_ID))
                                .and(REFUND_STATE.STATUS.eq(RefundStatus.succeeded))
                                .and(REFUND_STATE.EVENT_CREATED_AT.ge(fromTime))
                                .and(REFUND_STATE.EVENT_CREATED_AT.lt(toTime))
                )
                .innerJoin(PAYMENT_STATE)
                .on(
                        PAYMENT_STATE.INVOICE_ID.eq(REFUND.INVOICE_ID)
                                .and(PAYMENT_STATE.PAYMENT_ID.eq(REFUND.PAYMENT_ID))
                                .and(PAYMENT_STATE.STATUS.eq(InvoicePaymentStatus.captured))
                                .and(PAYMENT_STATE.EVENT_CREATED_AT.ge(fromTime))
                                .and(PAYMENT_STATE.EVENT_CREATED_AT.lt(toTime))
                )
                .innerJoin(PAYMENT)
                .on(
                        PAYMENT.INVOICE_ID.eq(REFUND.INVOICE_ID)
                                .and(PAYMENT.PAYMENT_ID.eq(REFUND.PAYMENT_ID))
                )
                .leftJoin(INVOICE)
                .on(
                        INVOICE.INVOICE_ID.eq(REFUND.INVOICE_ID)
                )
                .where(
                        REFUND.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(REFUND.PARTY_SHOP_ID.eq(partyShopId))
                );

        return fetch(query, reportDataRowMapper);
    }
}
