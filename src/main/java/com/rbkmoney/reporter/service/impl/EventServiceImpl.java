package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.PayoutDao;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final PayoutDao payoutDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Optional<Long> getPayoutLastEventId() {
        try {
            log.info("Trying to get last payout event id");
            Optional<Long> eventId = payoutDao.getLastEventId();
            log.info("Last payout event id, eventId='{}'", eventId.orElse(null));
            return eventId;
        } catch (DaoException e) {
            throw new StorageException("Failed to get last payout event id", e);
        }
    }
}
