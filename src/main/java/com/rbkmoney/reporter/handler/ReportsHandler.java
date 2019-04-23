package com.rbkmoney.reporter.handler;

import com.rbkmoney.damsel.base.InvalidRequest;
import com.rbkmoney.damsel.reports.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.config.properties.ReportingProperties;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.exception.PartyNotFoundException;
import com.rbkmoney.reporter.exception.ReportNotFoundException;
import com.rbkmoney.reporter.exception.ShopNotFoundException;
import com.rbkmoney.reporter.service.PartyService;
import com.rbkmoney.reporter.service.ReportService;
import com.rbkmoney.reporter.util.DamselUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.reporter.util.DamselUtil.buildInvalidRequest;

/**
 * Created by tolkonepiu on 18/07/2017.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReportsHandler implements ReportingSrv.Iface {

    private final ReportingProperties reportingProperties;
    private final ReportService reportService;
    private final PartyService partyService;

    @Override
    public List<Report> getReports(ReportRequest reportRequest, List<String> reportTypes) throws DatasetTooBig, InvalidRequest, TException {
        checkArgs(reportRequest, reportTypes);

        Instant toTime = TypeUtil.stringToInstant(reportRequest.getTimeRange().getToTime());
        Instant fromTime = TypeUtil.stringToInstant(reportRequest.getTimeRange().getFromTime());

        List<com.rbkmoney.reporter.domain.tables.pojos.Report> reportsByRange = reportService.getReportsByRangeNotCancelled(
                reportRequest.getPartyId(),
                reportRequest.getShopId(),
                fromTime,
                toTime,
                reportTypes
        );

        if (reportingProperties.getReportsLimit() > 0 && reportsByRange.size() > reportingProperties.getReportsLimit()) {
            throw new DatasetTooBig(reportingProperties.getReportsLimit());
        }

        return reportsByRange.stream()
                .map(report -> DamselUtil.toDamselReport(report, reportService.getReportFileDataIds(report.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public long generateReport(ReportRequest reportRequest, String reportType) throws PartyNotFound, ShopNotFound, InvalidRequest, TException {
        checkArgs(reportRequest, Collections.singletonList(reportType));

        Instant toTime = TypeUtil.stringToInstant(reportRequest.getTimeRange().getToTime());
        Instant fromTime = TypeUtil.stringToInstant(reportRequest.getTimeRange().getFromTime());

        try {
            // проверка на существование в хелгейте
            partyService.getShop(reportRequest.getPartyId(), reportRequest.getShopId());

            return reportService.createReport(
                    reportRequest.getPartyId(),
                    reportRequest.getShopId(),
                    fromTime,
                    toTime,
                    reportType
            );
        } catch (PartyNotFoundException ex) {
            throw new PartyNotFound();
        } catch (ShopNotFoundException ex) {
            throw new ShopNotFound();
        }
    }

    @Override
    public Report getReport(String partyId, String shopId, long reportId) throws ReportNotFound, TException {
        try {
            return DamselUtil.toDamselReport(
                    reportService.getReport(partyId, shopId, reportId),
                    reportService.getReportFileDataIds(reportId)
            );
        } catch (ReportNotFoundException ex) {
            throw new ReportNotFound();
        }
    }

    @Override
    public void cancelReport(String partyId, String shopId, long reportId) throws ReportNotFound, TException {
        try {
            reportService.cancelReport(partyId, shopId, reportId);
        } catch (ReportNotFoundException ex) {
            throw new ReportNotFound();
        }
    }

    private void checkArgs(ReportRequest reportRequest, List<String> reportTypes) throws InvalidRequest {
        try {
            Instant fromTime = TypeUtil.stringToInstant(reportRequest.getTimeRange().getFromTime());
            Instant toTime = TypeUtil.stringToInstant(reportRequest.getTimeRange().getToTime());

            if (fromTime.isAfter(toTime)) {
                throw new IllegalArgumentException("fromTime must be less that toTime");
            }

            for (String reportType : reportTypes) {
                checkReportType(reportType);
            }
        } catch (IllegalArgumentException ex) {
            throw buildInvalidRequest(ex);
        }
    }

    private void checkReportType(String reportType) throws IllegalArgumentException {
        if (Arrays.stream(ReportType.values())
                .noneMatch(r -> r.getLiteral().equals(reportType))) {
            throw new IllegalArgumentException("reportType does not exist");
        }
    }
}
