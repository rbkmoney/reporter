package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;

public interface PaymentService {

    Long save(Payment payment) throws StorageException;

    Payment get(String invoiceId, String paymentId) throws StorageException, NotFoundException;

    void updateNotCurrent(String invoiceId, String paymentId) throws StorageException;
}