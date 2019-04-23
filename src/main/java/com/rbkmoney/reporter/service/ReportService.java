package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.*;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public interface ReportService {

    List<Report> getReportsByRangeNotCancelled(String partyId, String shopId, Instant fromTime, Instant toTime, List<String> reportTypes) throws StorageException;

    List<Report> getReportsByRange(String partyId, String shopId, Instant fromTime, Instant toTime, List<String> reportTypes) throws StorageException;

    List<Report> getPendingReports() throws StorageException;

    List<String> getReportFileDataIds(long reportId) throws StorageException;

    Report getReport(String partyId, String shopId, long reportId) throws ReportNotFoundException, StorageException;

    long createReport(String partyId, String shopId, Instant fromTime, Instant toTime, String reportType) throws PartyNotFoundException, ShopNotFoundException;

    long createReport(String partyId, String shopId, Instant fromTime, Instant toTime, String reportType, ZoneId timezone, Instant createdAt) throws PartyNotFoundException, ShopNotFoundException;

    void generateReport(Report report);

    void cancelReport(String partyId, String shopId, long reportId) throws ReportNotFoundException, StorageException;

    void changeReportStatus(Report report, ReportStatus reportStatus);

    void finishedReportTask(Report report, List<String> fileDataIds);

    List<String> processSignAndUpload(Report report) throws IOException, FileStorageException;

}
