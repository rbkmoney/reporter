package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.merch_stat.InvoicePaymentRefundStatus;
import com.rbkmoney.damsel.merch_stat.InvoicePaymentStatus;
import com.rbkmoney.damsel.merch_stat.StatPayment;
import com.rbkmoney.damsel.merch_stat.StatRefund;
import com.rbkmoney.reporter.model.ShopAccountingModel;

import java.time.Instant;
import java.util.List;

/**
 * Created by tolkonepiu on 11/07/2017.
 */
public interface StatisticService {

    ShopAccountingModel getShopAccounting(String partyId, String contractId, String currencyCode, Instant toTime);

    ShopAccountingModel getShopAccounting(String partyId, String contractId, String currencyCode, Instant fromTime, Instant toTime);

    List<StatPayment> getPayments(String partyId, String contractId, Instant fromTime, Instant toTime, InvoicePaymentStatus status);

    StatPayment getPayment(String invoiceId, String paymentId, InvoicePaymentStatus status);

    List<StatRefund> getRefunds(String partyId, String contractId, Instant fromTime, Instant toTime, InvoicePaymentRefundStatus status);

}
