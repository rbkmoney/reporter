package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.InvoiceDao;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.StorageException;
import com.rbkmoney.reporter.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDao invoiceDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Long save(Invoice invoice) throws StorageException {
        try {
            return invoiceDao.save(invoice);
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to save invoice, invoice='%s'", invoice), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Invoice get(String invoiceId) throws StorageException, NotFoundException {
        try {
            Invoice invoice = invoiceDao.get(invoiceId);
            if (invoice == null) {
                throw new NotFoundException(String.format("Invoice not found, invoiceId='%s'", invoiceId));
            }
            return invoice;
        } catch (DaoException e) {
            throw new StorageException(String.format("Failed to get invoice, invoiceId='%s'", invoiceId), e);
        }
    }
}
