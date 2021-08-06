package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractDao;
import com.rbkmoney.reporter.dao.AllocationDao;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPayment;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPaymentDetails;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefund;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefundDetails;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationRefundRecord;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Cursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.rbkmoney.reporter.domain.tables.AllocationPayment.ALLOCATION_PAYMENT;
import static com.rbkmoney.reporter.domain.tables.AllocationPaymentDetails.ALLOCATION_PAYMENT_DETAILS;
import static com.rbkmoney.reporter.domain.tables.AllocationRefund.ALLOCATION_REFUND;
import static com.rbkmoney.reporter.domain.tables.AllocationRefundDetails.ALLOCATION_REFUND_DETAILS;

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
                        ALLOCATION_PAYMENT.EXT_ALLOCATION_ID
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
                .onDuplicateKeyUpdate()
                .set(getDslContext().newRecord(ALLOCATION_PAYMENT_DETAILS, allocationPaymentDetails))
                .execute();
    }

    @Override
    public Cursor<AllocationPaymentRecord> getAllocationPaymentsCursor(String invoiceId, String paymentId) {
        return getDslContext()
                .selectFrom(ALLOCATION_PAYMENT)
                .where(ALLOCATION_PAYMENT.INVOICE_ID.eq(invoiceId))
                .and(ALLOCATION_PAYMENT.PAYMENT_ID.eq(paymentId))
                .fetchLazy();
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
                        ALLOCATION_REFUND.EXT_ALLOCATION_ID
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
                .onDuplicateKeyUpdate()
                .set(getDslContext().newRecord(ALLOCATION_REFUND_DETAILS, allocationRefundDetails))
                .execute();
    }

    @Override
    public Cursor<AllocationRefundRecord> getAllocationRefundsCursor(String invoiceId, String paymentId,
                                                                     String refundId) {
        return getDslContext()
                .selectFrom(ALLOCATION_REFUND)
                .where(ALLOCATION_REFUND.INVOICE_ID.eq(invoiceId))
                .and(ALLOCATION_REFUND.PAYMENT_ID.eq(paymentId))
                .and(ALLOCATION_REFUND.REFUND_ID.eq(refundId))
                .fetchLazy();
    }
}
