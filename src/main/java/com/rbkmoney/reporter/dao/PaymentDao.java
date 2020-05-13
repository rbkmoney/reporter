package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.batch.key.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.exception.DaoException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PaymentDao {

    PaymentPartyData getPaymentPartyData(PaymentInvoiceUniqueBatchKey uniqueBatchKey) throws DaoException;

    Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws DaoException;

    List<PaymentRegistryReportData> getPaymentRegistryReportData(String partyId, String shopId, LocalDateTime fromTime, LocalDateTime toTime) throws DaoException;

}
