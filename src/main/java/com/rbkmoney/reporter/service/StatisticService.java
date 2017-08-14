package com.rbkmoney.reporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.merch_stat.MerchantStatisticsSrv;
import com.rbkmoney.damsel.merch_stat.StatRequest;
import com.rbkmoney.reporter.dsl.Query;
import com.rbkmoney.reporter.dsl.ShopAccountingQuery;
import com.rbkmoney.reporter.dsl.StatisticDsl;
import com.rbkmoney.reporter.model.ShopAccountingModel;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tolkonepiu on 11/07/2017.
 */
@Service
public class StatisticService {

    @Autowired
    private MerchantStatisticsSrv.Iface merchantStatisticsSrv;

    @Autowired
    private ObjectMapper objectMapper;

    public ShopAccountingModel getShopAccounting(String partyId, String shopId, Instant fromTime, Instant toTime) {
        return getShopAccountings(fromTime, toTime).stream().filter(
                shopAccountingModel -> shopAccountingModel.getMerchantId().equals(partyId)
                        && shopAccountingModel.getShopId().equals(shopId)
        ).findFirst().orElse(new ShopAccountingModel());
    }

    public List<ShopAccountingModel> getShopAccountings(Instant fromTime, Instant toTime) {
        try {
            return merchantStatisticsSrv.getStatistics(createShopAccountingStatRequest(fromTime, toTime))
                    .getData()
                    .getRecords()
                    .stream()
                    .map(record -> objectMapper.convertValue(record, ShopAccountingModel.class))
                    .collect(Collectors.toList());
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

    private StatRequest createShopAccountingStatRequest(Instant from, Instant to) {
        StatisticDsl statisticDsl = new StatisticDsl();
        Query query = new Query();
        ShopAccountingQuery shopAccountingQuery = new ShopAccountingQuery();
        shopAccountingQuery.setFromTime(from);
        shopAccountingQuery.setToTime(to);
        query.setShopAccountingQuery(shopAccountingQuery);
        statisticDsl.setQuery(query);

        try {
            return new StatRequest(objectMapper.writeValueAsString(statisticDsl));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
