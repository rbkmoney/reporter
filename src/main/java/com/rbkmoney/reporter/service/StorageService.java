package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.pojos.File;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

public interface StorageService {

    String getFileUrl(String keyName, String bucketName, Instant expiresIn);

    File saveFile(String keyName, String bucketName, String filename, InputStream inputStream) throws IOException;

}
