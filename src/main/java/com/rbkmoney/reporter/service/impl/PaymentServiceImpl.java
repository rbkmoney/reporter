package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao paymentDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Payment payment) throws StorageException {
        log.info("Trying to save payment, payment='{}'", payment);
        try {
            Long id = paymentDao.save(payment);
            log.info("Payment have been saved, payment='{}'", payment);
            return id;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to save payment, payment='%s'", payment), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Payment get(String invoiceId, String paymentId) throws StorageException, NotFoundException {
        log.info("Trying to get payment, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
        try {
            Payment payment = paymentDao.get(invoiceId, paymentId);
            if (payment == null) {
                throw new NotFoundException(String.format("Payment not found, invoiceId='%s', paymentId='%s'", invoiceId, paymentId));
            }
            log.info("Payment have been got, payment='{}'", payment);
            return payment;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get payment, invoiceId='%s', paymentId='%s'", invoiceId, paymentId), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateNotCurrent(String invoiceId, String paymentId) throws StorageException {
        log.info("Trying to update not current payments, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
        try {
            paymentDao.updateNotCurrent(invoiceId, paymentId);
            log.info("Not current payments have been update, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to update not current payments, invoiceId='%s', paymentId='%s'", invoiceId, paymentId), e);
        }
    }
}
