package com.rbkmoney.reporter.config;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.reporter.config.properties.BustermazePaymentProperties;
import com.rbkmoney.reporter.config.properties.BustermazePayoutProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class EventPublisherConfig {

    @Bean
    public EventPublisher<StockEvent> paymentEventPublisher(EventHandler<StockEvent> paymentStockEventHandler,
                                                            BustermazePaymentProperties properties) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(properties.getUrl().getURI())
                .withHousekeeperTimeout(properties.getHousekeeperTimeout())
                .withEventHandler(paymentStockEventHandler)
                .withMaxPoolSize(properties.getMaxPoolSize())
                .withMaxQuerySize(properties.getMaxQuerySize())
                .withPollDelay(properties.getDelay())
                .withEventRetryDelay(properties.getRetryDelay())
                .build();
    }

    @Bean
    public EventPublisher<StockEvent> payoutEventPublisher(EventHandler<StockEvent> payoutStockEventHandler,
                                                           BustermazePayoutProperties properties) throws IOException {
        return new PollingEventPublisherBuilder()
                .withURI(properties.getUrl().getURI())
                .withHousekeeperTimeout(properties.getHousekeeperTimeout())
                .withEventHandler(payoutStockEventHandler)
                .withMaxPoolSize(properties.getMaxPoolSize())
                .withMaxQuerySize(properties.getMaxQuerySize())
                .withPollDelay(properties.getDelay())
                .withEventRetryDelay(properties.getRetryDelay())
                .build();
    }
}
