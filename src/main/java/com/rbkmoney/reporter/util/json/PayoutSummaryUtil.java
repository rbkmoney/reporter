package com.rbkmoney.reporter.util.json;

import com.rbkmoney.damsel.payout_processing.PayoutSummaryItem;
import com.rbkmoney.geck.common.util.TypeUtil;

import java.util.List;
import java.util.stream.Collectors;

public class PayoutSummaryUtil {

    public static PayoutSummary toDtoPayoutSummary(List<PayoutSummaryItem> items) {
        List<PayoutSummary.PayoutSummaryItem> payoutSummaryItems = items.stream()
                .map(
                        item -> {
                            PayoutSummary.PayoutSummaryItem payoutSummaryItem = new PayoutSummary.PayoutSummaryItem();
                            payoutSummaryItem.setAmount(item.getAmount());
                            payoutSummaryItem.setFee(item.getFee());
                            fillCurrency(item, payoutSummaryItem);
                            payoutSummaryItem.setFromTime(TypeUtil.stringToLocalDateTime(item.getFromTime()));
                            payoutSummaryItem.setToTime(TypeUtil.stringToLocalDateTime(item.getToTime()));
                            fillOperationType(item, payoutSummaryItem);
                            payoutSummaryItem.setCount(item.getCount());
                            return payoutSummaryItem;
                        }
                )
                .collect(Collectors.toList());

        PayoutSummary payoutSummary = new PayoutSummary();
        payoutSummary.setPayoutSummaryItems(payoutSummaryItems);
        return payoutSummary;
    }

    private static void fillCurrency(PayoutSummaryItem item, PayoutSummary.PayoutSummaryItem payoutSummaryItem) {
        PayoutSummary.PayoutSummaryItem.CurrencyRef currencyRef = new PayoutSummary.PayoutSummaryItem.CurrencyRef();
        currencyRef.setSymbolicCode(item.getCurrencySymbolicCode());

        payoutSummaryItem.setCurrency(currencyRef);
    }

    private static void fillOperationType(PayoutSummaryItem item, PayoutSummary.PayoutSummaryItem payoutSummaryItem) {
        switch (item.getOperationType()) {
            case refund:
                payoutSummaryItem.setOperationType(PayoutSummary.PayoutSummaryItem.OperationType.REFUND);
                break;
            case payment:
                payoutSummaryItem.setOperationType(PayoutSummary.PayoutSummaryItem.OperationType.PAYMENT);
                break;
            case adjustment:
                payoutSummaryItem.setOperationType(PayoutSummary.PayoutSummaryItem.OperationType.ADJUSTMENT);
                break;
            default:
                break;
        }
    }
}
