package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.AbstractIntegrationTest;
import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
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

        assertEquals(reportDao.getReportsByRange(partyId, shopId, Arrays.asList(reportType), createdAt.minusDays(1), createdAt.plusDays(1)).size(), 1);
    }

}
