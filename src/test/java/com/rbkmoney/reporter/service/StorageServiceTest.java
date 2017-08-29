package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.AbstractIntegrationTest;
import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;

public class StorageServiceTest extends AbstractIntegrationTest {

    @Autowired
    StorageService storageService;

    @Test
    public void saveFileTest() throws IOException {
        FileMeta fileMeta = storageService.saveFile(Files.createTempFile("kek_", "_kek"));
        //TODO get file url
    }

}
