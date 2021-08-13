package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.config.PostgresqlSpringBootITest;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPayment;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefund;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.rbkmoney.testcontainers.annotations.util.RandomBeans.random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@PostgresqlSpringBootITest
public class AllocationDaoTest {

    @Autowired
    private AllocationDao allocationDao;

    @Autowired PaymentDao paymentDao;

    @Test
    public void saveAndGetAllocationPaymentTest() {
        int recordsCount = 100;

        Long extPaymentId = random(Long.class);
        createPayment(extPaymentId);
        String invoiceId = random(String.class);
        InvoicePaymentStatus status = random(InvoicePaymentStatus.class);

        List<AllocationPayment> sourceRecords = IntStream.range(0, recordsCount)
                .mapToObj(i -> {
                    String allocationId = random(String.class);
                    AllocationPayment allocationPayment = random(AllocationPayment.class);
                    allocationPayment.setInvoiceId(invoiceId);
                    allocationPayment.setExtPaymentId(extPaymentId);
                    allocationPayment.setAllocationId(allocationId);
                    allocationPayment.setStatus(status);
                    allocationDao.saveAllocationPayment(allocationPayment);
                    return allocationPayment;
                }).collect(Collectors.toList());

        List<AllocationPayment> resultRecords =
                allocationDao.getAllocationPayments(invoiceId, extPaymentId, status);

        assertEquals(recordsCount, resultRecords.size());
        sourceRecords.sort(Comparator.comparingLong(AllocationPayment::getId));
        resultRecords.sort(Comparator.comparingLong(AllocationPayment::getId));
        assertIterableEquals(sourceRecords, resultRecords);
    }

    @Test
    public void saveAndGetAllocationRefundTest() {
        int recordsCount = 100;

        Long extPaymentId = random(Long.class);
        createPayment(extPaymentId);
        String invoiceId = random(String.class);
        String refundId = random(String.class);
        InvoicePaymentStatus status = random(InvoicePaymentStatus.class);

        List<AllocationRefund> sourceRecords = IntStream.range(0, recordsCount)
                .mapToObj(i -> {
                    String allocationId = random(String.class);
                    AllocationRefund allocationRefund = random(AllocationRefund.class);
                    allocationRefund.setInvoiceId(invoiceId);
                    allocationRefund.setExtPaymentId(extPaymentId);
                    allocationRefund.setRefundId(refundId);
                    allocationRefund.setAllocationId(allocationId);
                    allocationRefund.setStatus(status);
                    allocationDao.saveAllocationRefund(allocationRefund);
                    return allocationRefund;
                }).collect(Collectors.toList());

        List<AllocationRefund> resultRecords =
                allocationDao.getAllocationRefunds(invoiceId, extPaymentId, refundId, status);

        assertEquals(recordsCount, resultRecords.size());
        sourceRecords.sort(Comparator.comparingLong(AllocationRefund::getId));
        resultRecords.sort(Comparator.comparingLong(AllocationRefund::getId));
        assertIterableEquals(sourceRecords, resultRecords);
    }

    private void createPayment(Long paymentId) {
        Payment payment = random(Payment.class);
        payment.setId(paymentId);
        paymentDao.savePayment(payment);
    }
}
