package com.rbkmoney.reporter.config;

import com.rbkmoney.reporter.converter.SourceEventParser;
import com.rbkmoney.reporter.handler.EventStockEventHandler;
import com.rbkmoney.reporter.listener.MessageListener;
import com.rbkmoney.reporter.listener.impl.ProcessingEventsMessageListenerImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.processing.topic.enabled", havingValue = "true")
    public MessageListener processingEventsKafkaListener(SourceEventParser sourceEventParser,
                                                         EventStockEventHandler handler) {
        return new ProcessingEventsMessageListenerImpl(sourceEventParser, handler);
    }
}
