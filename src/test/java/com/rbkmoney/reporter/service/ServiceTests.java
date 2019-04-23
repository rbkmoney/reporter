package com.rbkmoney.reporter.service;

import com.rbkmoney.file.storage.FileStorageSrv;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.tables.pojos.Report;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.thrift.TException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.geck.common.util.TypeUtil.stringToTemporal;
import static com.rbkmoney.geck.common.util.TypeUtil.toLocalDateTime;
import static java.nio.file.Files.newInputStream;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;

public class ServiceTests extends AbstractAppServiceTests {

    private String partyId = generateString();
    private String shopId = generateString();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private FileStorageSrv.Iface client;

    @Test
    @Sql("classpath:data/sql/shop_accounting_full_data.sql")
    public void reportingServiceTest() {
        Map<String, Long> data = reportingService.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );
        assertEquals((long) 3000, (long) data.get("funds_acquired"));
        assertEquals((long) 75, (long) data.get("fee_charged"));
        assertEquals((long) 2, (long) data.get("funds_adjusted"));
        assertEquals((long) 1000, (long) data.get("funds_refunded"));
        assertEquals((long) 950, (long) data.get("funds_paid_out"));
    }

    @Test
    public void reportServiceTest() {
        jdbcTemplate.execute("truncate table rpt.report cascade");

        List<Long> reportIds = range(0, 5)
                .mapToLong(i -> createReport("provision_of_service"))
                .boxed()
                .collect(toList());
        reportIds.add(createReport("payment_registry"));

        assertEquals(
                5,
                reportService.getReportsByRange(
                        partyId,
                        shopId,
                        fromTime.toInstant(ZoneOffset.UTC),
                        toTime.toInstant(ZoneOffset.UTC),
                        singletonList("provision_of_service")
                )
                        .size()
        );

        Long reportId = reportIds.get(0);
        Report report = reportService.getReport(partyId, shopId, reportId);
        assertEquals(toTime, report.getToTime());

        reportService.cancelReport(partyId, shopId, reportId);

        assertEquals(
                4,
                reportService.getReportsByRangeNotCancelled(
                        partyId,
                        shopId,
                        fromTime.toInstant(ZoneOffset.UTC),
                        toTime.toInstant(ZoneOffset.UTC),
                        singletonList("provision_of_service")
                )
                        .size()
        );

        reportService.changeReportStatus(report, ReportStatus.created);

        assertEquals(
                5,
                reportService.getReportsByRangeNotCancelled(
                        partyId,
                        shopId,
                        fromTime.toInstant(ZoneOffset.UTC),
                        toTime.toInstant(ZoneOffset.UTC),
                        singletonList("provision_of_service")
                )
                        .size()
        );

        assertEquals(1, reportService.getPendingReports().size());
    }

    @Test
    public void fileStorageServiceTest() throws URISyntaxException, IOException, TException, InterruptedException {
        Path file = getFileFromResources();
        String fileDataId = fileStorageService.saveFile(file);
        String downloadUrl = client.generateDownloadUrl(fileDataId, generateCurrentTimePlusDay().toString());

        HttpResponse responseGet = httpClient.execute(new HttpGet(downloadUrl));
        InputStream content = responseGet.getEntity().getContent();
        assertEquals(getContent(newInputStream(file)), getContent(content));
    }

    private long createReport(String reportType) {
        return reportService.createReport(
                partyId,
                shopId,
                fromTime.toInstant(ZoneOffset.UTC),
                toTime.toInstant(ZoneOffset.UTC),
                reportType
        );
    }

    private Path getFileFromResources() throws URISyntaxException {
        ClassLoader classLoader = this.getClass().getClassLoader();

        URL url = requireNonNull(classLoader.getResource("respect1"));
        return Paths.get(url.toURI());
    }
}
