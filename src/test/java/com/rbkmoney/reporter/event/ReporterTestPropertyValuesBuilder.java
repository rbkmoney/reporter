package com.rbkmoney.reporter.event;

import com.rbkmoney.TestContainers;

import java.util.ArrayList;
import java.util.List;

import static com.rbkmoney.TestContainersConstants.*;

public class ReporterTestPropertyValuesBuilder {

    public static String[] build(TestContainers testContainers) {
        List<String> strings = new ArrayList<>();
        if (!testContainers.isDockerContainersEnable()) {
            withUsingTestContainers(testContainers, strings);
        } else {
            withoutUsingTestContainers(strings);
        }

        strings.add("storage.signingRegion=" + SIGNING_REGION);
        strings.add("storage.accessKey=" + AWS_ACCESS_KEY);
        strings.add("storage.secretKey=" + AWS_SECRET_KEY);
        strings.add("storage.clientProtocol=" + PROTOCOL);
        strings.add("storage.client.maxErrorRetry=" + MAX_ERROR_RETRY);
        strings.add("storage.bucketName=" + BUCKET_NAME);
        strings.add("kafka.processing.payment.enabled=false");
        strings.add("kafka.processing.payout.enabled=false");
        strings.add("bustermaze.payout.polling.enabled=false");
        strings.add("jobs.synchronization.enabled=false");
        strings.add("jobs.report.enabled=false");
        return strings.toArray(new String[0]);
    }

    private static void withUsingTestContainers(TestContainers testContainers, List<String> strings) {
        testContainers.getPostgresSQLTestContainer().ifPresent(
                c -> {
                    strings.add("spring.datasource.url=" + c.getJdbcUrl());
                    strings.add("spring.datasource.username=" + c.getUsername());
                    strings.add("spring.datasource.password=" + c.getPassword());
                    strings.add("flyway.url=" + c.getJdbcUrl());
                    strings.add("flyway.user=" + c.getUsername());
                    strings.add("flyway.password=" + c.getPassword());
                }
        );
        testContainers.getCephTestContainer().ifPresent(
                c -> strings.add("storage.endpoint=" + c.getContainerIpAddress() + ":" + c.getMappedPort(80))
        );
        testContainers.getFileStorageTestContainer().ifPresent(
                c -> {
                    strings.add("filestorage.url=http://" + c.getContainerIpAddress() + ":" + FILE_STORAGE_PORT + "/file_storage");
                }
        );
    }

    private static void withoutUsingTestContainers(List<String> strings) {
        // порты должны совпадать с портом из docker-compose-dev.yml
        strings.add("storage.endpoint=localhost:" + CEPH_PORT);
        strings.add("filestorage.url=http://localhost:" + FILE_STORAGE_PORT + "/file_storage");
    }
}
