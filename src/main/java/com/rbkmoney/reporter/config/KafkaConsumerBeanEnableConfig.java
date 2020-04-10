package com.rbkmoney.reporter.config;

import com.rbkmoney.reporter.listener.PartyManagementListener;
import com.rbkmoney.reporter.service.PartyManagementService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "info.single-instance-mode", havingValue = "false")
    public PartyManagementListener partyManagementListener(PartyManagementService partyManagementService) {
        return new PartyManagementListener(partyManagementService);
    }
}
