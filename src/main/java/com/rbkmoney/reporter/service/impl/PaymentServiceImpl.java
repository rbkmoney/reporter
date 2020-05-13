package com.rbkmoney.reporter.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.rbkmoney.reporter.batch.key.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao paymentDao;
    private final Cache<PaymentInvoiceUniqueBatchKey, PaymentPartyData> paymentPartyDataCache;

    @Override
    public void savePaymentPartyData(PaymentInvoiceUniqueBatchKey uniqueBatchKey, PaymentPartyData paymentPartyData) {
        paymentPartyDataCache.put(uniqueBatchKey, paymentPartyData);
    }

    @Transactional
    @Override
    public PaymentPartyData getPaymentPartyData(PaymentInvoiceUniqueBatchKey uniqueBatchKey) throws StorageException, NotFoundException {
        String invoiceId = uniqueBatchKey.getInvoiceId();
        String paymentId = uniqueBatchKey.getPaymentId();

        log.info("Trying to get PaymentPartyData, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);

        PaymentPartyData paymentPartyData = paymentPartyDataCache.get(
                uniqueBatchKey,
                k -> {
                    try {
                        PaymentPartyData data = paymentDao.getPaymentPartyData(uniqueBatchKey);
                        if (data == null) {
                            throw new NotFoundException(String.format("PaymentPartyData not found, invoiceId='%s', paymentId='%s'", invoiceId, paymentId));
                        }
                        return data;
                    } catch (DaoException e) {
                        throw new StorageException(String.format("Failed to get PaymentPartyData, invoiceId='%s', paymentId='%s'", invoiceId, paymentId), e);
                    }
                }
        );

        log.info("PaymentPartyData has been got, invoiceId='{}', paymentId='{}'", invoiceId, paymentId);
        return paymentPartyData;
    }
}
