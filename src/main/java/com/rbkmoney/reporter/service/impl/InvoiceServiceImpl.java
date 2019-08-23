package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.InvoiceDao;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDao invoiceDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Invoice invoice) throws StorageException {
        log.info("Trying to save invoice, invoiceId='{}'", invoice.getInvoiceId());
        try {
            Long id = invoiceDao.save(invoice);
            if (id != null) {
                log.info("Invoice has been saved, invoiceId='{}'", invoice.getInvoiceId());
            } else {
                log.info("Invoice is duplicate, id is null, invoiceId='{}'", invoice.getInvoiceId());
            }
            return id;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to save invoice, invoice='%s'", invoice), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Invoice get(String invoiceId) throws StorageException, NotFoundException {
        log.info("Trying to get invoice, invoiceId='{}'", invoiceId);
        try {
            Invoice invoice = invoiceDao.get(invoiceId);
            if (invoice == null) {
                throw new NotFoundException(String.format("Invoice not found, invoiceId='%s'", invoiceId));
            }
            log.info("Invoice has been got, invoiceId='{}'", invoiceId);
            return invoice;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get invoice, invoiceId='%s'", invoiceId), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public PartyData getPartyData(String invoiceId) throws StorageException, NotFoundException {
        log.info("Trying to get PartyData, invoiceId='{}'", invoiceId);
        try {
            PartyData partyData = invoiceDao.getPartyData(invoiceId);
            if (partyData == null) {
                throw new NotFoundException(String.format("PartyData not found, invoiceId='%s'", invoiceId));
            }
            log.info("PartyData has been got, invoiceId='{}'", invoiceId);
            return partyData;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get PartyData, invoiceId='%s'", invoiceId), e);
        }
    }
}
