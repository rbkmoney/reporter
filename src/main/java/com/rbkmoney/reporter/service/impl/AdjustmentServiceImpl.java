package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.AdjustmentDao;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.AdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdjustmentServiceImpl implements AdjustmentService {

    private final AdjustmentDao adjustmentDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Adjustment adjustment) throws StorageException {
        try {
            return adjustmentDao.save(adjustment);
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to save adjustment, adjustment='%s'", adjustment), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Adjustment get(String invoiceId, String paymentId, String adjustmentId) throws StorageException, NotFoundException {
        try {
            Adjustment adjustment = adjustmentDao.get(invoiceId, paymentId, adjustmentId);
            if (adjustment == null) {
                throw new NotFoundException(String.format("Adjustment not found, invoiceId='%s', paymentId='%s', adjustmentId='%s'", invoiceId, paymentId, adjustmentId));
            }
            return adjustment;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get adjustment, invoiceId='%s', paymentId='%s', adjustmentId='%s'", invoiceId, paymentId, adjustmentId), e);
        }
    }
}
