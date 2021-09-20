package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractDao;
import com.rbkmoney.reporter.dao.AllocationDao;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPayment;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPaymentDetails;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefund;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefundDetails;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentDetailsRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationRefundRecord;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Cursor;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.rbkmoney.reporter.domain.tables.AllocationPayment.ALLOCATION_PAYMENT;
import static com.rbkmoney.reporter.domain.tables.AllocationPaymentDetails.ALLOCATION_PAYMENT_DETAILS;
import static com.rbkmoney.reporter.domain.tables.AllocationRefund.ALLOCATION_REFUND;
import static com.rbkmoney.reporter.domain.tables.AllocationRefundDetails.ALLOCATION_REFUND_DETAILS;
import static java.util.Optional.ofNullable;
import static org.jooq.impl.DSL.trueCondition;

@Component
public class AllocationDaoImpl extends AbstractDao implements AllocationDao {

    @Autowired
    public AllocationDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long saveAllocationPayment(AllocationPayment allocationPayment) {
        return getDslContext()
                .insertInto(ALLOCATION_PAYMENT)
                .set(getDslContext().newRecord(ALLOCATION_PAYMENT, allocationPayment))
                .onConflict(
                        ALLOCATION_PAYMENT.INVOICE_ID,
                        ALLOCATION_PAYMENT.PAYMENT_ID,
                        ALLOCATION_PAYMENT.ALLOCATION_ID
                )
                .doUpdate()
                .set(getDslContext().newRecord(ALLOCATION_PAYMENT, allocationPayment))
                .returning(ALLOCATION_PAYMENT.ID)
                .fetchOne()
                .getId();
    }

    @Override
    public void saveAllocationPaymentDetails(AllocationPaymentDetails allocationPaymentDetails) {
        getDslContext()
                .insertInto(ALLOCATION_PAYMENT_DETAILS)
                .set(getDslContext().newRecord(ALLOCATION_PAYMENT_DETAILS, allocationPaymentDetails))
                .onConflict(
                        ALLOCATION_PAYMENT_DETAILS.EXT_ALLOCATION_PAYMENT_ID
                )
                .doUpdate()
                .set(getDslContext().newRecord(ALLOCATION_PAYMENT_DETAILS, allocationPaymentDetails))
                .execute();
    }

    @Override
    public List<AllocationPayment> getAllocationPayments(
            String invoiceId,
            String paymentId,
            InvoicePaymentStatus status
    ) {
        Result<AllocationPaymentRecord> records = getDslContext()
                .selectFrom(ALLOCATION_PAYMENT)
                .where(ALLOCATION_PAYMENT.INVOICE_ID.eq(invoiceId))
                .and(ALLOCATION_PAYMENT.PAYMENT_ID.eq(paymentId))
                .and(ALLOCATION_PAYMENT.STATUS.eq(status))
                .fetch();
        return records.isEmpty() ? new ArrayList<>() : records.into(AllocationPayment.class);
    }

    @Override
    public Cursor<AllocationPaymentRecord> getAllocationPaymentsCursor(String partyId, String shopId,
                                                                       Optional<LocalDateTime> fromTime,
                                                                       LocalDateTime toTime) {
        return getDslContext()
                .selectFrom(ALLOCATION_PAYMENT)
                .where(fromTime.map(ALLOCATION_PAYMENT.STATUS_CREATED_AT::ge).orElse(DSL.trueCondition()))
                .and(ALLOCATION_PAYMENT.STATUS_CREATED_AT.lt(toTime))
                .and(ALLOCATION_PAYMENT.PARTY_ID.eq(partyId))
                .and(ofNullable(shopId).map(ALLOCATION_PAYMENT.SHOP_ID::eq).orElse(trueCondition()))
                .and(ALLOCATION_PAYMENT.STATUS.eq(InvoicePaymentStatus.captured))
                .orderBy(ALLOCATION_PAYMENT.STATUS_CREATED_AT, ALLOCATION_PAYMENT.CREATED_AT)
                .fetchLazy();
    }

