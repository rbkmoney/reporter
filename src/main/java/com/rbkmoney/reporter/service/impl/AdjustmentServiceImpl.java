package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.AdjustmentDao;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.AdjustmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdjustmentServiceImpl implements AdjustmentService {

    private final AdjustmentDao adjustmentDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Adjustment adjustment) throws StorageException {
        log.info("Trying to save adjustment, adjustment='{}'", adjustment);
        try {
            Long id = adjustmentDao.save(adjustment);
            log.info("Adjustment have been saved, adjustment='{}'", adjustment);
            return id;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to save adjustment, adjustment='%s'", adjustment), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Adjustment get(String invoiceId, String paymentId, String adjustmentId) throws StorageException, NotFoundException {
        log.info("Trying to get adjustment, invoiceId='{}', paymentId='{}', adjustmentId='{}'", invoiceId, paymentId, adjustmentId);
        try {
            Adjustment adjustment = adjustmentDao.get(invoiceId, paymentId, adjustmentId);
            if (adjustment == null) {
                throw new NotFoundException(String.format("Adjustment not found, invoiceId='%s', paymentId='%s', adjustmentId='%s'", invoiceId, paymentId, adjustmentId));
            }
            log.info("Adjustment have been got, adjustment='{}'", adjustment);
            return adjustment;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get adjustment, invoiceId='%s', paymentId='%s', adjustmentId='%s'", invoiceId, paymentId, adjustmentId), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateNotCurrent(String invoiceId, String paymentId, String adjustmentId) throws StorageException {
        log.info("Trying to update not current adjustments, invoiceId='{}', paymentId='{}', adjustmentId='{}'", invoiceId, paymentId, adjustmentId);
        try {
            adjustmentDao.updateNotCurrent(invoiceId, paymentId, adjustmentId);
            log.info("Not current adjustments have been update, invoiceId='{}', paymentId='{}', adjustmentId='{}'", invoiceId, paymentId, adjustmentId);
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to update not current adjustments, invoiceId='%s', paymentId='%s', adjustmentId='%s'", invoiceId, paymentId, adjustmentId), e);
        }
    }
}
