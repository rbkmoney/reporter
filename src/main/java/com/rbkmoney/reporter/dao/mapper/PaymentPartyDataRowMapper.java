package com.rbkmoney.reporter.dao.mapper;

import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.Tables.PAYMENT_COST;
import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;

public class PaymentPartyDataRowMapper implements RowMapper<PaymentPartyData> {

    @Override
    public PaymentPartyData mapRow(ResultSet rs, int i) throws SQLException {
        PaymentPartyData paymentPartyData = new PaymentPartyData();
        paymentPartyData.setPartyId(UUID.fromString(rs.getString(PAYMENT.PARTY_ID.getName())));
        paymentPartyData.setPartyShopId(rs.getString(PAYMENT.PARTY_SHOP_ID.getName()));
        paymentPartyData.setPaymentAmount(rs.getLong(PAYMENT_COST.AMOUNT.getName()));
        paymentPartyData.setPaymentCurrencyCode(rs.getString(PAYMENT_COST.CURRENCY_CODE.getName()));
        return paymentPartyData;
    }
}
