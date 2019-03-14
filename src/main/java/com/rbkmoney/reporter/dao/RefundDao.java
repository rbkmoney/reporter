package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.exception.DaoException;

import java.util.Optional;

public interface RefundDao {

    Optional<Long> getLastEventId() throws DaoException;

    Long save(Refund refund) throws DaoException;

    Refund get(String invoiceId, String paymentId, String refundId) throws DaoException;

    void updateNotCurrent(String invoiceId, String paymentId, String refundId) throws DaoException;

}
