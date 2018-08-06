package com.rbkmoney.reporter.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.merch_stat.InvoicePaymentRefundStatus;
import com.rbkmoney.damsel.merch_stat.InvoicePaymentStatus;
import com.rbkmoney.damsel.merch_stat.InvoiceStatus;
import com.rbkmoney.damsel.merch_stat.StatRequest;

import java.time.Instant;
import java.util.Optional;

public class DslUtil {

    public static StatRequest createPaymentsRequest(String partyId, String contractId, Instant fromTime, Instant toTime, InvoicePaymentStatus status, Optional<String> continuationToken, int size, ObjectMapper objectMapper) {
        StatisticDsl statisticDsl = new StatisticDsl();
        Query query = new Query();
        PaymentsQuery paymentsQuery = new PaymentsQuery();
        paymentsQuery.setMerchantId(partyId);
        paymentsQuery.setContractId(contractId);
        paymentsQuery.setFromTime(fromTime);
        paymentsQuery.setToTime(toTime);
        paymentsQuery.setPaymentStatus(status.getSetField().getFieldName());
        query.setPaymentsQuery(paymentsQuery);
        query.setSize(size);
        statisticDsl.setQuery(query);

        return createStatRequest(statisticDsl, continuationToken, objectMapper);
    }

    public static StatRequest createInvoicesRequest(String partyId, String contractId, Instant fromTime, Instant toTime, Optional<String> continuationToken, int size, ObjectMapper objectMapper) {
        StatisticDsl statisticDsl = new StatisticDsl();
        Query query = new Query();
        InvoicesQuery invoicesQuery = new InvoicesQuery();
        invoicesQuery.setMerchantId(partyId);
        invoicesQuery.setContractId(contractId);
        invoicesQuery.setFromTime(fromTime);
        invoicesQuery.setToTime(toTime);
        query.setInvoicesQuery(invoicesQuery);
        query.setSize(size);
        statisticDsl.setQuery(query);

        return createStatRequest(statisticDsl, continuationToken, objectMapper);
    }

    public static StatRequest createInvoiceRequest(String invoiceId, ObjectMapper objectMapper) {
        StatisticDsl statisticDsl = new StatisticDsl();
        Query query = new Query();
        InvoicesQuery invoicesQuery = new InvoicesQuery();
        invoicesQuery.setInvoiceId(invoiceId);
        query.setInvoicesQuery(invoicesQuery);
        statisticDsl.setQuery(query);

        return createStatRequest(statisticDsl, Optional.empty(), objectMapper);
    }

    public static StatRequest createPaymentRequest(String invoiceId, String paymentId, Optional<InvoicePaymentStatus> status, ObjectMapper objectMapper) {
        StatisticDsl statisticDsl = new StatisticDsl();
        Query query = new Query();
        PaymentsQuery paymentsQuery = new PaymentsQuery();
        paymentsQuery.setInvoiceId(invoiceId);
        paymentsQuery.setPaymentId(paymentId);
        status.ifPresent((paymentStatus) -> paymentsQuery.setPaymentStatus(paymentStatus.getSetField().getFieldName()));
        query.setPaymentsQuery(paymentsQuery);
        statisticDsl.setQuery(query);

        return createStatRequest(statisticDsl, Optional.empty(), objectMapper);
    }

    public static StatRequest createRefundsRequest(String partyId, String contractId, Instant fromTime, Instant toTime, InvoicePaymentRefundStatus status, Optional<String> continuationToken, int size, ObjectMapper objectMapper) {
        StatisticDsl statisticDsl = new StatisticDsl();
        Query query = new Query();
        RefundsQuery refundsQuery = new RefundsQuery();
        refundsQuery.setMerchantId(partyId);
        refundsQuery.setContractId(contractId);
        refundsQuery.setFromTime(fromTime);
        refundsQuery.setToTime(toTime);
        refundsQuery.setRefundStatus(status.getSetField().getFieldName());
        query.setRefundsQuery(refundsQuery);
        query.setSize(size);
        statisticDsl.setQuery(query);

        return createStatRequest(statisticDsl, continuationToken, objectMapper);
    }

    public static StatRequest createStatRequest(StatisticDsl statisticDsl, Optional<String> continuationToken, ObjectMapper objectMapper) {
        try {
            StatRequest statRequest = new StatRequest(objectMapper.writeValueAsString(statisticDsl));
            continuationToken.ifPresent(
                    token -> statRequest.setContinuationToken(token)
            );
            return statRequest;
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static StatRequest createShopAccountingStatRequest(String merchantId, String contractId, String currencyCode, Optional<Instant> from, Instant to, ObjectMapper objectMapper) {
        StatisticDsl statisticDsl = new StatisticDsl();
        Query query = new Query();
        ShopAccountingQuery shopAccountingQuery = new ShopAccountingQuery();
        shopAccountingQuery.setMerchantId(merchantId);
        shopAccountingQuery.setContractId(contractId);
        shopAccountingQuery.setCurrencyCode(currencyCode);
        shopAccountingQuery.setFromTime(from);
        shopAccountingQuery.setToTime(to);
        query.setShopAccountingQuery(shopAccountingQuery);
        statisticDsl.setQuery(query);

        return createStatRequest(statisticDsl, Optional.empty(), objectMapper);
    }

}
