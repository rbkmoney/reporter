package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.AdjustmentDao;
import com.rbkmoney.reporter.dao.AllocationDao;
import com.rbkmoney.reporter.dao.InvoiceDao;
import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.dao.RefundDao;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.records.AdjustmentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentDetailsRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationRefundRecord;
import com.rbkmoney.reporter.domain.tables.records.InvoiceRecord;
import com.rbkmoney.reporter.domain.tables.records.PaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.RefundRecord;
import com.rbkmoney.reporter.exception.PaymentNotFoundException;
import com.rbkmoney.reporter.model.LocalReportFilter;
import com.rbkmoney.reporter.service.LocalStatisticService;
import lombok.RequiredArgsConstructor;
import org.jooq.Cursor;
import org.jooq.Result;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocalStatisticServiceImpl implements LocalStatisticService {

    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;
    private final RefundDao refundDao;
    private final AllocationDao allocationDao;
    private final AdjustmentDao adjustmentDao;

    @Override
    public Map<String, String> getPurposes(LocalReportFilter filter) {
        Map<String, String> purposes = new HashMap<>();
        List<Invoice> invoices = invoiceDao.getInvoices(
                filter.getPartyId(),
                filter.getShopId(),
                Optional.ofNullable(filter.getFromTime()),
                filter.getToTime()
        );
        invoices.forEach(invoice -> purposes.put(invoice.getInvoiceId(), invoice.getProduct()));
        return purposes;
    }

    @Override
    public InvoiceRecord getInvoice(String invoiceId) {
        return invoiceDao.getInvoice(invoiceId);
    }

    @Override
    public Cursor<PaymentRecord> getPaymentsCursor(LocalReportFilter filter) {
        return paymentDao.getPaymentsCursor(
                filter.getPartyId(),
                filter.getShopId(),
                Optional.ofNullable(filter.getFromTime()),
                filter.getToTime()
        );
    }

    @Override
    public Cursor<AllocationPaymentRecord> getAllocationPaymentsCursor(LocalReportFilter filter) {
        return allocationDao.getAllocationPaymentsCursor(
                filter.getPartyId(),
                filter.getShopId(),
                Optional.ofNullable(filter.getFromTime()),
                filter.getToTime()
        );
    }

    @Override
    public Result<AllocationPaymentDetailsRecord> getAllocationPaymentsDetails(LocalReportFilter filter) {
        return allocationDao.getAllocationPaymentsDetails(
                filter.getPartyId(),
                filter.getShopId(),
                Optional.ofNullable(filter.getFromTime()),
                filter.getToTime()
        );
    }

    @Override
    public PaymentRecord getCapturedPayment(String partyId, String shopId, String invoiceId, String paymentId) {
        PaymentRecord payment = paymentDao.getPayment(partyId, shopId, invoiceId, paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException(String.format("Payment not found, " +
                    "invoiceId='%s', paymentId='%s'", invoiceId, paymentId));
        }
        return payment;
    }

    @Override
    public Cursor<RefundRecord> getRefundsCursor(LocalReportFilter filter) {
        return refundDao.getRefundsCursor(
                filter.getPartyId(),
                filter.getShopId(),
                filter.getFromTime(),
                filter.getToTime()
        );
    }

    @Override
    public Cursor<AllocationRefundRecord> getAllocationRefundsCursor(LocalReportFilter filter) {
        return allocationDao.getAllocationRefundsCursor(
                filter.getPartyId(),
                filter.getShopId(),
                Optional.ofNullable(filter.getFromTime()),
                filter.getToTime()
        );
    }

    @Override
    public Cursor<AdjustmentRecord> getAdjustmentCursor(LocalReportFilter filter) {
        return adjustmentDao.getAdjustmentCursor(
                filter.getPartyId(),
                filter.getShopId(),
                filter.getFromTime(),
                filter.getToTime()
        );
    }

}
