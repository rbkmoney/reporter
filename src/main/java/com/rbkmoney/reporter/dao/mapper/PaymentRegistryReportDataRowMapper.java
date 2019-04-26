package com.rbkmoney.reporter.dao.mapper;

import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.PaymentTool;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.tables.Invoice.INVOICE;
import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;

public class PaymentRegistryReportDataRowMapper implements RowMapper<PaymentRegistryReportData> {

    public static final Field<Long> defaultValue = DSL.field("0", Long.class);
    public static final Field<Long> paymentAmount = DSL.field("payment_amount", Long.class);
    public static final Field<Long> paymentFee = DSL.field("payment_fee", Long.class);
    public static final Field<Long> paymentProviderFee = DSL.field("payment_provider_fee", Long.class);
    public static final Field<Long> paymentExternalFee = DSL.field("payment_external_fee", Long.class);

    @Override
    public PaymentRegistryReportData mapRow(ResultSet rs, int i) throws SQLException {
        PaymentRegistryReportData data = new PaymentRegistryReportData();
        data.setId(rs.getLong(PAYMENT.ID.getName()));
        data.setEventCreatedAt(rs.getObject(PAYMENT.EVENT_CREATED_AT.getName(), LocalDateTime.class));
        data.setEventType(TypeUtil.toEnumField(rs.getString(PAYMENT.EVENT_TYPE.getName()), InvoiceEventType.class));
        data.setPartyId(UUID.fromString(rs.getString(PAYMENT.PARTY_ID.getName())));
        data.setPartyShopId(rs.getString(PAYMENT.PARTY_SHOP_ID.getName()));
        data.setInvoiceId(rs.getString(PAYMENT.INVOICE_ID.getName()));
        data.setPaymentId(rs.getString(PAYMENT.PAYMENT_ID.getName()));
        data.setPaymentTool(TypeUtil.toEnumField(rs.getString(PAYMENT.PAYMENT_TOOL.getName()), PaymentTool.class));
        data.setPaymentEmail(rs.getString(PAYMENT.PAYMENT_EMAIL.getName()));
        data.setPaymentAmount(rs.getLong(paymentAmount.getName()));
        data.setPaymentFee(rs.getLong(paymentFee.getName()));
        data.setPaymentExternalFee(rs.getLong(paymentExternalFee.getName()));
        data.setPaymentProviderFee(rs.getLong(paymentProviderFee.getName()));
        data.setInvoiceProduct(rs.getString(INVOICE.INVOICE_PRODUCT.getName()));
        return data;
    }
}
