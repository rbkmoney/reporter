package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.File;
import com.rbkmoney.reporter.domain.tables.pojos.Report;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportDao {

    Report getReport(String partyId, String shopId, long reportId);

    List<File> getReportFiles(long reportId);

    List<Report> getReportsByRange(String partyId, String shopId, List<ReportType> reportTypes, LocalDateTime fromTime, LocalDateTime toTime);

    long createReport(String partyId, String shopId, LocalDateTime fromTime, LocalDateTime toTime, ReportType reportType, String timezone, LocalDateTime createdAt);
}
