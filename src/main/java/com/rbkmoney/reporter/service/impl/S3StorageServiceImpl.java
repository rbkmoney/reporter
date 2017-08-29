package com.rbkmoney.reporter.service.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.rbkmoney.reporter.domain.tables.pojos.FileMeta;
import com.rbkmoney.reporter.exception.FileNotFoundException;
import com.rbkmoney.reporter.exception.FileStorageException;
import com.rbkmoney.reporter.service.StorageService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class S3StorageServiceImpl implements StorageService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TransferManager transferManager;
    private final AmazonS3 storageClient;
    private final String bucketName;

    @Autowired
    public S3StorageServiceImpl(TransferManager transferManager, @Value("${storage.bucketName}") String bucketName) {
        this.transferManager = transferManager;
        this.storageClient = transferManager.getAmazonS3Client();
        this.bucketName = bucketName;
    }

    @PostConstruct
    public void init() {
        if (!storageClient.doesBucketExist(bucketName)) {
            log.info("Create bucket in file storage, bucketId='{}'", bucketName);
            storageClient.createBucket(bucketName);
        }
    }

    @Override
    public String getFileUrl(String fileId, String bucketId, Instant expiresIn) throws FileStorageException, FileNotFoundException {
        try {
            log.debug("Trying to generate presigned url, fileId='{}', bucketId='{}', expiresAt='{}'", fileId, bucketId, expiresIn);
            URL url = storageClient.generatePresignedUrl(bucketId, fileId, Date.from(expiresIn));
            if (Objects.isNull(url)) {
                throw new FileNotFoundException("Presigned url is null, fileId='%s', bucketId='%s'", fileId, bucketId);
            }
            return url.toString();
        } catch (AmazonClientException ex) {
            throw new FileStorageException("Failed to get presigned url from storage, fileId='%s', bucketId='%s'", ex, fileId, bucketId);
        }
    }

    @Override
    public FileMeta saveFile(Path file) throws FileStorageException {
        try (InputStream inputStream = Files.newInputStream(file)) {
            return saveFile(file.getFileName().toString(), inputStream);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to save path in storage, filename='%s', bucketId='%s'", ex, file.getFileName().toString(), bucketName);
        }
    }

    @Override
    public FileMeta saveFile(String filename, byte[] bytes) throws FileStorageException {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            return saveFile(filename, inputStream);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to save byte array in storage, filename='%s', bucketId='%s'", ex, filename, bucketName);
        }
    }

    @Override
    public FileMeta saveFile(String filename, InputStream inputStream) throws FileStorageException {
        log.debug("Trying to upload file to storage, filename='{}', bucketId='{}'", filename, bucketName);

        try {
            String fileId;
            do {
                fileId = UUID.randomUUID().toString();
            } while (storageClient.doesObjectExist(bucketName, fileId));

            FileMeta fileMeta = createFileMeta(
                    fileId,
                    bucketName,
                    filename,
                    DigestUtils.md5Hex(inputStream),
                    DigestUtils.sha256Hex(inputStream)
            );

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentDisposition("attachment;filename=" + filename);
            Upload upload = transferManager.upload(
                    new PutObjectRequest(bucketName, fileId, inputStream, objectMetadata)
            );
            try {
                upload.waitForUploadResult();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("File have been successfully uploaded, fileId='{}', bucketId='{}', filename='{}', md5='{}', sha256='{}'",
                    fileMeta.getFileId(), fileMeta.getBucketId(), fileMeta.getFilename(), fileMeta.getMd5(), fileMeta.getSha256());

            return fileMeta;

        } catch (IOException | AmazonClientException ex) {
            throw new FileStorageException("Failed to upload file to storage, filename='%s', bucketId='%s'", ex, filename, bucketName);
        }
    }

    @PreDestroy
    public void terminate() {
        transferManager.shutdownNow(true);
    }

    private FileMeta createFileMeta(String fileId, String bucketId, String filename, String md5, String sha256) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setFileId(fileId);
        fileMeta.setBucketId(bucketId);
        fileMeta.setFilename(filename);
        fileMeta.setMd5(md5);
        fileMeta.setSha256(sha256);
        return fileMeta;
    }
}