package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.File;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.model.PartyModel;
import com.rbkmoney.reporter.model.ShopAccountingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private ReportService reportService;

    @Autowired
    private PartyService partyService;

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private StorageService storageService;

    @Value("${storage.bucketName}")
    private String bucketName;

    @Scheduled(fixedDelay=500)
    public void processProvisionOfServicePendingTasks() {
        List<Report> reports = reportService.getPendingReportsByType(ReportType.provision_of_service);
        for (Report report : reports) {
            Instant fromTime = report.getFromTime().toInstant(ZoneOffset.UTC);
            Instant toTime = report.getToTime().toInstant(ZoneOffset.UTC);
            Instant createdAt = report.getCreatedAt().toInstant(ZoneOffset.UTC);

            report.getToTime().toInstant(ZoneOffset.UTC);

            PartyModel partyModel = partyService.getPartyRepresentation(
                    report.getPartyId(),
                    report.getPartyShopId(),
                    createdAt
            );

            ShopAccountingModel shopAccountingModel = statisticService.getShopAccounting(
                    report.getPartyId(),
                    report.getPartyShopId(),
                    fromTime,
                    toTime
            );

            try {
                Path reportFile = Files.createTempFile("provision_of_service_", "_report.xlsx");
                reportService.generateProvisionOfServiceReport(
                        partyModel,
                        shopAccountingModel,
                        fromTime,
                        toTime,
                        Files.newOutputStream(reportFile)
                );

                //TODO report uniq id
                File reportFileModel = storageService.saveFile(
                        UUID.randomUUID().toString(),
                        bucketName,
                        reportFile.getFileName().toString(),
                        Files.newInputStream(reportFile)
                );

                reportService.finishedReportTask(report, reportFileModel);

                Files.delete(reportFile);

            } catch (IOException ex) {
                //TODO блэт
                ex.printStackTrace();
            }
        }
    }

}
