package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;
import com.rbkmoney.reporter.exception.FileNotFoundException;
import com.rbkmoney.reporter.exception.FileStorageException;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;

public interface StorageService {

    String getFileUrl(String fileId, String bucketId, Instant expiresIn) throws FileNotFoundException, FileStorageException;

    FileMeta saveFile(Path file) throws FileStorageException;

    FileMeta saveFile(String filename, byte[] bytes) throws FileStorageException;

    FileMeta saveFile(String filename, InputStream inputStream) throws FileStorageException;

}
