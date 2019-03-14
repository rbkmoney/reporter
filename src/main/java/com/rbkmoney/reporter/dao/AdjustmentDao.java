package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.exception.DaoException;

import java.util.Optional;

public interface AdjustmentDao {

    Optional<Long> getLastEventId() throws DaoException;

    Long save(Adjustment adjustment) throws DaoException;

    Adjustment get(String invoiceId, String paymentId, String adjustmentId) throws DaoException;

    void updateNotCurrent(String invoiceId, String paymentId, String adjustmentId) throws DaoException;
}
