package com.rbkmoney.reporter.util;

import com.rbkmoney.damsel.domain.FinalCashFlowAccount;
import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.enums.AdjustmentCashFlowType;
import com.rbkmoney.reporter.domain.enums.CashFlowAccount;
import com.rbkmoney.reporter.domain.enums.PaymentChangeType;
import com.rbkmoney.reporter.domain.tables.pojos.CashFlow;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CashFlowUtil {

    public static CashFlowAccount getCashFlowAccountType(FinalCashFlowAccount cfa) {
        CashFlowAccount sourceAccountType =
                TypeUtil.toEnumField(cfa.getAccountType().getSetField().getFieldName(), CashFlowAccount.class);
        if (sourceAccountType == null) {
            throw new IllegalArgumentException("Illegal cash flow account type: " + cfa.getAccountType());
        }
        return sourceAccountType;
    }

    public static String getCashFlowAccountTypeValue(FinalCashFlowAccount cfa) {
        if (cfa.getAccountType().isSetMerchant()) {
            return cfa.getAccountType().getMerchant().name();
        } else if (cfa.getAccountType().isSetProvider()) {
            return cfa.getAccountType().getProvider().name();
        } else if (cfa.getAccountType().isSetSystem()) {
            return cfa.getAccountType().getSystem().name();
        } else if (cfa.getAccountType().isSetExternal()) {
            return cfa.getAccountType().getExternal().name();
        } else if (cfa.getAccountType().isSetWallet()) {
            return cfa.getAccountType().getWallet().name();
        } else {
            throw new IllegalArgumentException("Illegal cash flow account type: " + cfa.getAccountType());
        }
    }

    public static List<CashFlow> convertCashFlows(List<FinalCashFlowPosting> cashFlowPostings,
                                                  String invoiceId,
                                                  Long sequenceId,
                                                  Integer changeId,
                                                  String paymentId,
                                                  LocalDateTime createdAt,
                                                  PaymentChangeType paymentchangetype) {
        return convertCashFlows(cashFlowPostings, invoiceId, sequenceId, changeId, paymentId, null, createdAt, paymentchangetype, null);
    }

    public static List<CashFlow> convertRefundCashFlows(List<FinalCashFlowPosting> cashFlowPostings,
                                                        String invoiceId,
                                                        Long sequenceId,
                                                        Integer changeId,
                                                        String paymentId,
                                                        String refundId,
                                                        LocalDateTime createdAt,
                                                        PaymentChangeType paymentchangetype) {
        return convertCashFlows(cashFlowPostings, invoiceId, sequenceId, changeId, paymentId, refundId, createdAt, paymentchangetype, null);
    }

    public static List<CashFlow> convertCashFlows(List<FinalCashFlowPosting> cashFlowPostings,
                                                  String invoiceId,
                                                  Long sequenceId,
                                                  Integer changeId,
                                                  String paymentId,
                                                  String refundId,
                                                  LocalDateTime createdAt,
                                                  PaymentChangeType paymentchangetype,
                                                  AdjustmentCashFlowType adjustmentcashflowtype) {
        return cashFlowPostings.stream().map(cf -> {
            CashFlow pcf = new CashFlow();
            pcf.setInvoiceId(invoiceId);
            pcf.setSequenceId(sequenceId);
            pcf.setChangeId(changeId);
            pcf.setPaymentId(paymentId);
            pcf.setRefundId(refundId);
            pcf.setCreatedAt(createdAt);
            pcf.setObjType(paymentchangetype);
            pcf.setAdjFlowType(adjustmentcashflowtype);
            pcf.setSourceAccountType(CashFlowUtil.getCashFlowAccountType(cf.getSource()));
            pcf.setSourceAccountTypeValue(getCashFlowAccountTypeValue(cf.getSource()));
            pcf.setSourceAccountId(cf.getSource().getAccountId());
            pcf.setDestinationAccountType(CashFlowUtil.getCashFlowAccountType(cf.getDestination()));
            pcf.setDestinationAccountTypeValue(getCashFlowAccountTypeValue(cf.getDestination()));
            pcf.setDestinationAccountId(cf.getDestination().getAccountId());
            pcf.setAmount(cf.getVolume().getAmount());
            pcf.setCurrencyCode(cf.getVolume().getCurrency().getSymbolicCode());
            pcf.setDetails(cf.getDetails());
            return pcf;
        }).collect(Collectors.toList());
    }

}
