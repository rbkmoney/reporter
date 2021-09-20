package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.records.AdjustmentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentDetailsRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationRefundRecord;
import com.rbkmoney.reporter.domain.tables.records.InvoiceRecord;
import com.rbkmoney.reporter.domain.tables.records.PaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.RefundRecord;
import com.rbkmoney.reporter.model.LocalReportFilter;
import org.jooq.Cursor;
import org.jooq.Result;

import java.util.Map;

public interface LocalStatisticService {

    Map<String, String> getPurposes(LocalReportFilter filter);

    InvoiceRecord getInvoice(String invoiceId);

    Cursor<PaymentRecord> getPaymentsCursor(LocalReportFilter filter);

    Cursor<AllocationPaymentRecord> getAllocationPaymentsCursor(LocalReportFilter filter);

    Result<AllocationPaymentDetailsRecord> getAllocationPaymentsDetails(LocalReportFilter filter);

    PaymentRecord getCapturedPayment(String partyId, String shopId, String invoiceId, String paymentId);

    Cursor<RefundRecord> getRefundsCursor(LocalReportFilter filter);

    Cursor<AllocationRefundRecord> getAllocationRefundsCursor(LocalReportFilter filter);

    Cursor<AdjustmentRecord> getAdjustmentCursor(LocalReportFilter filter);

}
