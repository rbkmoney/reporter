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
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService {

    private final RefundDao refundDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Refund refund) throws StorageException {
        String invoiceId = refund.getInvoiceId();
        String paymentId = refund.getPaymentId();
        String refundId = refund.getRefundId();

        log.info("Trying to save refund, invoiceId='{}', paymentId='{}', refundId='{}'", invoiceId, paymentId, refundId);
        try {
            Long id = refundDao.save(refund);
            if (id != null) {
                log.info("Refund has been saved, invoiceId='{}', paymentId='{}', refundId='{}'", invoiceId, paymentId, refundId);
            } else {
                log.info("Refund is duplicate, id is null, invoiceId='{}', paymentId='{}', refundId='{}'", invoiceId, paymentId, refundId);
            }
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
            log.info("Refund has been got, refund='{}'", refund);
            return refund;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get refund, invoiceId='%s', paymentId='%s', refundId='%s'", invoiceId, paymentId, refundId), e);
        }
    }
}
