package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.config.PostgresqlSpringBootITest;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationPayment;
import com.rbkmoney.reporter.domain.tables.pojos.AllocationRefund;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationRefundRecord;
import org.jooq.Cursor;
import org.jooq.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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

    @Test
    public void saveAndGetAllocationPaymentTest() {
        int recordsCount = 100;
        String invoiceId = random(String.class);
        String paymentId = random(String.class);
        List<AllocationPayment> sourceRecords = IntStream.range(0, recordsCount)
                .mapToObj(i -> {
                    String extAllocationId = random(String.class);
                    AllocationPayment allocationPayment = random(AllocationPayment.class);
                    allocationPayment.setInvoiceId(invoiceId);
                    allocationPayment.setPaymentId(paymentId);
                    allocationPayment.setExtAllocationId(extAllocationId);
                    allocationDao.saveAllocationPayment(allocationPayment);
                    return allocationPayment;
                }).collect(Collectors.toList());

        Cursor<AllocationPaymentRecord> cursor =
                allocationDao.getAllocationPaymentsCursor(invoiceId, paymentId);
        List<AllocationPayment> resultRecords = new ArrayList<>();
        while (cursor.hasNext()) {
            Result<AllocationPaymentRecord> allocationPaymentRecords = cursor.fetchNext(100);
            resultRecords.addAll(allocationPaymentRecords.into(AllocationPayment.class));
        }
        assertEquals(recordsCount, resultRecords.size());
        sourceRecords.sort(Comparator.comparingLong(AllocationPayment::getId));
        resultRecords.sort(Comparator.comparingLong(AllocationPayment::getId));
        assertIterableEquals(sourceRecords, resultRecords);
    }

    @Test
    public void saveAndGetAllocationRefundTest() {
        int recordsCount = 100;
        String invoiceId = random(String.class);
        String paymentId = random(String.class);
        String refundId = random(String.class);
        List<AllocationRefund> sourceRecords = IntStream.range(0, recordsCount)
                .mapToObj(i -> {
                    String extAllocationId = random(String.class);
                    AllocationRefund allocationRefund = random(AllocationRefund.class);
                    allocationRefund.setInvoiceId(invoiceId);
                    allocationRefund.setPaymentId(paymentId);
                    allocationRefund.setRefundId(refundId);
                    allocationRefund.setExtAllocationId(extAllocationId);
                    allocationDao.saveAllocationRefund(allocationRefund);
                    return allocationRefund;
                }).collect(Collectors.toList());

        Cursor<AllocationRefundRecord> cursor =
                allocationDao.getAllocationRefundsCursor(invoiceId, paymentId, refundId);
        List<AllocationRefund> resultRecords = new ArrayList<>();
        while (cursor.hasNext()) {
            Result<AllocationRefundRecord> allocationRefundRecords = cursor.fetchNext(100);
            resultRecords.addAll(allocationRefundRecords.into(AllocationRefund.class));
        }
        assertEquals(recordsCount, resultRecords.size());
        sourceRecords.sort(Comparator.comparingLong(AllocationRefund::getId));
        resultRecords.sort(Comparator.comparingLong(AllocationRefund::getId));
        assertIterableEquals(sourceRecords, resultRecords);
    }
}
