package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.exception.FileStorageException;

import java.nio.file.Path;

public interface FileStorageService {

    String saveFile(Path file) throws FileStorageException;

}
