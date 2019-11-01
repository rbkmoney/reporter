package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.dao.mapper.PaymentPartyDataRowMapper;
import com.rbkmoney.reporter.dao.mapper.PaymentRegistryReportDataRowMapper;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
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

import static com.rbkmoney.reporter.domain.Tables.*;
import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;

@Component
public class PaymentDaoImpl extends AbstractGenericDao implements PaymentDao {

    private final PaymentRegistryReportDataRowMapper reportDataRowMapper;
    private final PaymentPartyDataRowMapper paymentPartyDataRowMapper;

    @Autowired
    public PaymentDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
        reportDataRowMapper = new PaymentRegistryReportDataRowMapper();
        paymentPartyDataRowMapper = new PaymentPartyDataRowMapper();
    }

    @Override
    public PaymentPartyData getPaymentPartyData(PaymentInvoiceUniqueBatchKey uniqueBatchKey) throws DaoException {
        Query query = getDslContext()
                .select(
                        PAYMENT.PARTY_ID,
                        PAYMENT.PARTY_SHOP_ID,
                        PAYMENT_COST.AMOUNT,
                        PAYMENT_COST.CURRENCY_CODE
                )
                .from(PAYMENT)
                .innerJoin(PAYMENT_COST)
                .on(
                        PAYMENT_COST.ID.eq(
                                getDslContext().select(DSL.max(PAYMENT_COST.ID))
                                        .from(PAYMENT_COST)
                                        .where(
                                                PAYMENT_COST.INVOICE_ID.eq(PAYMENT.INVOICE_ID)
                                                        .and(PAYMENT_COST.PAYMENT_ID.eq(PAYMENT.PAYMENT_ID))
                                        )
                        )
                )
                .where(
                        PAYMENT.INVOICE_ID.eq(uniqueBatchKey.getInvoiceId())
                                .and(PAYMENT.PAYMENT_ID.eq(uniqueBatchKey.getPaymentId()))
                )
                .limit(1);

        return fetchOne(query, paymentPartyDataRowMapper);
    }

    @Override
    public Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException {
        String amountKey = "funds_acquired";
        String feeKey = "fee_charged";

        Query query = getDslContext().select(
                DSL.sum(PAYMENT_COST.AMOUNT).as(amountKey),
                DSL.sum(DSL.coalesce(PAYMENT_FEE.FEE, 0L)).as(feeKey)
        )
                .from(PAYMENT)
                .innerJoin(PAYMENT_STATE)
                .on(
                        PAYMENT_STATE.INVOICE_ID.eq(PAYMENT.INVOICE_ID)
                                .and(PAYMENT_STATE.PAYMENT_ID.eq(PAYMENT.PAYMENT_ID))
                                .and(PAYMENT_STATE.STATUS.eq(InvoicePaymentStatus.captured))
                                .and(fromTime.map(PAYMENT_STATE.EVENT_CREATED_AT::ge).orElse(DSL.trueCondition()))
                                .and(PAYMENT_STATE.EVENT_CREATED_AT.lt(toTime))
                )
                .innerJoin(PAYMENT_COST)
                .on(
                        PAYMENT_COST.ID.eq(
                                getDslContext().select(DSL.max(PAYMENT_COST.ID))
                                        .from(PAYMENT_COST)
                                        .where(
                                                PAYMENT_COST.INVOICE_ID.eq(PAYMENT.INVOICE_ID)
                                                        .and(PAYMENT_COST.PAYMENT_ID.eq(PAYMENT.PAYMENT_ID))
                                        )
                        )
                                .and(PAYMENT_COST.CURRENCY_CODE.eq(currencyCode))
                )
                .leftJoin(PAYMENT_FEE)
                .on(
                        PAYMENT_FEE.ID.eq(
                                getDslContext().select(DSL.max(PAYMENT_FEE.ID))
                                        .from(PAYMENT_FEE)
                                        .where(
                                                PAYMENT_FEE.INVOICE_ID.eq(PAYMENT.INVOICE_ID)
                                                        .and(PAYMENT_FEE.PAYMENT_ID.eq(PAYMENT.PAYMENT_ID))
                                        )
                        )
                                .and(PAYMENT_FEE.FEE_CURRENCY_CODE.eq(currencyCode))
                )
                .where(
                        PAYMENT.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(PAYMENT.PARTY_SHOP_ID.eq(partyShopId))
                );

        return Optional.ofNullable(fetchOne(query, (rs, i) -> Map.of(amountKey, rs.getLong(amountKey), feeKey, rs.getLong(feeKey)))).orElse(Map.of(amountKey, 0L, feeKey, 0L));
    }

    @Override
    public List<PaymentRegistryReportData> getPaymentRegistryReportData(String partyId, String partyShopId, LocalDateTime fromTime, LocalDateTime toTime) throws DaoException {
        Query query = getDslContext().select(
                PAYMENT.ID,
                PAYMENT_STATE.EVENT_CREATED_AT,
                PAYMENT.PARTY_ID,
                PAYMENT.PARTY_SHOP_ID,
                PAYMENT.INVOICE_ID,
                PAYMENT.PAYMENT_ID,
                PAYMENT.TOOL,
                PAYMENT.EMAIL,
                PAYMENT_COST.AMOUNT,
                PAYMENT_FEE.FEE,
                PAYMENT_FEE.PROVIDER_FEE,
                PAYMENT_FEE.EXTERNAL_FEE,
                INVOICE.PRODUCT
        )
                .from(PAYMENT)
                .innerJoin(PAYMENT_STATE)
                .on(
                        PAYMENT_STATE.INVOICE_ID.eq(PAYMENT.INVOICE_ID)
                                .and(PAYMENT_STATE.PAYMENT_ID.eq(PAYMENT.PAYMENT_ID))
                                .and(PAYMENT_STATE.STATUS.eq(InvoicePaymentStatus.captured))
                                .and(PAYMENT_STATE.EVENT_CREATED_AT.ge(fromTime))
                                .and(PAYMENT_STATE.EVENT_CREATED_AT.lt(toTime))
                )
                .innerJoin(PAYMENT_COST)
                .on(
                        PAYMENT_COST.ID.eq(
                                getDslContext().select(DSL.max(PAYMENT_COST.ID))
                                        .from(PAYMENT_COST)
                                        .where(
                                                PAYMENT_COST.INVOICE_ID.eq(PAYMENT.INVOICE_ID)
                                                        .and(PAYMENT_COST.PAYMENT_ID.eq(PAYMENT.PAYMENT_ID))
                                        )
                        )
                )
                .leftJoin(PAYMENT_FEE)
                .on(
                        PAYMENT_FEE.ID.eq(
                                getDslContext().select(DSL.max(PAYMENT_FEE.ID))
                                        .from(PAYMENT_FEE)
                                        .where(
                                                PAYMENT_FEE.INVOICE_ID.eq(PAYMENT.INVOICE_ID)
                                                        .and(PAYMENT_FEE.PAYMENT_ID.eq(PAYMENT.PAYMENT_ID))
                                        )
                        )
                )
                .leftJoin(INVOICE)
                .on(
                        INVOICE.INVOICE_ID.eq(PAYMENT.INVOICE_ID)
                )
                .where(
                        PAYMENT.PARTY_ID.eq(UUID.fromString(partyId))
                                .and(PAYMENT.PARTY_SHOP_ID.eq(partyShopId))
                );

        return fetch(query, reportDataRowMapper);
    }
}
