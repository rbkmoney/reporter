package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.batch.key.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;

public interface InvoiceService {

    void savePartyData(InvoiceUniqueBatchKeyImpl uniqueBatchKey, PartyData partyData);

    PartyData getPartyData(InvoiceUniqueBatchKeyImpl uniqueBatchKey) throws StorageException, NotFoundException;

}
