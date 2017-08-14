package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.domain.tables.pojos.File;
import com.rbkmoney.reporter.service.StorageService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

@Component
//@Profile("test")
public class MockStorageService implements StorageService {
    @Override
    public String getFileUrl(String keyName, String bucketName, Instant expiresIn) {
        return "https://www.youtube.com/watch?v=c4wHJqqud3U";
    }

    @Override
    public File saveFile(String keyName, String bucketName, String filename, InputStream inputStream) throws IOException {
        File file = new File();
        file.setId(keyName);
        file.setBucketId(bucketName);
        file.setFilename(filename);

        //TODO it doesn't work)
        file.setMd5(DigestUtils.md5Hex(inputStream));
        file.setSha256(DigestUtils.sha256Hex(inputStream));

        return file;
    }
}
