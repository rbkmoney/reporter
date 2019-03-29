package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.RefundDao;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundDao refundDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Refund refund) throws StorageException {
        log.info("Trying to save refund, refund='{}'", refund);
        try {
            Long id = refundDao.save(refund);
            log.info("Refund have been saved, refund='{}'", refund);
            return id;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to save refund, refund='%s'", refund), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Refund get(String invoiceId, String paymentId, String refundId) throws StorageException, NotFoundException {
        log.info("Trying to get refund, invoiceId='{}', paymentId='{}', refundId='{}'", invoiceId, paymentId, refundId);
        try {
            Refund refund = refundDao.get(invoiceId, paymentId, refundId);
            if (refund == null) {
                throw new NotFoundException(String.format("Refund not found, invoiceId='%s', paymentId='%s', refundId='%s'", invoiceId, paymentId, refundId));
            }
            log.info("Refund have been got, refund='{}'", refund);
            return refund;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get refund, invoiceId='%s', paymentId='%s', refundId='%s'", invoiceId, paymentId, refundId), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateNotCurrent(String invoiceId, String paymentId, String refundId) throws StorageException {
        log.info("Trying to update not current refunds, invoiceId='{}', paymentId='{}', refundId='{}'", invoiceId, paymentId, refundId);
        try {
            refundDao.updateNotCurrent(invoiceId, paymentId, refundId);
            log.info("Not current refunds have been update, invoiceId='{}', paymentId='{}', refundId='{}'", invoiceId, paymentId, refundId);
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to update not current refunds, invoiceId='%s', paymentId='%s', refundId='%s'", invoiceId, paymentId, refundId), e);
        }
    }
}
