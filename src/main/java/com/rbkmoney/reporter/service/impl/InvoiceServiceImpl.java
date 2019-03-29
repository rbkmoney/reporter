package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.InvoiceDao;
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
@Slf4j
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDao invoiceDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Invoice invoice) throws StorageException {
        log.info("Trying to save invoice, invoice='{}'", invoice);
        try {
            Long id = invoiceDao.save(invoice);
            log.info("Invoice have been saved, invoice='{}'", invoice);
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
            log.info("Invoice have been got, invoice='{}'", invoice);
            return invoice;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get invoice, invoiceId='%s'", invoiceId), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateNotCurrent(String invoiceId) throws StorageException {
        log.info("Trying to update not current invoices, invoiceId='{}'", invoiceId);
        try {
            invoiceDao.updateNotCurrent(invoiceId);
            log.info("Not current invoices have been update, invoiceId='{}'", invoiceId);
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to update not current invoices, invoiceId='%s'", invoiceId), e);
        }
    }
}
