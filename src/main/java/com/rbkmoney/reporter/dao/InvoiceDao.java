package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.batch.key.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.exception.DaoException;

public interface InvoiceDao {

    PartyData getPartyData(InvoiceUniqueBatchKeyImpl uniqueBatchKey) throws DaoException;

}
