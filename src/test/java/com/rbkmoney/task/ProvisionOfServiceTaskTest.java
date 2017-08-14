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
public class ProvisionOfServiceTaskTest extends AbstractIntegrationTest {

    @Autowired
    ReportService reportService;

    @Test
    public void generateReportTest() {
        long reportId = reportService.generateReport("kek", "kek", Instant.now(), Instant.now(), ReportType.provision_of_service);

        Report report = reportService.getReport("kek", "kek", reportId);
        while (report.getStatus() != ReportStatus.created) {
            report = reportService.getReport("kek", "kek", reportId);
        }
    }

}
