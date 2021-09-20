package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPayment;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPaymentDetails;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefund;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefundDetails;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentDetailsRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationRefundRecord;
import org.jooq.Cursor;
import org.jooq.Result;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AllocationDao {

    Long saveAllocationPayment(AllocationPayment allocationPayment);

    void saveAllocationPaymentDetails(AllocationPaymentDetails allocationPaymentDetails);

    List<AllocationPayment> getAllocationPayments(
            String invoiceId,
            String paymentId,
            InvoicePaymentStatus status
    );

    Cursor<AllocationPaymentRecord> getAllocationPaymentsCursor(
            String partyId,
            String shopId,
            Optional<LocalDateTime> fromTime,
            LocalDateTime toTime
    );

    Result<AllocationPaymentDetailsRecord> getAllocationPaymentsDetails(
            String partyId,
            String shopId,
            Optional<LocalDateTime> fromTime,
            LocalDateTime toTime
    );

    Long saveAllocationRefund(AllocationRefund allocationRefund);

    void saveAllocationRefundDetails(AllocationRefundDetails allocationRefundDetails);

    List<AllocationRefund> getAllocationRefunds(
            String invoiceId,
            String paymentId,
            String refundId,
            InvoicePaymentStatus status
    );

    Cursor<AllocationRefundRecord> getAllocationRefundsCursor(
            String partyId,
            String shopId,
            Optional<LocalDateTime> fromTime,
            LocalDateTime toTime
    );
}