    @Override
    public Result<AllocationPaymentDetailsRecord> getAllocationPaymentsDetails(String partyId, String shopId,
                                                                               Optional<LocalDateTime> fromTime,
                                                                               LocalDateTime toTime) {
        return getDslContext()
                .select(ALLOCATION_PAYMENT_DETAILS.fields())
                .from(ALLOCATION_PAYMENT_DETAILS)
                .join(ALLOCATION_PAYMENT)
                .on(ALLOCATION_PAYMENT.ID.eq(ALLOCATION_PAYMENT_DETAILS.EXT_ALLOCATION_PAYMENT_ID))
                .where(fromTime.map(ALLOCATION_PAYMENT.STATUS_CREATED_AT::ge).orElse(DSL.trueCondition()))
                .and(ALLOCATION_PAYMENT.STATUS_CREATED_AT.lt(toTime))
                .and(ALLOCATION_PAYMENT.PARTY_ID.eq(partyId))
                .and(ofNullable(shopId).map(ALLOCATION_PAYMENT.SHOP_ID::eq).orElse(trueCondition()))
                .and(ALLOCATION_PAYMENT.STATUS.eq(InvoicePaymentStatus.captured))
                .orderBy(ALLOCATION_PAYMENT.STATUS_CREATED_AT, ALLOCATION_PAYMENT.CREATED_AT)
                .fetchInto(ALLOCATION_PAYMENT_DETAILS);
    }

    @Override
    public Long saveAllocationRefund(AllocationRefund allocationRefund) {
        return getDslContext()
                .insertInto(ALLOCATION_REFUND)
                .set(getDslContext().newRecord(ALLOCATION_REFUND, allocationRefund))
                .onConflict(
                        ALLOCATION_REFUND.INVOICE_ID,
                        ALLOCATION_REFUND.PAYMENT_ID,
                        ALLOCATION_REFUND.REFUND_ID,
                        ALLOCATION_REFUND.ALLOCATION_ID
                )
                .doUpdate()
                .set(getDslContext().newRecord(ALLOCATION_REFUND, allocationRefund))
                .returning(ALLOCATION_REFUND.ID)
                .fetchOne()
                .getId();
    }

    @Override
    public void saveAllocationRefundDetails(AllocationRefundDetails allocationRefundDetails) {
        getDslContext()
                .insertInto(ALLOCATION_REFUND_DETAILS)
                .set(getDslContext().newRecord(ALLOCATION_REFUND_DETAILS, allocationRefundDetails))
                .onConflict(
                        ALLOCATION_REFUND_DETAILS.EXT_ALLOCATION_REFUND_ID
                )
                .doUpdate()
                .set(getDslContext().newRecord(ALLOCATION_REFUND_DETAILS, allocationRefundDetails))
                .execute();
    }

    @Override
    public List<AllocationRefund> getAllocationRefunds(
            String invoiceId,
            String paymentId,
            String refundId,
            InvoicePaymentStatus status
    ) {
        Result<AllocationRefundRecord> records = getDslContext()
                .selectFrom(ALLOCATION_REFUND)
                .where(ALLOCATION_REFUND.INVOICE_ID.eq(invoiceId))
                .and(ALLOCATION_REFUND.PAYMENT_ID.eq(paymentId))
                .and(ALLOCATION_REFUND.REFUND_ID.eq(refundId))
                .and(ALLOCATION_REFUND.STATUS.eq(status))
                .fetch();
        return records.isEmpty() ? new ArrayList<>() : records.into(AllocationRefund.class);
    }

    @Override
    public Cursor<AllocationRefundRecord> getAllocationRefundsCursor(String partyId, String shopId,
                                                                     Optional<LocalDateTime> fromTime,
                                                                     LocalDateTime toTime) {
        return getDslContext()
                .selectFrom(ALLOCATION_REFUND)
                .where(fromTime.map(ALLOCATION_REFUND.STATUS_CREATED_AT::ge).orElse(DSL.trueCondition()))
                .and(ALLOCATION_REFUND.STATUS_CREATED_AT.lt(toTime))
                .and(ALLOCATION_REFUND.PARTY_ID.eq(partyId))
                .and(ofNullable(shopId).map(ALLOCATION_REFUND.SHOP_ID::eq).orElse(trueCondition()))
                .and(ALLOCATION_REFUND.STATUS.eq(InvoicePaymentStatus.captured))
                .orderBy(ALLOCATION_REFUND.STATUS_CREATED_AT, ALLOCATION_REFUND.CREATED_AT)
                .fetchLazy();
    }
}
