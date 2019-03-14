package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.exception.DaoException;

import java.util.Optional;

public interface PaymentDao {

    Optional<Long> getLastEventId() throws DaoException;

    Long save(Payment payment) throws DaoException;

    Payment get(String invoiceId, String paymentId) throws DaoException;

    void updateNotCurrent(String invoiceId, String paymentId) throws DaoException;

}
