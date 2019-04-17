package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.exception.DaoException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public interface PayoutDao {

    Optional<Long> getLastEventId() throws DaoException;

    Long save(Payout payout) throws DaoException;

    Payout get(String payoutId) throws DaoException;

    Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException;

    void updateNotCurrent(String payoutId) throws DaoException;

}
