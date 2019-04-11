package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.AdjustmentDao;
import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.dao.PayoutDao;
import com.rbkmoney.reporter.dao.RefundDao;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.ReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

    private final AdjustmentDao adjustmentDao;
    private final PaymentDao paymentDao;
    private final RefundDao refundDao;
    private final PayoutDao payoutDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, LocalDateTime toTime) throws StorageException {
        return getShopAccountingReportData(partyId, partyShopId, currencyCode, Optional.empty(), toTime);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, LocalDateTime fromTime, LocalDateTime toTime) throws StorageException {
        return getShopAccountingReportData(partyId, partyShopId, currencyCode, Optional.of(fromTime), toTime);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public List<PaymentRegistryReportData> getPaymentRegistryReportData(String partyId, String partyShopId, LocalDateTime fromTime, LocalDateTime toTime) throws StorageException {
        log.info("Trying to get payment data for payment registry report, partyId='{}', partyShopId='{}', fromTime='{}', toTime='{}'", partyId, partyShopId, fromTime, toTime);
        try {
            List<PaymentRegistryReportData> data = paymentDao.getPaymentRegistryReportData(partyId, partyShopId, fromTime, toTime);
            log.info("Payment data for payment registry report has been got, partyId='{}', partyShopId='{}', fromTime='{}', toTime='{}'", partyId, partyShopId, fromTime, toTime);
            return data;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get payment data for payment registry report, partyId='%s', partyShopId='%s', fromTime='%s', toTime='%s'", partyId, partyShopId, fromTime, toTime), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public List<RefundPaymentRegistryReportData> getRefundPaymentRegistryReportData(String partyId, String partyShopId, LocalDateTime fromTime, LocalDateTime toTime) throws StorageException {
        log.info("Trying to get refund data for payment registry report, partyId='{}', partyShopId='{}', fromTime='{}', toTime='{}'", partyId, partyShopId, fromTime, toTime);
        try {
            List<RefundPaymentRegistryReportData> data = refundDao.getRefundPaymentRegistryReportData(partyId, partyShopId, fromTime, toTime);
            log.info("Refund data for payment registry report has been got, partyId='{}', partyShopId='{}', fromTime='{}', toTime='{}'", partyId, partyShopId, fromTime, toTime);
            return data;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get refund data for payment registry report, partyId='%s', partyShopId='%s', fromTime='%s', toTime='%s'", partyId, partyShopId, fromTime, toTime), e);
        }
    }

    private Map<String, Long> getShopAccountingReportData(String partyId, String partyShopId, String currencyCode, Optional<LocalDateTime> fromTime, LocalDateTime toTime) throws StorageException {
        log.info("Trying to get data for shop accounting report, partyId='{}', partyShopId='{}', currencyCode='{}', fromTime='{}', toTime='{}'", partyId, partyShopId, currencyCode, fromTime, toTime);
        try {
            Map<String, Long> data = new HashMap<>();
            data.putAll(adjustmentDao.getShopAccountingReportData(partyId, partyShopId, currencyCode, fromTime, toTime));
            data.putAll(paymentDao.getShopAccountingReportData(partyId, partyShopId, currencyCode, fromTime, toTime));
            data.putAll(refundDao.getShopAccountingReportData(partyId, partyShopId, currencyCode, fromTime, toTime));
            data.putAll(payoutDao.getShopAccountingReportData(partyId, partyShopId, currencyCode, fromTime, toTime));
            log.info("Data for shop accounting report has been got, partyId='{}', partyShopId='{}', currencyCode='{}', fromTime='{}', toTime='{}'", partyId, partyShopId, currencyCode, fromTime, toTime);
            return data;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get data for shop accounting report, partyId='%s', partyShopId='%s', currencyCode='%s', fromTime='%s', toTime='%s'", partyId, partyShopId, currencyCode, fromTime, toTime), e);
        }
    }
}
