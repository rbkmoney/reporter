package com.rbkmoney.reporter.template;

import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.domain.tables.records.AdjustmentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentDetailsRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationPaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.AllocationRefundRecord;
import com.rbkmoney.reporter.domain.tables.records.PaymentRecord;
import com.rbkmoney.reporter.domain.tables.records.RefundRecord;
import com.rbkmoney.reporter.model.LocalReportCreatorDto;
import com.rbkmoney.reporter.service.LocalStatisticService;
import com.rbkmoney.reporter.service.PartyService;
import com.rbkmoney.reporter.service.ReportCreatorService;
import com.rbkmoney.reporter.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.jooq.Cursor;
import org.jooq.Result;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LocalPaymentRegistryTemplateImpl implements ReportTemplate {

    private final PartyService partyService;

    private final ReportCreatorService<LocalReportCreatorDto> localReportCreatorService;

    private final LocalStatisticService localStatisticService;

    @Override
    public boolean isAccept(ReportType reportType) {
        return reportType == ReportType.payment_registry
                || reportType == ReportType.provision_of_service
                || reportType == ReportType.payment_registry_by_payout;
    }

    @Override
    public void processReportTemplate(Report report, OutputStream outputStream) throws
            IOException {
        String partyId = report.getPartyId();
        String shopId = report.getPartyShopId();
        LocalDateTime fromTime = report.getFromTime();
        LocalDateTime toTime = report.getToTime();
        ZoneId reportZoneId = ZoneId.of(report.getTimezone());
        String formattedFromTime =
                TimeUtil.toLocalizedDate(fromTime.toInstant(ZoneOffset.UTC), reportZoneId);
        String formattedToTime =
                TimeUtil.toLocalizedDate(toTime.minusNanos(1).toInstant(ZoneOffset.UTC), reportZoneId);

        Map<String, String> purposes =
                localStatisticService.getPurposes(partyId, shopId, fromTime, toTime);
        Map<String, String> shopUrls = partyService.getShopUrls(partyId);

        try (
                Cursor<PaymentRecord> paymentsCursor =
                        localStatisticService.getPaymentsCursor(partyId, shopId, fromTime, toTime);
                Cursor<AllocationPaymentRecord> allocationPaymentCursor =
                        localStatisticService.getAllocationPaymentsCursor(partyId, shopId, fromTime, toTime);
                Cursor<RefundRecord> refundsCursor =
                        localStatisticService.getRefundsCursor(partyId, shopId, fromTime, toTime);
                Cursor<AllocationRefundRecord> allocationRefundCursor = localStatisticService
                        .getAllocationRefundsCursor(partyId, shopId, fromTime, toTime);
                Cursor<AdjustmentRecord> adjustmentCursor =
                        localStatisticService.getAdjustmentCursor(partyId, shopId, fromTime, toTime);
        ) {
            Result<AllocationPaymentDetailsRecord> allocationPaymentDetails =
                    localStatisticService.getAllocationPaymentsDetails(partyId, shopId, fromTime, toTime);
            LocalReportCreatorDto reportCreatorDto = LocalReportCreatorDto.builder()
                    .fromTime(formattedFromTime)
                    .toTime(formattedToTime)
                    .paymentsCursor(paymentsCursor)
                    .allocationPaymentCursor(allocationPaymentCursor)
                    .allocationPaymentDetails(allocationPaymentDetails)
                    .refundsCursor(refundsCursor)
                    .allocationRefundCursor(allocationRefundCursor)
                    .adjustmentsCursor(adjustmentCursor)
                    .report(report)
                    .outputStream(outputStream)
                    .shopUrls(shopUrls)
                    .purposes(purposes)
                    .build();

            localReportCreatorService.createReport(reportCreatorDto);
        }
    }
}
