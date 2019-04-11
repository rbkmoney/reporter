package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static com.rbkmoney.geck.common.util.TypeUtil.stringToTemporal;
import static com.rbkmoney.geck.common.util.TypeUtil.toLocalDateTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServiceTests extends AbstractAppServiceTests {

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private StorageService storageService;

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
    public void saveFileTest() throws IOException {
        Path expectedFile = Files.createTempFile("reporter_", "_expected_file");
        Path actualFile = Files.createTempFile("reporter_", "_actual_file");

        try {
            Files.write(expectedFile, "4815162342".getBytes());
            FileMeta fileMeta = storageService.saveFile(expectedFile);
            URL url = storageService.getFileUrl(fileMeta.getFileId(), fileMeta.getBucketId(), LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC));
            assertNotNull(url);

            try (InputStream in = url.openStream()) {
                Files.copy(in, actualFile, StandardCopyOption.REPLACE_EXISTING);
            }
            assertEquals(Files.readAllLines(expectedFile), Files.readAllLines(actualFile));
            assertEquals(fileMeta.getMd5(), DigestUtils.md5Hex(Files.newInputStream(actualFile)));
            assertEquals(fileMeta.getSha256(), DigestUtils.sha256Hex(Files.newInputStream(actualFile)));
        } finally {
            Files.deleteIfExists(expectedFile);
            Files.deleteIfExists(actualFile);
        }
    }
}
