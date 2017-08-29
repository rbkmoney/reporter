package com.rbkmoney.task;

import com.rbkmoney.reporter.AbstractIntegrationTest;
import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.service.ReportService;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

@Ignore
public class ReportServiceTest extends AbstractIntegrationTest {

    @Autowired
    ReportService reportService;

    @Test(timeout = 3000)
    public void generateReportTest() {
        long reportId = reportService.createReport("kek", "kek", Instant.now(), Instant.now(), ReportType.provision_of_service);

        Report report;
        do {
            report = reportService.getReport("kek", "kek", reportId);
        } while (report.getStatus() != ReportStatus.created);
    }

}
