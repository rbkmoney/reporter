package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
import com.rbkmoney.reporter.exception.StorageException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReportingService {

    Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, LocalDateTime toTime) throws StorageException;

    Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, LocalDateTime fromTime, LocalDateTime toTime) throws StorageException;

    List<PaymentRegistryReportData> getPaymentRegistryReportData(String partyId, String shopId, LocalDateTime fromTime, LocalDateTime toTime) throws StorageException;

    List<RefundPaymentRegistryReportData> getRefundPaymentRegistryReportData(String partyId, String shopId, LocalDateTime fromTime, LocalDateTime toTime) throws StorageException;

}
