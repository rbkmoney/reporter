package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.merch_stat.*;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.service.PartyService;
import com.rbkmoney.reporter.service.StatisticService;
import com.rbkmoney.reporter.service.TemplateService;
import com.rbkmoney.reporter.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Map;

@Component
public class PaymentRegistryTemplateImpl implements TemplateService {

    private final StatisticService statisticService;

    private final PartyService partyService;

    @Autowired
    public PaymentRegistryTemplateImpl(
            StatisticService statisticService,
            PartyService partyService) {
        this.statisticService = statisticService;
        this.partyService = partyService;
    }

    @Override
    public boolean accept(ReportType reportType) {
        return reportType == ReportType.payment_registry || reportType == ReportType.provision_of_service;
    }

    @Override
    public void processReportTemplate(Report report, OutputStream outputStream) throws
            IOException {
        ZoneId reportZoneId = ZoneId.of(report.getTimezone());
        String fromTime = TimeUtil.toLocalizedDate(report.getFromTime().toInstant(ZoneOffset.UTC), reportZoneId);
        String toTime = TimeUtil.toLocalizedDate(report.getToTime().minusNanos(1).toInstant(ZoneOffset.UTC), reportZoneId);

        Iterator<StatPayment> paymentsIterator = statisticService.getCapturedPaymentsIterator(
                report.getPartyId(),
                report.getPartyShopId(),
                report.getFromTime().toInstant(ZoneOffset.UTC),
                report.getToTime().toInstant(ZoneOffset.UTC)
        );

        Iterator<StatRefund> refundsIterator = statisticService.getRefundsIterator(
                report.getPartyId(),
                report.getPartyShopId(),
                report.getFromTime().toInstant(ZoneOffset.UTC),
                report.getToTime().toInstant(ZoneOffset.UTC)
        );

        Map<String, String> shopUrls = partyService.getShopUrls(report.getPartyId());
        Map<String, String> purposes = statisticService.getPurposes(report.getPartyId(), report.getPartyShopId(),
                report.getFromTime().toInstant(ZoneOffset.UTC), report.getToTime().toInstant(ZoneOffset.UTC));

        ReportCreator reportCreator = new ReportCreator(fromTime, toTime, paymentsIterator, refundsIterator, report,
                outputStream, shopUrls, purposes, statisticService);
        reportCreator.createReport();
    }
}
