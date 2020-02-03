package com.rbkmoney.reporter.dao.mapper;

import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.domain.enums.PaymentTool;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.Tables.PAYMENT_COST;
import static com.rbkmoney.reporter.domain.Tables.PAYMENT_FEE;
import static com.rbkmoney.reporter.domain.tables.Invoice.INVOICE;
import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;
import static com.rbkmoney.reporter.domain.tables.PaymentState.PAYMENT_STATE;

public class PaymentRegistryReportDataRowMapper implements RowMapper<PaymentRegistryReportData> {

    @Override
    public PaymentRegistryReportData mapRow(ResultSet rs, int i) throws SQLException {
        PaymentRegistryReportData data = new PaymentRegistryReportData();
        data.setId(rs.getLong(PAYMENT.ID.getName()));
        data.setEventCreatedAt(rs.getObject(PAYMENT_STATE.EVENT_CREATED_AT.getName(), LocalDateTime.class));
        data.setPartyId(UUID.fromString(rs.getString(PAYMENT.PARTY_ID.getName())));
        data.setPartyShopId(rs.getString(PAYMENT.PARTY_SHOP_ID.getName()));
        data.setInvoiceId(rs.getString(PAYMENT.INVOICE_ID.getName()));
        data.setPaymentId(rs.getString(PAYMENT.PAYMENT_ID.getName()));
        data.setPaymentTool(TypeUtil.toEnumField(rs.getString(PAYMENT.TOOL.getName()), PaymentTool.class));
        data.setPaymentEmail(rs.getString(PAYMENT.EMAIL.getName()));
        data.setPaymentAmount(rs.getLong(PAYMENT_COST.AMOUNT.getName()));
        data.setPaymentFee(rs.getLong(PAYMENT_FEE.FEE.getName()));
        data.setPaymentProviderFee(rs.getLong(PAYMENT_FEE.PROVIDER_FEE.getName()));
        data.setPaymentExternalFee(rs.getLong(PAYMENT_FEE.EXTERNAL_FEE.getName()));
        data.setInvoiceProduct(rs.getString(INVOICE.PRODUCT.getName()));
        data.setCurrencySymbolicCode(rs.getString(PAYMENT_COST.CURRENCY_CODE.getName()));
        return data;
    }
}
