package com.rbkmoney.reporter.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Created by tolkonepiu on 12/07/2017.
 */
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ReportServiceTest {

    @Autowired
    ReportService reportService;

    @Test
    public void generateProvisionReportTest() {
        Instant fromTime = LocalDateTime.of(2016, 01, 01, 00, 00).toInstant(ZoneOffset.UTC);
        Instant toTime = LocalDateTime.of(2016, 12, 31, 11, 59).toInstant(ZoneOffset.UTC);
        System.out.println(reportService.generateProvisionReport("74480e4f-1a36-4edd-8175-7a9e984313b0", "1", fromTime, toTime));
    }

}
