package com.rbkmoney.reporter.dao.impl;

import com.rbkmoney.dao.impl.AbstractGenericDao;
import com.rbkmoney.reporter.batch.key.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.dao.InvoiceDao;
import com.rbkmoney.reporter.dao.mapper.PartyDataRowMapper;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.exception.DaoException;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.rbkmoney.reporter.domain.tables.Invoice.INVOICE;

@Component
public class InvoiceDaoImpl extends AbstractGenericDao implements InvoiceDao {

    private final PartyDataRowMapper partyDataRowMapper;

    @Autowired
    public InvoiceDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
        partyDataRowMapper = new PartyDataRowMapper();
    }

    @Override
    public PartyData getPartyData(InvoiceUniqueBatchKeyImpl uniqueBatchKey) throws DaoException {
        Query query = getDslContext()
                .select(
                        INVOICE.PARTY_ID,
                        INVOICE.PARTY_SHOP_ID
                )
                .from(INVOICE)
                .where(INVOICE.INVOICE_ID.eq(uniqueBatchKey.getInvoiceId()));
        return fetchOne(query, partyDataRowMapper);
    }
}
