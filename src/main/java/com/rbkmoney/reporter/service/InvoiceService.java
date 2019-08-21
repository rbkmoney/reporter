package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;

public interface InvoiceService {

    Long save(Invoice invoice) throws StorageException;

    Invoice get(String invoiceId) throws StorageException, NotFoundException;

    PartyData getPartyData(String invoiceId) throws StorageException, NotFoundException;

}
