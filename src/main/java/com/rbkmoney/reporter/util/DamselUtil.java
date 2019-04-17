package com.rbkmoney.reporter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.rbkmoney.damsel.base.InvalidRequest;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.reports.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.serializer.kit.json.JsonHandler;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import org.apache.thrift.TBase;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static Map<FeeType, Long> getFees(List<FinalCashFlowPosting> finalCashFlowPostings) {
        return finalCashFlowPostings.stream()
                .collect(
                        Collectors.groupingBy(
                                DamselUtil::getFeeType,
                                Collectors.summingLong(posting -> posting.getVolume().getAmount())
                        )
                );
    }

    public static FeeType getFeeType(FinalCashFlowPosting cashFlowPosting) {
        CashFlowAccount source = cashFlowPosting.getSource().getAccountType();
        CashFlowAccount destination = cashFlowPosting.getDestination().getAccountType();

        if (source.isSetProvider() && source.getProvider() == ProviderCashFlowAccount.settlement
                && destination.isSetMerchant() && destination.getMerchant() == MerchantCashFlowAccount.settlement) {
            return FeeType.AMOUNT;
        }

        if (source.isSetMerchant()
                && source.getMerchant() == MerchantCashFlowAccount.settlement
                && destination.isSetSystem()) {
            return FeeType.FEE;
        }

        if (source.isSetSystem()
                && destination.isSetExternal()) {
            return FeeType.EXTERNAL_FEE;
        }

        if (source.isSetSystem()
                && destination.isSetProvider()) {
            return FeeType.PROVIDER_FEE;
        }

        return FeeType.UNKNOWN;
    }

    public static Report toDamselReport(com.rbkmoney.reporter.domain.tables.pojos.Report report, List<com.rbkmoney.reporter.domain.tables.pojos.FileMeta> files) throws IllegalArgumentException {
        Report dReport = new Report();
        dReport.setReportId(report.getId());
        dReport.setStatus(ReportStatus.valueOf(report.getStatus().getLiteral()));
        ReportTimeRange timeRange = new ReportTimeRange(
                TypeUtil.temporalToString(report.getFromTime()),
                TypeUtil.temporalToString(report.getToTime())
        );
        dReport.setTimeRange(timeRange);
        dReport.setReportType(ReportType.valueOf(report.getType().name()));
        dReport.setCreatedAt(TypeUtil.temporalToString(report.getCreatedAt()));

        dReport.setFiles(files.stream()
                .map(DamselUtil::toDamselFile)
                .collect(Collectors.toList()));

        return dReport;
    }

    public static FileMeta toDamselFile(com.rbkmoney.reporter.domain.tables.pojos.FileMeta file) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setFileId(file.getFileId());
        fileMeta.setFilename(file.getFilename());
        Signature signature = new Signature();
        signature.setMd5(file.getMd5());
        signature.setSha256(file.getSha256());
        fileMeta.setSignature(signature);
        return fileMeta;
    }

    public static InvalidRequest buildInvalidRequest(Throwable throwable) {
        return buildInvalidRequest(throwable.getMessage());
    }

    public static InvalidRequest buildInvalidRequest(String... messages) {
        return new InvalidRequest(Arrays.asList(messages));
    }

}
