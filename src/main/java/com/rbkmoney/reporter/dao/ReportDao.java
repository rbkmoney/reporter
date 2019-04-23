package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.FileInfo;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.exception.DaoException;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportDao extends GenericDao {

    Report getReport(String partyId, String shopId, long reportId) throws DaoException;

    List<FileInfo> getByReportIds(long reportId) throws DaoException;

    void changeReportStatus(long reportId, ReportStatus status) throws DaoException;

    Long attachFileInfo(long reportId, String fileDataId) throws DaoException;

    List<Report> getPendingReports(int limit) throws DaoException;

    List<Report> getPendingReportsByType(ReportType reportType) throws DaoException;

    List<Report> getReportsByRange(String partyId, String shopId, LocalDateTime fromTime, LocalDateTime toTime, List<ReportType> reportTypes) throws DaoException;

    long createReport(String partyId, String shopId, LocalDateTime fromTime, LocalDateTime toTime, ReportType reportType, String timezone, LocalDateTime createdAt) throws DaoException;
}
