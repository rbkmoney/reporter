package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.dao.AbstractGenericDao;
import com.rbkmoney.reporter.dao.ReportDao;
import com.rbkmoney.reporter.dao.mapper.RecordRowMapper;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.enums.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.FileInfo;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.domain.tables.records.FileInfoRecord;
import com.rbkmoney.reporter.exception.DaoException;
import org.jooq.Condition;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static com.rbkmoney.reporter.domain.tables.FileInfo.FILE_INFO;
import static com.rbkmoney.reporter.domain.tables.Report.REPORT;

@Component
public class ReportDaoImpl extends AbstractGenericDao implements ReportDao {

    private final RowMapper<Report> reportRowMapper;
    private final RecordRowMapper<FileInfo> fileInfoRecordRowMapper;

    @Autowired
    public ReportDaoImpl(DataSource dataSource) {
        super(dataSource);
        reportRowMapper = BeanPropertyRowMapper.newInstance(Report.class);
        fileInfoRecordRowMapper = new RecordRowMapper<>(FILE_INFO, FileInfo.class);
    }

    @Override
    public Report getReport(String partyId, String shopId, long reportId) throws DaoException {
        Query query = getDslContext().selectFrom(REPORT).where(
                REPORT.ID.eq(reportId)
                        .and(REPORT.PARTY_ID.eq(partyId))
                        .and(REPORT.PARTY_SHOP_ID.eq(shopId))
        );
        return fetchOne(query, reportRowMapper);
    }

    @Override
    public List<FileInfo> getByReportIds(long reportId) throws DaoException {
        Condition condition = FILE_INFO.REPORT_ID.eq(reportId);
        Query query = getDslContext().selectFrom(FILE_INFO).where(condition);

        return fetch(query, fileInfoRecordRowMapper);
    }

    @Override
    public void changeReportStatus(long reportId, ReportStatus status) throws DaoException {
        Query query = getDslContext().update(REPORT)
                .set(REPORT.STATUS, status)
                .where(REPORT.ID.eq(reportId));

        executeOne(query);
    }

    @Override
    public Long attachFileInfo(long reportId, String fileDataId) throws DaoException {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setReportId(reportId);
        fileInfo.setFileDataId(fileDataId);

        FileInfoRecord record = getDslContext().newRecord(FILE_INFO, fileInfo);

        Query query = getDslContext().insertInto(FILE_INFO).set(record).returning(FILE_INFO.ID);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public List<Report> getPendingReports(int limit) throws DaoException {
        Query query = getDslContext().selectFrom(REPORT)
                .where(REPORT.STATUS.eq(ReportStatus.pending))
                .limit(limit)
                .forUpdate();

        return fetch(query, reportRowMapper);
    }

    @Override
    public List<Report> getPendingReportsByType(ReportType reportType) throws DaoException {
        Query query = getDslContext().selectFrom(REPORT)
                .where(REPORT.STATUS.eq(ReportStatus.pending))
                .and(REPORT.TYPE.eq(reportType))
                .forUpdate();

        return fetch(query, reportRowMapper);
    }

    @Override
    public List<Report> getReportsByRange(String partyId, String shopId, LocalDateTime fromTime, LocalDateTime toTime, List<ReportType> reportTypes) throws DaoException {
        Condition condition = REPORT.PARTY_ID.eq(partyId)
                .and(REPORT.PARTY_SHOP_ID.eq(shopId))
                .and(REPORT.FROM_TIME.ge(fromTime))
                .and(REPORT.TO_TIME.le(toTime));

        if (!reportTypes.isEmpty()) {
            condition = condition.and(REPORT.TYPE.in(reportTypes));
        }

        Query query = getDslContext().selectFrom(REPORT).where(condition);

        return fetch(query, reportRowMapper);
    }

    @Override
    public long createReport(String partyId, String shopId, LocalDateTime fromTime, LocalDateTime toTime, ReportType reportType, String timezone, LocalDateTime createdAt) throws DaoException {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        Query query = getDslContext().insertInto(REPORT)
                .set(REPORT.PARTY_ID, partyId)
                .set(REPORT.PARTY_SHOP_ID, shopId)
                .set(REPORT.FROM_TIME, fromTime)
                .set(REPORT.TO_TIME, toTime)
                .set(REPORT.TYPE, reportType)
                .set(REPORT.TIMEZONE, timezone)
                .set(REPORT.CREATED_AT, createdAt)
                .returning(REPORT.ID);

        execute(query, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
