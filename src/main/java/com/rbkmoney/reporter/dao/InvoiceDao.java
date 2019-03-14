package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.exception.DaoException;

import java.util.Optional;

public interface InvoiceDao {

    Optional<Long> getLastEventId() throws DaoException;

    Long save(Invoice invoice) throws DaoException;

    Invoice get(String invoiceId) throws DaoException;

    void updateNotCurrent(String invoiceId) throws DaoException;
}
