package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPayment;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPaymentDetails;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefund;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefundDetails;

import java.util.List;

public interface AllocationDao {

    Long saveAllocationPayment(AllocationPayment allocationPayment);

    void saveAllocationPaymentDetails(AllocationPaymentDetails allocationPaymentDetails);

    List<AllocationPayment> getAllocationPayments(
            String invoiceId,
            String paymentId,
            InvoicePaymentStatus status
    );

    Long saveAllocationRefund(AllocationRefund allocationRefund);

    void saveAllocationRefundDetails(AllocationRefundDetails allocationRefundDetails);

    List<AllocationRefund> getAllocationRefunds(
            String invoiceId,
            String paymentId,
            String refundId,
            InvoicePaymentStatus status
    );
}
