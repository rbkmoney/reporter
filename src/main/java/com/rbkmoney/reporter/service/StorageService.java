package com.rbkmoney.reporter.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.Date;

/**
 * Created by tolkonepiu on 12/07/2017.
 */
@Service
public class StorageService {

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private TransferManager transferManager;

    @Value("${storage.bucketName}")
    private String bucketName;

    public Upload saveFile(String key, File file) {
        return transferManager.upload(
                new PutObjectRequest(bucketName, key, file)
        );
    }

    public URL getFileUrl(String key, Date expiration) {
        return s3Client.generatePresignedUrl(bucketName, key, expiration);
    }


}
