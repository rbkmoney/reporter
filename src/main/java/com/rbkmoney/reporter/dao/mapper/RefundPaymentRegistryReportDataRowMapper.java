package com.rbkmoney.reporter.dao.mapper;

import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
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
import static com.rbkmoney.reporter.domain.tables.Refund.REFUND;

public class RefundPaymentRegistryReportDataRowMapper implements RowMapper<RefundPaymentRegistryReportData> {

    public static final Field<Long> refundAmount = DSL.field("refund_amount", Long.class);

    @Override
    public RefundPaymentRegistryReportData mapRow(ResultSet rs, int i) throws SQLException {
        RefundPaymentRegistryReportData data = new RefundPaymentRegistryReportData();
        data.setId(rs.getLong(REFUND.ID.getName()));
        data.setEventId(rs.getLong(REFUND.EVENT_ID.getName()));
        data.setRefundEventCreatedAt(rs.getObject(REFUND.EVENT_CREATED_AT.getName(), LocalDateTime.class));
        data.setPaymentEventCreatedAt(rs.getObject(PAYMENT.EVENT_CREATED_AT.getName(), LocalDateTime.class));
        data.setEventType(TypeUtil.toEnumField(rs.getString(REFUND.EVENT_TYPE.getName()), InvoiceEventType.class));
        data.setPartyId(UUID.fromString(rs.getString(REFUND.PARTY_ID.getName())));
        data.setPartyShopId(rs.getString(REFUND.PARTY_SHOP_ID.getName()));
        data.setInvoiceId(rs.getString(REFUND.INVOICE_ID.getName()));
        data.setPaymentId(rs.getString(REFUND.PAYMENT_ID.getName()));
        data.setPaymentTool(TypeUtil.toEnumField(rs.getString(PAYMENT.PAYMENT_TOOL.getName()), PaymentTool.class));
        data.setPaymentEmail(rs.getString(PAYMENT.PAYMENT_EMAIL.getName()));
        data.setRefundAmount(rs.getLong(refundAmount.getName()));
        data.setInvoiceProduct(rs.getString(INVOICE.INVOICE_PRODUCT.getName()));
        return data;
    }
}
