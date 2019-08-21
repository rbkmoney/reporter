package com.rbkmoney.reporter.dao.mapper;

import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.tables.Invoice.INVOICE;

public class PartyDataRowMapper implements RowMapper<PartyData> {

    @Override
    public PartyData mapRow(ResultSet rs, int i) throws SQLException {
        PartyData partyData = new PartyData();
        partyData.setPartyId(UUID.fromString(rs.getString(INVOICE.PARTY_ID.getName())));
        partyData.setPartyShopId(rs.getString(INVOICE.PARTY_SHOP_ID.getName()));
        return partyData;
    }
}
