package com.rbkmoney.reporter.dao.mapper;

import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
import com.rbkmoney.reporter.domain.enums.PaymentTool;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.rbkmoney.reporter.domain.tables.Invoice.INVOICE;
import static com.rbkmoney.reporter.domain.tables.Payment.PAYMENT;
import static com.rbkmoney.reporter.domain.tables.PaymentState.PAYMENT_STATE;
import static com.rbkmoney.reporter.domain.tables.Refund.REFUND;
import static com.rbkmoney.reporter.domain.tables.RefundState.REFUND_STATE;

public class RefundPaymentRegistryReportDataRowMapper implements RowMapper<RefundPaymentRegistryReportData> {

    @Override
    public RefundPaymentRegistryReportData mapRow(ResultSet rs, int i) throws SQLException {
        RefundPaymentRegistryReportData data = new RefundPaymentRegistryReportData();
        data.setId(rs.getLong(REFUND.ID.getName()));
        data.setRefundEventCreatedAt(rs.getObject(REFUND_STATE.EVENT_CREATED_AT.getName(), LocalDateTime.class));
        data.setPaymentEventCreatedAt(rs.getObject(PAYMENT_STATE.EVENT_CREATED_AT.getName(), LocalDateTime.class));
        data.setPartyId(UUID.fromString(rs.getString(REFUND.PARTY_ID.getName())));
        data.setPartyShopId(rs.getString(REFUND.PARTY_SHOP_ID.getName()));
        data.setInvoiceId(rs.getString(REFUND.INVOICE_ID.getName()));
        data.setPaymentId(rs.getString(REFUND.PAYMENT_ID.getName()));
        data.setPaymentTool(TypeUtil.toEnumField(rs.getString(PAYMENT.TOOL.getName()), PaymentTool.class));
        data.setPaymentEmail(rs.getString(PAYMENT.EMAIL.getName()));
        data.setRefundAmount(rs.getLong(REFUND.AMOUNT.getName()));
        data.setInvoiceProduct(rs.getString(INVOICE.PRODUCT.getName()));
        return data;
    }
}
