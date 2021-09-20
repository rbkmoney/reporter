package com.rbkmoney.reporter.model;

import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.domain.tables.records.AdjustmentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentDetailsRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationRefundRecord;
import com.rbkmoney.reporter.domain.tables.records.PaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.RefundRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jooq.Cursor;
import org.jooq.Result;

import java.io.OutputStream;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class LocalReportCreatorDto {

    private String fromTime;
    private String toTime;
    private Cursor<PaymentRecord> paymentsCursor;
    private Cursor<AllocationPaymentRecord> allocationPaymentCursor;
    private Result<AllocationPaymentDetailsRecord> allocationPaymentDetails;
    private Cursor<RefundRecord> refundsCursor;
    private Cursor<AllocationRefundRecord> allocationRefundCursor;
    private Cursor<AdjustmentRecord> adjustmentsCursor;
    private Report report;
    private OutputStream outputStream;
    private Map<String, String> shopUrls;
    private Map<String, String> purposes;

}
