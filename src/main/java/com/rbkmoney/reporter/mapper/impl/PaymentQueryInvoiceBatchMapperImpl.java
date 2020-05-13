package com.rbkmoney.reporter.mapper.impl;

import com.rbkmoney.reporter.batch.UniqueBatchKey;
import com.rbkmoney.reporter.batch.key.InvoiceUniqueBatchKeyImpl;
import com.rbkmoney.reporter.batch.key.PaymentInvoiceUniqueBatchKey;
import com.rbkmoney.reporter.dao.mapper.dto.PartyData;
import com.rbkmoney.reporter.dao.mapper.dto.PaymentPartyData;
import com.rbkmoney.reporter.dao.query.PaymentQueryTemplator;
import com.rbkmoney.reporter.domain.tables.pojos.*;
import com.rbkmoney.reporter.mapper.InvoiceBatchMapper;
import com.rbkmoney.reporter.mapper.InvoiceChangeMapper;
import com.rbkmoney.reporter.mapper.MapperPayload;
import com.rbkmoney.reporter.mapper.MapperResult;
import com.rbkmoney.reporter.service.InvoiceService;
import com.rbkmoney.reporter.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.reporter.util.MapperUtil.*;

@Component
@RequiredArgsConstructor
public class PaymentQueryInvoiceBatchMapperImpl implements InvoiceBatchMapper<PaymentPartyData, PartyData> {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final PaymentQueryTemplator paymentQueryTemplator;

    @Override
    public List<Query> map(InvoiceChangeMapper mapper, MapperPayload payload, Map<UniqueBatchKey, PaymentPartyData> producerCache, Map<UniqueBatchKey, PartyData> consumerCache) {
        List<Query> queries = new ArrayList<>();

        MapperResult mapperResult = mapper.map(payload.getInvoiceChange(), payload.getMachineEvent(), payload.getChangeId());

        if (mapperResult.getPayment() != null) {
            Payment payment = mapperResult.getPayment();

            fillPaymentFromCache(payment, getInvoiceUniqueBatchKeyImpl(payment), consumerCache);

            Query savePaymentQuery = paymentQueryTemplator.getSavePaymentQuery(payment);
            queries.add(savePaymentQuery);
        }

        if (mapperResult.getPaymentState() != null) {
            PaymentState paymentState = mapperResult.getPaymentState();

            Query savePaymentStateQuery = paymentQueryTemplator.getSavePaymentStateQuery(paymentState);
            queries.add(savePaymentStateQuery);
        }

        if (mapperResult.getPaymentCost() != null) {
            PaymentCost paymentCost = mapperResult.getPaymentCost();

            Payment payment = getPayment(mapperResult, paymentCost, consumerCache);

            fillCacheFromPayment(payment, paymentCost, producerCache);

            Query savePaymentCostQuery = paymentQueryTemplator.getSavePaymentCostQuery(paymentCost);
            queries.add(savePaymentCostQuery);
        }

        if (mapperResult.getPaymentRouting() != null) {
            PaymentRouting paymentRouting = mapperResult.getPaymentRouting();

            Query savePaymentRoutingQuery = paymentQueryTemplator.getSavePaymentRoutingQuery(paymentRouting);
            queries.add(savePaymentRoutingQuery);
        }

        if (mapperResult.getPaymentTerminalReceipt() != null) {
            PaymentTerminalReceipt paymentTerminalReceipt = mapperResult.getPaymentTerminalReceipt();

            Query savePaymentTerminalQuery = paymentQueryTemplator.getSavePaymentTerminalQuery(paymentTerminalReceipt);
            queries.add(savePaymentTerminalQuery);
        }

        if (mapperResult.getPaymentFee() != null) {
            Query savePaymentFeeQuery = paymentQueryTemplator.getSavePaymentFeeQuery(mapperResult.getPaymentFee());
            queries.add(savePaymentFeeQuery);
        }

        return queries;
    }


    private void fillCacheFromPayment(Payment payment, PaymentCost paymentCost, Map<UniqueBatchKey, PaymentPartyData> producerCache) {
        PaymentInvoiceUniqueBatchKey uniqueBatchKey = getPaymentInvoiceUniqueBatchKey(payment);
        PaymentPartyData paymentPartyData = getPaymentPartyData(paymentCost, payment);

        producerCache.put(uniqueBatchKey, paymentPartyData);

        paymentService.savePaymentPartyData(uniqueBatchKey, paymentPartyData);
    }

    private Payment getPayment(MapperResult mapperResult, PaymentCost paymentCost, Map<UniqueBatchKey, PartyData> consumerCache) {
        Payment payment = mapperResult.getPayment();

        if (payment == null) {
            payment = new Payment();
            payment.setInvoiceId(paymentCost.getInvoiceId());
            payment.setPaymentId(paymentCost.getPaymentId());

            fillPaymentFromCache(payment, getInvoiceUniqueBatchKeyImpl(payment), consumerCache);
        }

        return payment;
    }

    private void fillPaymentFromCache(Payment payment, InvoiceUniqueBatchKeyImpl uniqueBatchKey, Map<UniqueBatchKey, PartyData> consumerCache) {
        PartyData partyData = consumerCache.computeIfAbsent(uniqueBatchKey, key -> invoiceService.getPartyData(uniqueBatchKey));

        payment.setPartyId(partyData.getPartyId());
        payment.setPartyShopId(partyData.getPartyShopId());
    }
}
