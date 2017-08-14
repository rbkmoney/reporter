package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.dao.ReportDao;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.tables.pojos.File;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.FileNotFoundException;
import com.rbkmoney.reporter.exception.PartyNotFoundException;
import com.rbkmoney.reporter.exception.ReportNotFoundException;
import com.rbkmoney.reporter.exception.ShopNotFoundException;
import com.rbkmoney.reporter.model.PartyModel;
import com.rbkmoney.reporter.model.ShopAccountingModel;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

/**
 * Created by tolkonepiu on 17/07/2017.
 */
@Service
public class ReportService {

    public static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("Europe/Moscow");

    @Autowired
    ReportDao reportDao;

    @Autowired
    private PartyService partyService;

    @Autowired
    private StorageService storageService;

    public List<Report> getReportsByRange(String partyId, String shopId, List<ReportType> reportTypes, Instant fromTime, Instant toTime) {
        return reportDao.getReportsByRange(
                partyId,
                shopId,
                reportTypes,
                LocalDateTime.ofInstant(fromTime, ZoneOffset.UTC),
                LocalDateTime.ofInstant(toTime, ZoneOffset.UTC)
        );
    }

    public List<Report> getPendingReportsByType(ReportType reportType) {
        return reportDao.getPendingReportsByType(reportType);
    }

    public List<File> getReportFiles(long reportId) {
        return reportDao.getReportFiles(reportId);
    }

    public Report getReport(String partyId, String shopId, long reportId) throws ReportNotFoundException {
        Report report = reportDao.getReport(partyId, shopId, reportId);
        if (report == null) {
            throw new ReportNotFoundException("Report not found, partyId='%s', shopId='%s', reportId='%d'", partyId, shopId, reportId);
        }
        return report;
    }

    public long generateReport(String partyId, String shopId, Instant fromTime, Instant toTime, ReportType reportType) throws PartyNotFoundException, ShopNotFoundException {
        return generateReport(partyId, shopId, fromTime, toTime, reportType, DEFAULT_TIMEZONE, Instant.now());
    }

    public long generateReport(String partyId, String shopId, Instant fromTime, Instant toTime, ReportType reportType, ZoneId timezone, Instant createdAt) throws PartyNotFoundException, ShopNotFoundException {
        PartyModel partyModel = partyService.getPartyRepresentation(partyId, shopId, createdAt);
        if (partyModel == null) {
            throw new PartyNotFoundException("Party not found, partyId='%s'", partyId);
        }

        return reportDao.createReport(
                partyId,
                shopId,
                LocalDateTime.ofInstant(fromTime, ZoneOffset.UTC),
                LocalDateTime.ofInstant(toTime, ZoneOffset.UTC),
                reportType,
                timezone.getId(),
                LocalDateTime.ofInstant(createdAt, ZoneOffset.UTC)
        );
    }

    public String generatePresignedUrl(String fileId, Instant expiresAt) throws FileNotFoundException {
        File file = reportDao.getFile(fileId);
        if (file == null) {
            throw new FileNotFoundException("File with id '%s' not found", fileId);
        }

        return storageService.getFileUrl(file.getId(), file.getBucketId(), expiresAt);
    }

    public void finishedReportTask(Report report, File... files) {
        reportDao.getDSLContext().transaction(configuration -> {
            for (File file : files) {
                reportDao.attachFile(report.getId(), file);
            }

            reportDao.changeReportStatus(report.getId(), ReportStatus.created);
        });
    }

    public void generateProvisionOfServiceReport(PartyModel partyModel, ShopAccountingModel shopAccountingModel, Instant fromTime, Instant toTime, OutputStream outputStream) throws IOException {
        Context context = new Context();
        context.putVar("shopAccounting", shopAccountingModel);
        context.putVar("partyRepresentation", partyModel);
        context.putVar("fromTime", Date.from(fromTime));
        context.putVar("toTime", Date.from(toTime));

        processTemplate(
                context,
                ReportType.provision_of_service,
                outputStream
        );
    }

    public void processTemplate(Context context, ReportType reportType, OutputStream outputStream) throws IOException {
        processTemplate(context, reportType.getTemplateResource().getInputStream(), outputStream);
    }

    public void processTemplate(Context context, InputStream inputStream, OutputStream outputStream) throws IOException {
        JxlsHelper.getInstance()
                .processTemplate(
                        inputStream,
                        outputStream,
                        context
                );
    }

}
