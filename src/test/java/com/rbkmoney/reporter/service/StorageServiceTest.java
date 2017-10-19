package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.AbstractIntegrationTest;
import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertNotNull;

public class StorageServiceTest extends AbstractIntegrationTest {

    @Autowired
    StorageService storageService;

    @Test
    public void saveFileTest() throws IOException {
        FileMeta fileMeta = storageService.saveFile(Files.createTempFile("kek_", "_kek"));
        //TODO localstack cant work with it
//        URL url = storageService.getFileUrl(fileMeta.getFileId(), fileMeta.getBucketId(), LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC));
//        assertNotNull(url);
    }

}
