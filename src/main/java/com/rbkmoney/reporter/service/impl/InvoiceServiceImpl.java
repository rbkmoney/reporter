package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.reporter.dao.AdjustmentDao;
import com.rbkmoney.reporter.dao.InvoiceDao;
import com.rbkmoney.reporter.dao.PaymentDao;
import com.rbkmoney.reporter.dao.RefundDao;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDao invoiceDao;
    private final AdjustmentDao adjustmentDao;
    private final PaymentDao paymentDao;
    private final RefundDao refundDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Optional<Long> getCommonLastEventId() {
        try {
            log.info("Trying to get last common invoice event id");
            List<Long> lastEventIds = new ArrayList<>();
            invoiceDao.getLastEventId().ifPresent(lastEventIds::add);
            adjustmentDao.getLastEventId().ifPresent(lastEventIds::add);
            paymentDao.getLastEventId().ifPresent(lastEventIds::add);
            refundDao.getLastEventId().ifPresent(lastEventIds::add);
            Optional<Long> eventId = lastEventIds.stream()
                    .max(Comparator.comparing(aLong -> aLong));
            log.info("Last common invoice event id, eventId='{}'", eventId.orElse(null));
            return eventId;
        } catch (DaoException e) {
            throw new StorageException("Failed to get last common event id", e);
        }
    }

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
