package com.rbkmoney.reporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.merch_stat.MerchantStatisticsSrv;
import com.rbkmoney.damsel.merch_stat.StatPayment;
import com.rbkmoney.damsel.merch_stat.StatRequest;
import com.rbkmoney.reporter.dsl.PaymentQuery;
import com.rbkmoney.reporter.dsl.Query;
import com.rbkmoney.reporter.dsl.StatisticDsl;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Created by tolkonepiu on 11/07/2017.
 */
@Service
public class StatisticService {

    @Autowired
    private MerchantStatisticsSrv.Iface merchantStatisticsSrv;

    @Autowired
    private ObjectMapper objectMapper;

    public List<StatPayment> getPayments(String partyId, String shopId, Instant from, Instant to) {
        try {
            return merchantStatisticsSrv.getPayments(createStatRequest(partyId, shopId, from, to))
                    .getData()
                    .getPayments();
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

    private StatRequest createStatRequest(String partyId, String shopId, Instant from, Instant to) {
        StatisticDsl statisticDsl = new StatisticDsl();
        Query query = new Query();
        PaymentQuery paymentQuery = new PaymentQuery();
        paymentQuery.setMerchantId(partyId);
        paymentQuery.setShopId(shopId);
        paymentQuery.setFromTime(from);
        paymentQuery.setToTime(to);
        query.setPayments(paymentQuery);
        statisticDsl.setQuery(query);

        try {
            return new StatRequest(objectMapper.writeValueAsString(statisticDsl));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }


}
