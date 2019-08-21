package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
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
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao paymentDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Payment payment) throws StorageException {
        String invoiceId = payment.getInvoiceId();
        String paymentId = payment.getPaymentId();

        log.info("Trying to save payment, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
        try {
            Long id = paymentDao.save(payment);
            if (id != null) {
                log.info("Payment has been saved, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
            } else {
                log.info("Payment is duplicate, id is null, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
            }
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
            log.info("Payment has been got, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
            return payment;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get payment, invoiceId='%s', paymentId='%s'", invoiceId, paymentId), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public PaymentPartyData getPaymentPartyData(String invoiceId, String paymentId) throws StorageException, NotFoundException {
        log.info("Trying to get PaymentPartyData, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
        try {
            PaymentPartyData paymentPartyData = paymentDao.getPaymentPartyData(invoiceId, paymentId);
            if (paymentPartyData == null) {
                throw new NotFoundException(String.format("PaymentPartyData not found, invoiceId='%s', paymentId='%s'", invoiceId, paymentId));
            }
            log.info("PaymentPartyData has been got, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
            return paymentPartyData;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get PaymentPartyData, invoiceId='%s', paymentId='%s'", invoiceId, paymentId), e);
        }
    }
}
