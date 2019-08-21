package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.PayoutDao;
import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutServiceImpl implements PayoutService {

    private final PayoutDao payoutDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Payout payout) throws StorageException {
        log.info("Trying to save payout, payoutId='{}'", payout.getPayoutId());
        try {
            Long id = payoutDao.save(payout);
            if (id != null) {
                log.info("Payout has been saved, payoutId='{}'", payout.getPayoutId());
            } else {
                log.info("Payout is duplicate, id is null, payoutId='{}'", payout.getPayoutId());
            }
            return id;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to save payout, payout='%s'", payout), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Payout get(String payoutId) throws StorageException, NotFoundException {
        log.info("Trying to get payout, payoutId='{}'", payoutId);
        try {
            Payout payout = payoutDao.get(payoutId);
            if (payout == null) {
                throw new NotFoundException(String.format("Payout not found, payoutId='%s'", payoutId));
            }
            log.info("Payout has been got, payoutId='{}'", payoutId);
            return payout;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get payout, payoutId='%s'", payoutId), e);
        }
    }
}
