package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPayment;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPaymentDetails;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefund;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefundDetails;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationRefundRecord;
import org.jooq.Cursor;

public interface AllocationDao {

    Long saveAllocationPayment(AllocationPayment allocationPayment);

    void saveAllocationPaymentDetails(AllocationPaymentDetails allocationPaymentDetails);

    Cursor<AllocationPaymentRecord> getAllocationPaymentsCursor(
            String invoiceId,
            Long paymentId,
            InvoicePaymentStatus status
    );

    Long saveAllocationRefund(AllocationRefund allocationRefund);

    void saveAllocationRefundDetails(AllocationRefundDetails allocationRefundDetails);

    Cursor<AllocationRefundRecord> getAllocationRefundsCursor(
            String invoiceId,
            Long paymentId,
            String refundId,
            InvoicePaymentStatus status
    );
}
