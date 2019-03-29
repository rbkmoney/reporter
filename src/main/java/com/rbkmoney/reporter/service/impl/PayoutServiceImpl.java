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
@Slf4j
@RequiredArgsConstructor
public class PayoutServiceImpl implements PayoutService {

    private final PayoutDao payoutDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Payout payout) throws StorageException {
        log.info("Trying to save payout, payout='{}'", payout);
        try {
            Long id = payoutDao.save(payout);
            log.info("Payout have been saved, payout='{}'", payout);
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
            log.info("Payout have been got, payout='{}'", payout);
            return payout;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get payout, payoutId='%s'", payoutId), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateNotCurrent(String payoutId) throws StorageException {
        log.info("Trying to update not current payouts, payoutId='{}'", payoutId);
        try {
            payoutDao.updateNotCurrent(payoutId);
            log.info("Not current payouts have been update, payoutId='{}'", payoutId);
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to update not current payouts, payoutId='%s'", payoutId), e);
        }
    }
}
