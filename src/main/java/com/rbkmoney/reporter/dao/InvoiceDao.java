package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.exception.DaoException;

public interface InvoiceDao {

    Long save(Invoice invoice) throws DaoException;

    Invoice get(String invoiceId) throws DaoException;

    void updateNotCurrent(String invoiceId) throws DaoException;
}
