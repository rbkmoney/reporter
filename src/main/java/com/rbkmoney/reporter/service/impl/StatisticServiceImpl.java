package com.rbkmoney.reporter.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.merch_stat.*;
import com.rbkmoney.reporter.dsl.DslUtil;
import com.rbkmoney.reporter.exception.InvoiceNotFoundException;
import com.rbkmoney.reporter.exception.PaymentNotFoundException;
import com.rbkmoney.reporter.model.ShopAccountingModel;
import com.rbkmoney.reporter.service.StatisticService;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.time.Instant;
import java.util.*;

@Service
public class StatisticServiceImpl implements StatisticService {

    private final MerchantStatisticsSrv.Iface merchantStatisticsClient;

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private final ObjectMapper objectMapper;

    @Autowired
    public StatisticServiceImpl(MerchantStatisticsSrv.Iface merchantStatisticsClient, ObjectMapper objectMapper) {
        this.merchantStatisticsClient = merchantStatisticsClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public ShopAccountingModel getShopAccounting(String partyId, String contractId, String currencyCode, Instant toTime) {
        return getShopAccounting(partyId, contractId, currencyCode, Optional.empty(), toTime);
    }

    @Override
    public ShopAccountingModel getShopAccounting(String partyId, String contractId, String currencyCode, Instant fromTime, Instant toTime) {
        return getShopAccounting(partyId, contractId, currencyCode, Optional.of(fromTime), toTime);
    }

    @Override
    public Map<String, String> getPurposes(String partyId, String contractId, Instant fromTime, Instant toTime) {
        try {
            long from = 0;
            int size = 1000;
            Map<String, String> purposes = new HashMap<>();
            List<StatInvoice> nextInvoices;
            do {
                StatResponse statResponse = merchantStatisticsClient.getInvoices(DslUtil.createInvoicesRequest(partyId, contractId, fromTime, toTime, from, size, objectMapper));
                nextInvoices = statResponse.getData().getInvoices();
                nextInvoices.forEach(i -> purposes.put(i.getId(), i.getProduct()));
                from += size;
            } while (nextInvoices.size() == size);
            return purposes;
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
        public StatInvoice getInvoice(String invoiceId) {
        try {
            return merchantStatisticsClient.getPayments(DslUtil.createInvoiceRequest(invoiceId, objectMapper))
                    .getData()
                    .getInvoices()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new InvoiceNotFoundException(String.format("Invoice not found, invoiceId='%s'", invoiceId)));
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ShopAccountingModel getShopAccounting(String partyId, String contractId, String currencyCode, Optional<Instant> fromTime, Instant toTime) {
        try {
            ShopAccountingModel shopAccounting = merchantStatisticsClient.getStatistics(
                    DslUtil.createShopAccountingStatRequest(partyId, contractId, currencyCode, fromTime, toTime, objectMapper)
            ).getData()
                    .getRecords()
                    .stream()
                    .map(record -> objectMapper.convertValue(record, ShopAccountingModel.class))
                    .findFirst()
                    .orElse(new ShopAccountingModel(partyId, contractId, currencyCode));
            validate(shopAccounting);
            return shopAccounting;
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Iterator<StatPayment> getPaymentsIterator(String partyId, String contractId, Instant fromTime, Instant toTime, InvoicePaymentStatus status) {
        return new Iterator<StatPayment>() {
            private Optional<String> continuationToken = Optional.empty();
            private final int size = 1000;
            private List<StatPayment> nextPayments;

            @Override
            public boolean hasNext() {
                if (nextPayments == null || ((!nextPayments.iterator().hasNext()) && continuationToken.isPresent())) {
                    try {
                        StatResponse statResponse = merchantStatisticsClient.getPayments(DslUtil.createPaymentsRequest(partyId, contractId, fromTime, toTime, status, continuationToken, size, objectMapper));
                        nextPayments = statResponse.getData().getPayments();
                        continuationToken = Optional.ofNullable(statResponse.getContinuationToken());
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }
                }
                return nextPayments.iterator().hasNext();
            }

            @Override
            public StatPayment next() {
                return nextPayments.iterator().next();
            }
        };
    }

    @Override
    public StatPayment getPayment(String invoiceId, String paymentId) {
        return getPayment(invoiceId, paymentId, Optional.empty());
    }

    @Override
    public StatPayment getPayment(String invoiceId, String paymentId, Optional<InvoicePaymentStatus> status) {
        try {
            return merchantStatisticsClient.getPayments(DslUtil.createPaymentRequest(invoiceId, paymentId, status, objectMapper))
                    .getData()
                    .getPayments()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new PaymentNotFoundException(String.format("Payment not found, invoiceId='%s', paymentId='%s', paymentStatus='%s'", invoiceId, paymentId, status)));
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Iterator<StatRefund> getRefundsIterator(String partyId, String contractId, Instant fromTime, Instant toTime, InvoicePaymentRefundStatus status) {
        return new Iterator<StatRefund>() {
            private Optional<String> continuationToken = Optional.empty();
            private int size = 1000;
            private List<StatRefund> nextRefunds;

            @Override
            public boolean hasNext() {
                if (nextRefunds == null || ((!nextRefunds.iterator().hasNext()) && continuationToken.isPresent())) {
                    try {
                        StatResponse statResponse = merchantStatisticsClient.getPayments(DslUtil.createRefundsRequest(partyId, contractId, fromTime, toTime, status, continuationToken, size, objectMapper));
                        nextRefunds = statResponse.getData().getRefunds();
                        continuationToken = Optional.ofNullable(statResponse.getContinuationToken());
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }
                }
                return nextRefunds.iterator().hasNext();
            }

            @Override
            public StatRefund next() {
                return nextRefunds.iterator().next();
            }
        };
    }

    private <T> void validate(T model) {
        Set<ConstraintViolation<T>> constraintViolations = factory.getValidator().validate(model);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }
}
