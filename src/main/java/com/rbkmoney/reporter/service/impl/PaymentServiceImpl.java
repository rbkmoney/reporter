package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao paymentDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Payment payment) throws StorageException {
        try {
            return paymentDao.save(payment);
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to save payment, payment='%s'", payment), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Payment get(String invoiceId, String paymentId) throws StorageException, NotFoundException {
        try {
            Payment payment = paymentDao.get(invoiceId, paymentId);
            if (payment == null) {
                throw new NotFoundException(String.format("Payment not found, invoiceId='%s', paymentId='%s'", invoiceId, paymentId));
            }
            return payment;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get payment, invoiceId='%s', paymentId='%s'", invoiceId, paymentId), e);
        }
    }
}
