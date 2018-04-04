package com.rbkmoney.reporter.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.merch_stat.*;
import com.rbkmoney.reporter.dsl.DslUtil;
import com.rbkmoney.reporter.exception.PaymentNotFoundException;
import com.rbkmoney.reporter.model.Payment;
import com.rbkmoney.reporter.model.ShopAccountingModel;
import com.rbkmoney.reporter.service.DomainConfigService;
import com.rbkmoney.reporter.service.StatisticService;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public ShopAccountingModel getShopAccounting(String partyId, String shopId, Instant fromTime, Instant toTime) {
        ShopAccountingModel shopAccounting = getShopAccountings(fromTime, toTime).stream().filter(
                shopAccountingModel -> shopAccountingModel.getMerchantId().equals(partyId)
                        && shopAccountingModel.getShopId().equals(shopId)
        ).findFirst().orElse(new ShopAccountingModel());
        validate(shopAccounting);
        return shopAccounting;
    }

    @Override
    public List<ShopAccountingModel> getShopAccountings(Instant fromTime, Instant toTime) {
        try {
            return merchantStatisticsClient.getStatistics(DslUtil.createShopAccountingStatRequest(fromTime, toTime, objectMapper))
                    .getData()
                    .getRecords()
                    .stream()
                    .map(record -> objectMapper.convertValue(record, ShopAccountingModel.class))
                    .collect(Collectors.toList());
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<StatPayment> getPayments(String partyId, String contractId, Instant fromTime, Instant toTime, InvoicePaymentStatus status) {
        try {
            long from = 0;
            int size = 1000;
            List<StatPayment> payments = new ArrayList<>();
            List<StatPayment> nextPayments;
            do {
                StatResponse statResponse = merchantStatisticsClient.getPayments(DslUtil.createPaymentsRequest(partyId, contractId, fromTime, toTime, status, from, size, objectMapper));
                nextPayments = statResponse.getData().getPayments();
                payments.addAll(nextPayments);
                from += size;
            } while (nextPayments.size() == size);
            return payments;
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public StatPayment getPayment(String invoiceId, String paymentId, InvoicePaymentStatus status) {
        try {
            List<StatPayment> payments = merchantStatisticsClient.getPayments(DslUtil.createPaymentRequest(invoiceId, paymentId, status, objectMapper)).getData().getPayments();
            if (payments.isEmpty()) {
                throw new PaymentNotFoundException(String.format("Payment with id={}.{} and status={} not found", invoiceId, paymentId, status.getSetField().getFieldName()));
            }
            return payments.get(0);
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<StatRefund> getRefunds(String partyId, String contractId, Instant fromTime, Instant toTime, InvoicePaymentRefundStatus status) {
        try {
            long from = 0;
            int size = 1000;
            List<StatRefund> refunds = new ArrayList<>();
            List<StatRefund> nextRefunds;
            do {
                StatResponse statResponse = merchantStatisticsClient.getPayments(DslUtil.createRefundsRequest(partyId, contractId, fromTime, toTime, status, from, size, objectMapper));
                nextRefunds = statResponse.getData().getRefunds();
                refunds.addAll(nextRefunds);
                from += size;
            } while (nextRefunds.size() == size);
            return refunds;
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

    private <T> void validate(T model) {
        Set<ConstraintViolation<T>> constraintViolations = factory.getValidator().validate(model);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }
}
