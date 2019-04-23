package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.file.storage.FileStorageSrv;
import com.rbkmoney.file.storage.NewFileResult;
import com.rbkmoney.reporter.config.properties.FileStorageProperties;
import com.rbkmoney.reporter.exception.FileStorageException;
import com.rbkmoney.reporter.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final FileStorageProperties fileStorageProperties;
    private final FileStorageSrv.Iface fileStorageClient;
    private final HttpClient httpClient;

    @Override
    public String saveFile(Path file) throws FileStorageException {
        log.info("Trying to create new empty file in storage");
        String fileName = file.getFileName().toString();
        NewFileResult result = createFileInFileStorage(fileName);

        log.info("Trying to upload report file to storage");
        String uploadUrl = result.getUploadUrl();
        HttpPut requestPut = new HttpPut(uploadUrl);
        try {
            requestPut.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        requestPut.setEntity(new FileEntity(file.toFile()));

        HttpResponse response = getHttpResponse(result, requestPut);

        checkResponse(result.getFileDataId(), response);

        log.info("Report file has been successfully uploaded, fileDataId={}", result.getFileDataId());
        return result.getFileDataId();
    }

    private NewFileResult createFileInFileStorage(String fileName) {
        NewFileResult result;
        try {
            result = fileStorageClient.createNewFile(Collections.emptyMap(), getTime().toString());
        } catch (TException e) {
            throw new FileStorageException(
                    String.format(
                            "Failed to create new file from file storage, file name='%s'",
                            fileName
                    ),
                    e
            );
        }
        return result;
    }

    private Instant getTime() {
        return LocalDateTime.now(fileStorageProperties.getTimeZone())
                .plusMinutes(fileStorageProperties.getUrlLifeTimeDuration())
                .toInstant(ZoneOffset.UTC);
    }

    private HttpResponse getHttpResponse(NewFileResult result, HttpPut requestPut) {
        try {
            return httpClient.execute(requestPut);
        } catch (IOException e) {
            throw new FileStorageException(
                    String.format(
                            "Failed to upload report file by http request, fileDataId='%s'",
                            result.getFileDataId()
                    )
            );
        }
    }

    private void checkResponse(String fileDataId, HttpResponse response) {
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new FileStorageException(
                    String.format(
                            "Failed to upload report file by http response, fileDataId='%s', response='%s'",
                            fileDataId,
                            response.toString()
                    )
            );
        }
    }
}
