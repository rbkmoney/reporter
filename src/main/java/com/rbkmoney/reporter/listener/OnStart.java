package com.rbkmoney.reporter.listener;

import com.rbkmoney.eventstock.client.DefaultSubscriberConfig;
import com.rbkmoney.eventstock.client.EventConstraint;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.reporter.config.properties.BustermazeProperties;
import com.rbkmoney.reporter.dao.ContractMetaDao;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OnStart implements ApplicationListener<ApplicationReadyEvent> {

    private final EventPublisher eventPublisher;

    private final ContractMetaDao contractMetaDao;

    private final BustermazeProperties bustermazeProperties;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (bustermazeProperties.isEnable()) {
            EventConstraint.EventIDRange eventIDRange = new EventConstraint.EventIDRange();

            try {
                Optional.ofNullable(contractMetaDao.getLastEventId())
                        .ifPresent(eventIDRange::setFromExclusive);
            } catch (DaoException ex) {
                throw new StorageException("failed to get last event id from storage", ex);
            }

            eventPublisher.subscribe(
                    new DefaultSubscriberConfig<>(new EventFlowFilter(new EventConstraint(eventIDRange)))
            );
        }
    }
}
