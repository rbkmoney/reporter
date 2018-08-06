package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.merch_stat.*;
import com.rbkmoney.reporter.model.ShopAccountingModel;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by tolkonepiu on 11/07/2017.
 */
public interface StatisticService {

    ShopAccountingModel getShopAccounting(String partyId, String contractId, String currencyCode, Instant toTime);

    ShopAccountingModel getShopAccounting(String partyId, String contractId, String currencyCode, Instant fromTime, Instant toTime);

    Map<String, String> getPurposes(String partyId, String contractId, Instant fromTime, Instant toTime);

    StatInvoice getInvoice(String invoiceId);

    Iterator<StatPayment> getPaymentsIterator(String partyId, String contractId, Instant fromTime, Instant toTime, InvoicePaymentStatus status);

    StatPayment getPayment(String invoiceId, String paymentId);

    StatPayment getPayment(String invoiceId, String paymentId, Optional<InvoicePaymentStatus> status);

    Iterator<StatRefund> getRefundsIterator(String partyId, String contractId, Instant fromTime, Instant toTime, InvoicePaymentRefundStatus status);

}
