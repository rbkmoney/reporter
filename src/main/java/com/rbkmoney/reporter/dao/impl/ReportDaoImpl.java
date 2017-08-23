package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.dao.ReportDao;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import com.rbkmoney.reporter.domain.tables.records.FileMetaRecord;
import com.rbkmoney.reporter.domain.tables.records.ReportRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.rbkmoney.reporter.domain.tables.FileMeta.FILE_META;
import static com.rbkmoney.reporter.domain.tables.Report.REPORT;

@Component
public class ReportDaoImpl implements ReportDao {

    private final DSLContext dslContext;

    @Autowired
    public ReportDaoImpl(DataSource dataSource) {
        Configuration configuration = new DefaultConfiguration();
        configuration.set(SQLDialect.POSTGRES_9_5);
        configuration.set(dataSource);
        this.dslContext = DSL.using(configuration);
    }

    @Override
    public DSLContext getDSLContext() {
        return dslContext;
    }

    @Override
    public Report getReport(String partyId, String shopId, long reportId) {
        ReportRecord reportRecord = dslContext.selectFrom(REPORT).where(
                REPORT.ID.eq(reportId)
                        .and(REPORT.PARTY_ID.eq(partyId))
                        .and(REPORT.PARTY_SHOP_ID.eq(shopId))
        ).fetchOne();
        if (Objects.nonNull(reportRecord)) {
            return reportRecord.into(Report.class);
        }
        return null;
    }

    @Override
    public List<FileMeta> getReportFiles(long reportId) {
        return dslContext.selectFrom(FILE_META)
                .where(
                        FILE_META.REPORT_ID.eq(reportId)
                ).fetch().into(FileMeta.class);
    }

    @Override
    public void changeReportStatus(long reportId, ReportStatus status) {
        //TODO affected rows
        int affectedRow = dslContext.update(REPORT)
                .set(REPORT.STATUS, status)
                .where(REPORT.ID.eq(reportId))
                .execute();
    }

    @Override
    public FileMeta getFile(String fileId) {
        FileMetaRecord fileMetaRecord = dslContext
                .selectFrom(FILE_META)
                .where(FILE_META.FILE_ID.eq(fileId))
                .fetchOne();
        if (Objects.nonNull(fileMetaRecord)) {
            return fileMetaRecord.into(FileMeta.class);
        }
        return null;
    }

    @Override
    public String attachFile(long reportId, FileMeta file) {
        Record record = dslContext.insertInto(FILE_META)
                .set(FILE_META.FILE_ID, file.getFileId())
                .set(FILE_META.REPORT_ID, reportId)
                .set(FILE_META.BUCKET_ID, file.getBucketId())
                .set(FILE_META.FILENAME, file.getFilename())
                .set(FILE_META.MD5, file.getMd5())
                .set(FILE_META.SHA256, file.getSha256())
                .returning(FILE_META.FILE_ID)
                .fetchOne();

        return record.get(FILE_META.FILE_ID);
    }

    @Override
    public List<Report> getPendingReportsByType(ReportType reportType) {
        return dslContext.selectFrom(REPORT)
                .where(REPORT.STATUS.eq(ReportStatus.pending))
                .and(REPORT.TYPE.eq(reportType.name()))
                .fetch().into(Report.class);
    }

    @Override
    public List<Report> getReportsByRange(String partyId, String shopId, List<ReportType> reportTypes, LocalDateTime fromTime, LocalDateTime toTime) {
        Condition conditon = REPORT.PARTY_ID.eq(partyId)
                .and(REPORT.PARTY_SHOP_ID.eq(shopId))
                .and(REPORT.CREATED_AT.ge(fromTime))
                .and(REPORT.CREATED_AT.lt(toTime));

        if (!reportTypes.isEmpty()) {
            conditon = conditon.and(REPORT.TYPE.in(reportTypes));
        }

        return dslContext.selectFrom(REPORT).where(conditon).fetch().into(Report.class);
    }

    @Override
    public long createReport(String partyId, String shopId, LocalDateTime fromTime, LocalDateTime toTime, ReportType reportType, String timezone, LocalDateTime createdAt) {
        Record record = dslContext.insertInto(REPORT)
                .set(REPORT.PARTY_ID, partyId)
                .set(REPORT.PARTY_SHOP_ID, shopId)
                .set(REPORT.FROM_TIME, fromTime)
                .set(REPORT.TO_TIME, toTime)
                .set(REPORT.TYPE, reportType.name())
                .set(REPORT.TIMEZONE, timezone)
                .set(REPORT.CREATED_AT, createdAt)
                .returning(REPORT.ID).fetchOne();
        return record.get(REPORT.ID);
    }
}
