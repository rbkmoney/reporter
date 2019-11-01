package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;

public interface PaymentService {

    void savePaymentPartyData(PaymentInvoiceUniqueBatchKey uniqueBatchKey, PaymentPartyData paymentPartyData);

    PaymentPartyData getPaymentPartyData(PaymentInvoiceUniqueBatchKey uniqueBatchKey) throws StorageException, NotFoundException;

}
