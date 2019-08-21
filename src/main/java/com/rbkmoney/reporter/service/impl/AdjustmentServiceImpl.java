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
@RequiredArgsConstructor
@Slf4j
public class AdjustmentServiceImpl implements AdjustmentService {

    private final AdjustmentDao adjustmentDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Adjustment adjustment) throws StorageException {
        String invoiceId = adjustment.getInvoiceId();
        String paymentId = adjustment.getPaymentId();
        String adjustmentId = adjustment.getAdjustmentId();

        log.info("Trying to save adjustment, invoiceId='{}', paymentId='{}', adjustmentId='{}'", invoiceId, paymentId, adjustmentId);
        try {
            Long id = adjustmentDao.save(adjustment);
            if (id != null) {
                log.info("Adjustment has been saved, invoiceId='{}', paymentId='{}', adjustmentId='{}'", invoiceId, paymentId, adjustmentId);
            } else {
                log.info("Adjustment is duplicate, id is null, invoiceId='{}', paymentId='{}', adjustmentId='{}'", invoiceId, paymentId, adjustmentId);
            }
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
            log.info("Adjustment has been got, invoiceId='{}', paymentId='{}', adjustmentId='{}'", invoiceId, paymentId, adjustmentId);
            return adjustment;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get adjustment, invoiceId='%s', paymentId='%s', adjustmentId='%s'", invoiceId, paymentId, adjustmentId), e);
        }
    }
}
