package com.rbkmoney.task;

import com.rbkmoney.reporter.AbstractIntegrationTest;
import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.service.ReportService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;

public class ReportServiceTest extends AbstractIntegrationTest {

    @Autowired
    ReportService reportService;

    @Test
    public void generateProvisionOfServiceReportTest() {
        String partyId = random(String.class);
        String shopId = random(String.class);
        Instant fromTime = random(Instant.class);
        Instant toTime = random(Instant.class);
        ReportType reportType = ReportType.provision_of_service;

        long reportId = reportService.createReport(partyId, shopId, fromTime, toTime, reportType);

        Report report;
        do {
            report = reportService.getReport(partyId, shopId, reportId);
        } while (report.getStatus() != ReportStatus.created);

        assertEquals(2, reportService.getReportFiles(report.getId()).size());
    }

}
