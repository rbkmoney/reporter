package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.dao.ReportDao;
import com.rbkmoney.reporter.domain.tables.pojos.File;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static com.rbkmoney.reporter.domain.tables.File.FILE;
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
    public Report getReport(String partyId, String shopId, long reportId) {
        return dslContext.selectFrom(REPORT).where(
                REPORT.ID.eq(reportId)
                        .and(REPORT.PARTY_ID.eq(partyId))
                        .and(REPORT.PARTY_SHOP_ID.eq(shopId))
        ).fetchOne().into(Report.class);
    }

    @Override
    public List<File> getReportFiles(long reportId) {
        return dslContext.selectFrom(FILE)
                .where(
                        FILE.REPORT_ID.eq(reportId)
                ).fetch().into(File.class);
    }

    @Override
    public List<Report> getReportsByRange(String partyId, String shopId, List<ReportType> reportTypes, LocalDateTime fromTime, LocalDateTime toTime) {
        return dslContext.selectFrom(REPORT).where(
                REPORT.PARTY_ID.eq(partyId)
                        .and(REPORT.PARTY_SHOP_ID.eq(shopId))
                        .and(REPORT.TYPE.in(reportTypes))
                        .and(REPORT.CREATED_AT.ge(fromTime))
                        .and(REPORT.CREATED_AT.lt(toTime))
        ).fetch().into(Report.class);
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
                .returning(REPORT.ID).fetchOne();
        return record.get(REPORT.ID);
    }
}
