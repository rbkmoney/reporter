package com.rbkmoney.reporter.config;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.reporter.serialization.impl.MachineEventDeserializerImpl;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.LoggingErrorHandler;
import org.springframework.retry.support.RetryTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private static final String GROUP_ID = "ReporterListener";
    private static final String EARLIEST = "earliest";
    private static final String PKCS_12 = "PKCS12";

    @Value("${kafka.bootstrap.servers}")
    private String servers;

    @Value("${kafka.concurrency}")
    private int concurrency;

    @Value("${kafka.max-pool-records}")
    private String maxPoolRecords;

    @Value("${kafka.fetch-min-bytes}")
    private String fetchMinBytes;

    @Value("${kafka.fetch-max-wait-ms}")
    private String fetchMaxWaitMs;

    @Value("${kafka.ssl.enabled}")
    private boolean sslEnable;

    @Value("${kafka.ssl.truststore.location-config}")
    private String sslTruststoreLocationConfig;

    @Value("${kafka.ssl.truststore.password-config}")
    private String sslTruststorePasswordConfig;

    @Value("${kafka.ssl.keystore.location-config}")
    private String sslKeystoreLocationConfig;

    @Value("${kafka.ssl.keystore.password-config}")
    private String sslKeystorePasswordConfig;

    @Value("${kafka.ssl.key.password-config}")
    private String sslKeyPasswordConfig;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MachineEventDeserializerImpl.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPoolRecords);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinBytes);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMs);
        if (sslEnable) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name());
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, new File(sslTruststoreLocationConfig).getAbsolutePath());
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePasswordConfig);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, PKCS_12);
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, PKCS_12);
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, new File(sslKeystoreLocationConfig).getAbsolutePath());
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePasswordConfig);
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKeyPasswordConfig);
        }

        return props;
    }

    @Bean
    public ConsumerFactory<String, MachineEvent> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MachineEvent>> kafkaListenerContainerFactory(
            ConsumerFactory<String, MachineEvent> consumerFactory,
            RetryTemplate retryTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, MachineEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckOnError(false);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setErrorHandler(new LoggingErrorHandler());
        factory.setConcurrency(concurrency);
        factory.setRetryTemplate(retryTemplate);

        return factory;
    }
}
