package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.ReportDao;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.FileInfo;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.*;
import com.rbkmoney.reporter.service.FileStorageService;
import com.rbkmoney.reporter.service.ReportService;
import com.rbkmoney.reporter.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportDao reportDao;

    private final List<TemplateService> templateServices;
    private final FileStorageService fileStorageService;

    @Value("${report.defaultTimeZone}")
    private ZoneId defaultTimeZone;

    @Value("${report.batchSize}")
    private int batchSize;

    public List<Report> getReportsByRangeNotCancelled(String partyId, String shopId, Instant fromTime, Instant toTime, List<String> reportTypes) throws StorageException {
        return getReportsByRange(partyId, shopId, fromTime, toTime, reportTypes).stream()
                .filter(report -> report.getStatus() != ReportStatus.cancelled)
                .collect(Collectors.toList());
    }

    @Override
    public List<Report> getReportsByRange(String partyId, String shopId, Instant fromTime, Instant toTime, List<String> reportTypes) throws StorageException {
        try {
            return reportDao.getReportsByRange(
                    partyId,
                    shopId,
                    LocalDateTime.ofInstant(fromTime, ZoneOffset.UTC),
                    LocalDateTime.ofInstant(toTime, ZoneOffset.UTC),
                    Arrays.stream(ReportType.values())
                            .filter(reportType -> reportTypes.contains(reportType.getLiteral()))
                            .collect(Collectors.toList())
            );
        } catch (DaoException ex) {
            throw new StorageException(String.format("Failed to get reports by range, partyId='%s', shopId='%s', reportTypes='%s', fromTime='%s', toTime='%s'",
                    partyId, shopId, reportTypes, fromTime, toTime), ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Report> getPendingReports() throws StorageException {
        try {
            return reportDao.getPendingReports(batchSize);
        } catch (DaoException ex) {
            throw new StorageException("Failed to get pending reports", ex);
        }
    }

    @Override
    public List<String> getReportFileDataIds(long reportId) throws StorageException {
        try {
            log.info("Trying to get files information, reportId='{}'", reportId);

            List<String> fileIds = reportDao.getByReportIds(reportId).stream()
                    .map(FileInfo::getFileDataId)
                    .collect(Collectors.toList());
            log.info("Files information for report have been found, reportId='{}'", reportId);
            return fileIds;
        } catch (DaoException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public Report getReport(String partyId, String shopId, long reportId) throws ReportNotFoundException, StorageException {
        try {
            Report report = reportDao.getReport(partyId, shopId, reportId);
            if (report == null) {
                throw new ReportNotFoundException(String.format("Report not found, partyId='%s', shopId='%s', reportId='%d'", partyId, shopId, reportId));
            }
            return report;
        } catch (DaoException ex) {
            throw new StorageException(String.format("Failed to get report from storage, partyId='%s', shopId='%s', reportId='%d'", partyId, shopId, reportId), ex);
        }
    }

    @Override
    public long createReport(String partyId, String shopId, Instant fromTime, Instant toTime, String reportType) throws PartyNotFoundException, ShopNotFoundException {
        return createReport(partyId, shopId, fromTime, toTime, reportType, defaultTimeZone, Instant.now());
    }

    @Override
    public long createReport(String partyId, String shopId, Instant fromTime, Instant toTime, String reportType, ZoneId timezone, Instant createdAt) throws PartyNotFoundException, ShopNotFoundException {
        log.info("Trying to create report, partyId={}, shopId={}, reportType={}, fromTime={}, toTime={}",
                partyId, shopId, reportType, fromTime, toTime);

        try {
            long reportId = reportDao.createReport(
                    partyId,
                    shopId,
                    LocalDateTime.ofInstant(fromTime, ZoneOffset.UTC),
                    LocalDateTime.ofInstant(toTime, ZoneOffset.UTC),
                    Arrays.stream(ReportType.values())
                            .filter(r -> r.getLiteral().equals(reportType))
                            .findFirst()
                            .orElse(null),
                    timezone.getId(),
                    LocalDateTime.ofInstant(createdAt, ZoneOffset.UTC)
            );
            log.info("Report has been successfully created, reportId={}, partyId={}, shopId={}, reportType={}, fromTime={}, toTime={}",
                    reportId, partyId, shopId, reportType, fromTime, toTime);
            return reportId;
        } catch (DaoException ex) {
            throw new StorageException(String.format("Failed to save report in storage, partyId='%s', shopId='%s', fromTime='%s', toTime='%s', reportType='%s'",
                    partyId, shopId, fromTime, toTime, reportType), ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void generateReport(Report report) {
        log.info(
                "Trying to process report, reportId='{}', reportType='{}', partyId='{}', shopId='{}', fromTime='{}', toTime='{}'",
                report.getId(), report.getType(), report.getPartyId(), report.getPartyShopId(), report.getFromTime(), report.getToTime()
        );
        try {
            List<String> fileDataIds = processSignAndUpload(report);
            finishedReportTask(report, fileDataIds);
            log.info(
                    "Report has been successfully processed, reportId='{}', reportType='{}', partyId='{}', shopId='{}', fromTime='{}', toTime='{}'",
                    report.getId(), report.getType(), report.getPartyId(), report.getPartyShopId(), report.getFromTime(), report.getToTime()
            );
        } catch (ValidationException ex) {
            log.error("Report data validation failed, reportId='{}'", report.getId(), ex);
            changeReportStatus(report, ReportStatus.cancelled);
        } catch (Throwable throwable) {
            log.error(
                    "The report has failed to process, reportId='{}', reportType='{}', partyId='{}', shopId='{}', fromTime='{}', toTime='{}'",
                    report.getId(), report.getType(), report.getPartyId(), report.getPartyShopId(), report.getFromTime(), report.getToTime(), throwable
            );
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void cancelReport(String partyId, String shopId, long reportId) throws ReportNotFoundException, StorageException {
        log.info("Trying to cancel report, reportId='{}'", reportId);
        Report report = getReport(partyId, shopId, reportId);
        changeReportStatus(report, ReportStatus.cancelled);
        log.info("Report have been cancelled, reportId='{}'", reportId);
    }

    @Override
    public void changeReportStatus(Report report, ReportStatus reportStatus) {
        log.info("Trying to change report status, reportId='{}', reportStatus='{}'", report.getId(), reportStatus);
        try {
            reportDao.changeReportStatus(report.getId(), reportStatus);
            log.info("Report status have been successfully changed, reportId='{}', reportStatus='{}'", report.getId(), reportStatus);
        } catch (DaoException ex) {
            throw new StorageException(String.format("Failed to change report status, reportId='%d', reportStatus='%s'", report.getId(), reportStatus), ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void finishedReportTask(Report report, List<String> fileDataIds) throws StorageException {
        try {
            String fileDataIdsLog = fileDataIds.stream()
                    .collect(Collectors.joining(", ", "[", "]"));

            logInfo("Save report files information, fileDataIds: " + fileDataIdsLog + "; report: ", report);
            for (String fileDataId : fileDataIds) {
                reportDao.attachFileInfo(report.getId(), fileDataId);
            }

            logInfo("Change report status on [created], ", report);
            reportDao.changeReportStatus(report.getId(), ReportStatus.created);
        } catch (DaoException ex) {
            throw new StorageException(String.format("Failed to finish report task, reportId='%d'", report.getId()), ex);
        }
    }

    @Override
    public List<String> processSignAndUpload(Report report) throws IOException, FileStorageException {
        List<String> fileDataIds = new ArrayList<>();
        for (TemplateService templateService : templateServices) {
            if (templateService.accept(report.getType())) {
                logInfo("Create temp report file, report: ", report);
                Path reportFile = Files.createTempFile(report.getType() + "_", "_report.xlsx");
                try {
                    logInfo("Fill temp report file in with data, report: ", report);
                    templateService.processReportTemplate(report, Files.newOutputStream(reportFile));

                    logInfo("Save temp report file in file storage, report: ", report);
                    String fileDataId = fileStorageService.saveFile(reportFile);

                    fileDataIds.add(fileDataId);
                } finally {
                    logInfo("Delete temp report file, report: ", report);
                    Files.deleteIfExists(reportFile);
                }
            }
        }
        return fileDataIds;
    }

    private void logInfo(String message, Report report) {
        String format = getFormatMessage(message, report);
        log.info(format);
    }

    private String getFormatMessage(String message, Report report) {
        return String.format(
                message +
                        "reportId='%s', " +
                        "partyId='%s', " +
                        "contractId='%s', " +
                        "fromTime='%s', " +
                        "toTime='%s', " +
                        "createdAt='%s', " +
                        "reportType='%s', " +
                        "status='%s'"
                ,
                report.getId(),
                report.getPartyId(),
                report.getPartyShopId(),
                report.getFromTime().toString(),
                report.getToTime().toString(),
                report.getCreatedAt().toString(),
                report.getType(),
                report.getStatus()
        );
    }
}
