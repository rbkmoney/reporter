package com.rbkmoney.reporter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.rbkmoney.damsel.base.InvalidRequest;
import com.rbkmoney.damsel.domain.InvoicePaymentAdjustmentStatus;
import com.rbkmoney.damsel.reports.Report;
import com.rbkmoney.damsel.reports.ReportStatus;
import com.rbkmoney.damsel.reports.ReportTimeRange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.serializer.kit.json.JsonHandler;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import org.apache.thrift.TBase;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class DamselUtil {

    public static String toJsonString(TBase tBase) {
        return toJson(tBase).toString();
    }

    public static JsonNode toJson(TBase tBase) {
        try {
            return new TBaseProcessor().process(tBase, new JsonHandler());
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static String getInvoiceStatusDetails(com.rbkmoney.damsel.domain.InvoiceStatus invoiceStatus) {
        switch (invoiceStatus.getSetField()) {
            case FULFILLED:
                return invoiceStatus.getFulfilled().getDetails();
            case CANCELLED:
                return invoiceStatus.getCancelled().getDetails();
            default:
                return null;
        }
    }

    public static LocalDateTime getAdjustmentStatusCreatedAt(InvoicePaymentAdjustmentStatus adjustmentStatus) {
        switch (adjustmentStatus.getSetField()) {
            case CAPTURED:
                return TypeUtil.stringToLocalDateTime(adjustmentStatus.getCaptured().getAt());
            case CANCELLED:
                return TypeUtil.stringToLocalDateTime(adjustmentStatus.getCancelled().getAt());
            default:
                return null;
        }
    }

    public static Report toDamselReport(com.rbkmoney.reporter.domain.tables.pojos.Report report, List<String> fileDataIds) throws IllegalArgumentException {
        Report dReport = new Report();
        dReport.setReportId(report.getId());
        dReport.setTimeRange(getTimeRange(report));
        dReport.setCreatedAt(TypeUtil.temporalToString(report.getCreatedAt()));
        dReport.setReportType(report.getType().name());
        dReport.setStatus(ReportStatus.valueOf(report.getStatus().getLiteral()));
        dReport.setFileDataIds(fileDataIds);

        return dReport;
    }

    private static ReportTimeRange getTimeRange(com.rbkmoney.reporter.domain.tables.pojos.Report report) {
        return new ReportTimeRange(
                TypeUtil.temporalToString(report.getFromTime()),
                TypeUtil.temporalToString(report.getToTime())
        );
    }

    public static InvalidRequest buildInvalidRequest(Throwable throwable) {
        return buildInvalidRequest(throwable.getMessage());
    }

    public static InvalidRequest buildInvalidRequest(String... messages) {
        return new InvalidRequest(Arrays.asList(messages));
    }

}
