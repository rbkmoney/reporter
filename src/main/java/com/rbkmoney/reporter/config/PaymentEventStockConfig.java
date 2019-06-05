package com.rbkmoney.reporter.config;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.reporter.config.properties.BustermazePaymentProperties;
import com.rbkmoney.reporter.listener.stockevent.StockEventHandlerClientImpl;
import com.rbkmoney.sink.common.handle.stockevent.StockEventHandler;
import com.rbkmoney.sink.common.handle.stockevent.event.PaymentEventHandler;
import com.rbkmoney.sink.common.handle.stockevent.event.change.PartyChangeEventHandler;
import com.rbkmoney.sink.common.handle.stockevent.event.change.claimeffect.ClaimEffectEventHandler;
import com.rbkmoney.sink.common.handle.stockevent.event.change.impl.ClaimEffectStatusAcceptedChangeStockEventHandler;
import com.rbkmoney.sink.common.handle.stockevent.event.impl.PartyChangePaymentStockEventHandler;
import com.rbkmoney.sink.common.handle.stockevent.impl.PaymentEventStockEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class PaymentEventStockConfig {

    @Bean
    public PartyChangeEventHandler claimEffectStatusAcceptedChangeStockEventHandler(List<ClaimEffectEventHandler> eventsHandlers) {
        return new ClaimEffectStatusAcceptedChangeStockEventHandler(eventsHandlers);
    }

    @Bean
    public PartyChangePaymentStockEventHandler partyChangePaymentStockEventHandler(List<PartyChangeEventHandler> partyChangeEventHandlers) {
        return new PartyChangePaymentStockEventHandler(partyChangeEventHandlers);
    }

    @Bean
    public StockEventHandler<StockEvent> paymentEventStockEventHandler(List<PaymentEventHandler> eventHandlers) {
        return new PaymentEventStockEventHandler(eventHandlers);
    }

    @Bean
    public EventHandler<StockEvent> paymentStockEventHandler(StockEventHandler<StockEvent> paymentEventStockEventHandler) {
        return new StockEventHandlerClientImpl(paymentEventStockEventHandler);
    }

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
}
