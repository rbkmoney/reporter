package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.exception.DaoException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public interface AdjustmentDao {

    Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException;

}
