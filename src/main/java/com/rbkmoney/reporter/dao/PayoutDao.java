package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.exception.DaoException;

import java.util.Optional;

public interface PayoutDao {

    Optional<Long> getLastEventId() throws DaoException;

    Long save(Payout payout) throws DaoException;

    Payout get(String payoutId) throws DaoException;

    void updateNotCurrent(String payoutId) throws DaoException;

}
