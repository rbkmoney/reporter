package com.rbkmoney.reporter.config;

import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.reporter.config.properties.BustermazeProperties;
import com.rbkmoney.reporter.handler.EventStockClientHandler;
import com.rbkmoney.reporter.handler.EventStockHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class EventStockConfig {

    @Bean
    public EventPublisher eventPublisher(EventStockHandler eventStockHandler,
                                         BustermazeProperties properties) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(properties.getUrl().getURI())
                .withHousekeeperTimeout(properties.getHousekeeperTimeout())
                .withEventHandler(eventStockHandler)
                .withMaxPoolSize(properties.getMaxPoolSize())
                .withEventRetryDelay(properties.getDelay())
                .withPollDelay(properties.getDelay())
                .build();
    }

    @Bean
    public EventPublisher tempBustermazeEventPublisher(EventStockClientHandler eventStockHandler,
                                                       BustermazeProperties properties) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(properties.getUrl().getURI())
                .withHousekeeperTimeout(properties.getHousekeeperTimeout())
                .withEventHandler(eventStockHandler)
                .withMaxPoolSize(properties.getMaxPoolSize())
                .withEventRetryDelay(properties.getDelay())
                .withPollDelay(properties.getDelay())
                .build();
    }
}
