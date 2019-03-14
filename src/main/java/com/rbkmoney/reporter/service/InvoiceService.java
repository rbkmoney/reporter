package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;

import java.util.Optional;

public interface InvoiceService {

    Optional<Long> getCommonLastEventId();

    Long save(Invoice invoice) throws StorageException;

    Invoice get(String invoiceId) throws StorageException, NotFoundException;

    void updateNotCurrent(String invoiceId) throws StorageException;
}
