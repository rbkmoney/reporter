package com.rbkmoney.reporter.util;

import com.rbkmoney.damsel.domain.PaymentRoute;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.reporter.batch.impl.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.batch.impl.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.domain.enums.InvoicePaymentStatus;
import com.rbkmoney.reporter.domain.tables.pojos.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapperUtil {

    public static PartyData getPartyData(Invoice invoice) {
        return new PartyData(invoice.getPartyId(), invoice.getPartyShopId());
    }

    public static PaymentPartyData getPaymentPartyData(PaymentCost paymentCost, Payment payment) {
        return new PaymentPartyData(payment.getPartyId(), payment.getPartyShopId(), paymentCost.getAmount(), paymentCost.getCurrencyCode());
    }

    public static InvoiceUniqueBatchKeyImpl getInvoiceUniqueBatchKeyImpl(Payment payment) {
        return new InvoiceUniqueBatchKeyImpl(payment.getInvoiceId());
    }

    public static InvoiceUniqueBatchKeyImpl getInvoiceUniqueBatchKeyImpl(Invoice invoice) {
        return new InvoiceUniqueBatchKeyImpl(invoice.getInvoiceId());
    }

    public static PaymentInvoiceUniqueBatchKey getPaymentInvoiceUniqueBatchKey(Payment payment) {
        return new PaymentInvoiceUniqueBatchKey(payment.getInvoiceId(), payment.getPaymentId());
    }

    public static PaymentInvoiceUniqueBatchKey getPaymentInvoiceUniqueBatchKey(Adjustment adjustment) {
        return new PaymentInvoiceUniqueBatchKey(adjustment.getInvoiceId(), adjustment.getPaymentId());
    }

    public static PaymentInvoiceUniqueBatchKey getPaymentInvoiceUniqueBatchKey(Refund refund) {
        return new PaymentInvoiceUniqueBatchKey(refund.getInvoiceId(), refund.getPaymentId());
    }

    public static PaymentState getPaymentState(String invoiceId, Integer changeId, long sequenceId, LocalDateTime eventCreatedAt, String paymentId, com.rbkmoney.damsel.domain.InvoicePaymentStatus status) {
        PaymentState paymentState = new PaymentState();
        paymentState.setInvoiceId(invoiceId);
        paymentState.setSequenceId(sequenceId);
        paymentState.setChangeId(changeId);
        paymentState.setEventCreatedAt(eventCreatedAt);
        paymentState.setPaymentId(paymentId);
        paymentState.setStatus(TBaseUtil.unionFieldToEnum(status, InvoicePaymentStatus.class));

        return paymentState;
    }

    public static PaymentCost getPaymentCost(String invoiceId, long sequenceId, Integer changeId, LocalDateTime eventCreatedAt, String paymentId, Long amount, String currency) {
        PaymentCost paymentCost = new PaymentCost();
        paymentCost.setInvoiceId(invoiceId);
        paymentCost.setSequenceId(sequenceId);
        paymentCost.setChangeId(changeId);
        paymentCost.setEventCreatedAt(eventCreatedAt);
        paymentCost.setPaymentId(paymentId);
        paymentCost.setAmount(amount);
        paymentCost.setOriginAmount(amount);
        paymentCost.setCurrencyCode(currency);

        return paymentCost;
    }

    public static PaymentFee getPaymentFee(String invoiceId, Integer changeId, long sequenceId, LocalDateTime eventCreatedAt, String paymentId, Map<FeeType, Long> fees, Map<FeeType, String> currencies) {
        PaymentFee paymentFee = new PaymentFee();
        paymentFee.setInvoiceId(invoiceId);
        paymentFee.setSequenceId(sequenceId);
        paymentFee.setChangeId(changeId);
        paymentFee.setEventCreatedAt(eventCreatedAt);
        paymentFee.setPaymentId(paymentId);
        paymentFee.setFee(fees.get(FeeType.FEE));
        paymentFee.setFeeCurrencyCode(currencies.get(FeeType.FEE));
        paymentFee.setProviderFee(fees.get(FeeType.PROVIDER_FEE));
        paymentFee.setProviderFeeCurrencyCode(currencies.get(FeeType.PROVIDER_FEE));
        paymentFee.setExternalFee(fees.get(FeeType.EXTERNAL_FEE));
        paymentFee.setExternalFeeCurrencyCode(currencies.get(FeeType.EXTERNAL_FEE));

        return paymentFee;
    }

    public static PaymentRouting getPaymentRouting(String invoiceId, Integer changeId, long sequenceId, LocalDateTime eventCreatedAt, String paymentId, PaymentRoute paymentRoute) {
        PaymentRouting paymentRouting = new PaymentRouting();
        paymentRouting.setInvoiceId(invoiceId);
        paymentRouting.setSequenceId(sequenceId);
        paymentRouting.setChangeId(changeId);
        paymentRouting.setEventCreatedAt(eventCreatedAt);
        paymentRouting.setPaymentId(paymentId);
        paymentRouting.setProviderId(paymentRoute.getProvider().getId());
        paymentRouting.setTerminalId(paymentRoute.getTerminal().getId());

        return paymentRouting;
    }

    public static PaymentTerminalReceipt getPaymentTerminalReceipt(String invoiceId, Integer changeId, long sequenceId, LocalDateTime eventCreatedAt, String paymentId, com.rbkmoney.damsel.user_interaction.PaymentTerminalReceipt damselPaymentTerminalReceipt) {
        PaymentTerminalReceipt paymentTerminalReceipt = new PaymentTerminalReceipt();
        paymentTerminalReceipt.setInvoiceId(invoiceId);
        paymentTerminalReceipt.setSequenceId(sequenceId);
        paymentTerminalReceipt.setChangeId(changeId);
        paymentTerminalReceipt.setEventCreatedAt(eventCreatedAt);
        paymentTerminalReceipt.setPaymentId(paymentId);
        paymentTerminalReceipt.setPaymentShortId(damselPaymentTerminalReceipt.getShortPaymentId());

        return paymentTerminalReceipt;
    }
}
