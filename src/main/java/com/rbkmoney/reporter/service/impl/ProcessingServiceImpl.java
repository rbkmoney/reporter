package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.*;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.ProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessingServiceImpl implements ProcessingService {

    private final InvoiceDao invoiceDao;
    private final AdjustmentDao adjustmentDao;
    private final PaymentDao paymentDao;
    private final RefundDao refundDao;
    private final PayoutDao payoutDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Optional<Long> getLastEventId() {
        try {
            log.info("Trying to get last processing event id");
            List<Long> lastEventIds = new ArrayList<>();
            invoiceDao.getLastEventId().ifPresent(lastEventIds::add);
            adjustmentDao.getLastEventId().ifPresent(lastEventIds::add);
            paymentDao.getLastEventId().ifPresent(lastEventIds::add);
            refundDao.getLastEventId().ifPresent(lastEventIds::add);
            payoutDao.getLastEventId().ifPresent(lastEventIds::add);
            Optional<Long> eventId = lastEventIds.stream()
                    .max(Comparator.comparing(aLong -> aLong));
            log.info("Last processing event id, eventId='{}'", eventId.orElse(null));
            return eventId;
        } catch (DaoException e) {
            throw new StorageException("Failed to get last processing event id", e);
        }
    }
}
