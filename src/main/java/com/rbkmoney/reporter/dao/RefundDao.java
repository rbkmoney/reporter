package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.exception.DaoException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RefundDao {

    Long save(Refund refund) throws DaoException;

    Refund get(String invoiceId, String paymentId, String refundId) throws DaoException;

    Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException;

    List<RefundPaymentRegistryReportData> getRefundPaymentRegistryReportData(String partyId, String partyShopId, LocalDateTime fromTime, LocalDateTime toTime) throws DaoException;

}
