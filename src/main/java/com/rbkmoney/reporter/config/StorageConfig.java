package com.rbkmoney.reporter.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.endpoint}")
    String endpoint;

    @Value("${storage.signingRegion}")
    String signingRegion;

    @Bean
    public AmazonS3 storageClient(ClientConfiguration clientConfiguration) {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.setEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(endpoint, signingRegion)
        );
        builder.setPathStyleAccessEnabled(true);
        builder.setClientConfiguration(clientConfiguration);
        return builder.build();
    }

    @Bean
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProtocol(Protocol.HTTP);
        return clientConfiguration;
    }

    @Bean
    public TransferManager transferManager(AmazonS3 s3Client) {
        return TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
    }

}
