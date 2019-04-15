package com.rbkmoney.reporter.listener;

import com.rbkmoney.eventstock.client.DefaultSubscriberConfig;
import com.rbkmoney.eventstock.client.EventConstraint;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.SubscriberConfig;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.reporter.config.properties.BustermazeProperties;
import com.rbkmoney.reporter.service.ProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TempBustermazeEventPublisherOnStart implements ApplicationListener<ApplicationReadyEvent> {

    private final EventPublisher tempBustermazeEventPublisher;

    private final ProcessingService processingService;

    private final BustermazeProperties bustermazeProperties;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (bustermazeProperties.isEnable()) {
            tempBustermazeEventPublisher.subscribe(buildSubscriberConfig(processingService.getLastEventId()));
        }
    }

    private SubscriberConfig buildSubscriberConfig(Optional<Long> lastEventIdOptional) {
        EventConstraint.EventIDRange eventIDRange = new EventConstraint.EventIDRange();
        lastEventIdOptional.ifPresent(eventIDRange::setFromExclusive);
        EventFlowFilter eventFlowFilter = new EventFlowFilter(new EventConstraint(eventIDRange));
        return new DefaultSubscriberConfig(eventFlowFilter);
    }
}