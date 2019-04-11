package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.*;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public interface ReportService {

    List<Report> getReportsByRange(String partyId, String shopId, List<ReportType> reportTypes, Instant fromTime, Instant toTime) throws StorageException;

    List<Report> getPendingReports() throws StorageException;

    List<FileMeta> getReportFiles(long reportId) throws StorageException;

    Report getReport(String partyId, String shopId, long reportId) throws ReportNotFoundException, StorageException;

    long createReport(String partyId, String shopId, Instant fromTime, Instant toTime, ReportType reportType) throws PartyNotFoundException, ShopNotFoundException;

    long createReport(String partyId, String shopId, Instant fromTime, Instant toTime, ReportType reportType, ZoneId timezone, Instant createdAt) throws PartyNotFoundException, ShopNotFoundException;

    URL generatePresignedUrl(String fileId, Instant expiresIn) throws FileNotFoundException, StorageException;

    void generateReport(Report report);

    void cancelReport(String partyId, String shopId, long reportId) throws ReportNotFoundException, StorageException;

    void changeReportStatus(Report report, ReportStatus reportStatus);

    void finishedReportTask(long reportId, List<FileMeta> reportFiles) throws StorageException;

    List<FileMeta> processSignAndUpload(Report report) throws IOException;

}
