package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;

public interface RefundService {

    Long save(Refund refund) throws StorageException;

    Refund get(String invoiceId, String paymentId, String refundId) throws StorageException, NotFoundException;

    void updateNotCurrent(String invoiceId, String paymentId, String refundId) throws StorageException;
}
