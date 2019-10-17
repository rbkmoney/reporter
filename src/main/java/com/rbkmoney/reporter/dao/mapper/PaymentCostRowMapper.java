package com.rbkmoney.reporter.dao.mapper;

import com.rbkmoney.reporter.domain.tables.pojos.PaymentCost;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static com.rbkmoney.reporter.domain.Tables.PAYMENT_COST;

public class PaymentCostRowMapper implements RowMapper<PaymentCost> {

    @Override
    public PaymentCost mapRow(ResultSet rs, int i) throws SQLException {
        PaymentCost paymentCost = new PaymentCost();
        paymentCost.setId(rs.getLong(PAYMENT_COST.ID.getName()));
        paymentCost.setInvoiceId(rs.getString(PAYMENT_COST.INVOICE_ID.getName()));
        paymentCost.setSequenceId(rs.getLong(PAYMENT_COST.SEQUENCE_ID.getName()));
        paymentCost.setChangeId(rs.getInt(PAYMENT_COST.CHANGE_ID.getName()));
        paymentCost.setPaymentId(rs.getString(PAYMENT_COST.PAYMENT_ID.getName()));
        paymentCost.setCreatedAt(rs.getObject(PAYMENT_COST.CREATED_AT.getName(), LocalDateTime.class));
        paymentCost.setAmount(rs.getLong(PAYMENT_COST.AMOUNT.getName()));
        paymentCost.setOriginAmount(rs.getLong(PAYMENT_COST.ORIGIN_AMOUNT.getName()));
        paymentCost.setCurrency(rs.getString(PAYMENT_COST.CURRENCY.getName()));
        return paymentCost;
    }
}
