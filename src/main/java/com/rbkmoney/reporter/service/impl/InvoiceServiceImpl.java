package com.rbkmoney.reporter.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.rbkmoney.reporter.batch.impl.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.dao.InvoiceDao;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDao invoiceDao;
    private final Cache<InvoiceUniqueBatchKeyImpl, PartyData> partyDataCache;

    @Override
    public void savePartyData(InvoiceUniqueBatchKeyImpl uniqueBatchKey, PartyData partyData) {
        partyDataCache.put(uniqueBatchKey, partyData);
    }

    @Transactional
    @Override
    public PartyData getPartyData(InvoiceUniqueBatchKeyImpl uniqueBatchKey) throws StorageException, NotFoundException {
        String invoiceId = uniqueBatchKey.getInvoiceId();

        log.info("Trying to get PartyData, invoiceId='{}'", invoiceId);

        PartyData partyData = partyDataCache.get(
                uniqueBatchKey,
                k -> {
                    try {
                        PartyData data = invoiceDao.getPartyData(uniqueBatchKey);
                        if (data == null) {
                            throw new NotFoundException(String.format("PartyData not found, invoiceId='%s'", invoiceId));
                        }
                        return data;
                    } catch (DaoException e) {
                        throw new StorageException(String.format("Failed to get PartyData, invoiceId='%s'", invoiceId), e);
                    }
                }
        );

        log.info("PartyData has been got, invoiceId='{}'", invoiceId);
        return partyData;
    }
}
