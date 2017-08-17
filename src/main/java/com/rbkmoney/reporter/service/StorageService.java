package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

public interface StorageService {

    String getFileUrl(String keyName, String bucketName, Instant expiresIn);

    FileMeta saveFile(String keyName, String bucketName, String filename, InputStream inputStream) throws IOException;

}
