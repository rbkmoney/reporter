package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportDao {

    DSLContext getDSLContext();

    Report getReport(String partyId, String shopId, long reportId);

    List<FileMeta> getReportFiles(long reportId);

    void changeReportStatus(long reportId, ReportStatus status);

    FileMeta getFile(String fileId);

    String attachFile(long reportId, FileMeta file);

    List<Report> getPendingReportsByType(ReportType reportType);

    List<Report> getReportsByRange(String partyId, String shopId, List<ReportType> reportTypes, LocalDateTime fromTime, LocalDateTime toTime);

    long createReport(String partyId, String shopId, LocalDateTime fromTime, LocalDateTime toTime, ReportType reportType, String timezone, LocalDateTime createdAt);
}
