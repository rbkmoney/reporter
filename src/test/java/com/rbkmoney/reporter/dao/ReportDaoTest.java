package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.AbstractIntegrationTest;
import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.File;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ReportDaoTest extends AbstractIntegrationTest {

    @Autowired
    ReportDao reportDao;

    @Test
    public void insertAndGetReportTest() {
        String partyId = random(String.class);
        String shopId = random(String.class);
        LocalDateTime fromTime = random(LocalDateTime.class);
        LocalDateTime toTime = random(LocalDateTime.class);
        ReportType reportType = random(ReportType.class);
        String timezone = random(TimeZone.class).getID();
        LocalDateTime createdAt = random(LocalDateTime.class);

        long reportId = reportDao.createReport(partyId, shopId, fromTime, toTime, reportType, timezone, createdAt);
        assertNull(reportDao.getReport("is", "null", reportId));

        Report report = reportDao.getReport(partyId, shopId, reportId);
        assertEquals(reportId, report.getId().longValue());
        assertEquals(partyId, report.getPartyId());
        assertEquals(shopId, report.getPartyShopId());
        assertEquals(fromTime, report.getFromTime());
        assertEquals(toTime, report.getToTime());
        assertEquals(reportType.name(), report.getType());
        assertEquals(timezone, report.getTimezone());
        assertEquals(createdAt, report.getCreatedAt());

        assertEquals(1, reportDao.getReportsByRange(partyId, shopId, new ArrayList<>(), createdAt.minusDays(1), createdAt.plusDays(1)).size());

        assertEquals(1, reportDao.getReportsByRange(partyId, shopId, Arrays.asList(reportType), createdAt.minusDays(1), createdAt.plusDays(1)).size());
    }

    @Test
    public void attachFileTest() {
        File file = random(File.class);
        Long reportId = random(Long.class);

        String fileId = reportDao.attachFile(reportId, file);
        File currentFile = reportDao.getFile(fileId);

        assertEquals(file.getId(), currentFile.getId());
        assertEquals(reportId, currentFile.getReportId());
        assertEquals(file.getBucketId(), currentFile.getBucketId());
        assertEquals(file.getFilename(), currentFile.getFilename());
        assertEquals(file.getMd5(), currentFile.getMd5());
        assertEquals(file.getSha256(), currentFile.getSha256());

        List<File> files = reportDao.getReportFiles(reportId);
        assertEquals(1, files.size());

        currentFile = files.get(0);

        assertEquals(file.getId(), currentFile.getId());
        assertEquals(reportId, currentFile.getReportId());
        assertEquals(file.getBucketId(), currentFile.getBucketId());
        assertEquals(file.getFilename(), currentFile.getFilename());
        assertEquals(file.getMd5(), currentFile.getMd5());
        assertEquals(file.getSha256(), currentFile.getSha256());
    }

}