package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.model.PartyModel;
import com.rbkmoney.reporter.model.ShopAccountingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tolkonepiu on 17/07/2017.
 */
@Service
public class ReportService {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private PartyService partyService;

    public void generateProvisionOfServiceReport(String partyId, String shopId, Instant fromTime, Instant toTime) {
        List<ShopAccountingModel> shopAccountingModels = statisticService.getShopAccountings(fromTime, toTime).stream()
                .filter(shopAccounting ->
                        shopAccounting.getMerchantId().equals(partyId)
                                && shopAccounting.getShopId().equals(shopId)
                ).collect(Collectors.toList());
        generateProvisionOfServiceReport(shopAccountingModels, fromTime, toTime);
    }

    public void generateProvisionOfServiceReport(Instant fromTime, Instant toTime) {
        List<ShopAccountingModel> shopAccountingModels = statisticService.getShopAccountings(fromTime, toTime);
        generateProvisionOfServiceReport(shopAccountingModels, fromTime, toTime);
    }

    public void generateProvisionOfServiceReport(List<ShopAccountingModel> shopAccountingModels, Instant fromTime, Instant toTime) {
        for (ShopAccountingModel shopAccountingModel : shopAccountingModels) {
            String merchantId = shopAccountingModel.getMerchantId();
            String shopId = shopAccountingModel.getShopId();
            PartyModel partyModel = partyService.getPartyRepresentation(merchantId, shopId, toTime);
            generateProvisionOfServiceReport(partyModel, shopAccountingModel, fromTime, toTime);
        }
    }

    public void generateProvisionOfServiceReport(PartyModel partyModel, ShopAccountingModel shopAccountingModel, Instant fromTime, Instant toTime) {
        try {
            Path reportFile = Files.createTempFile("provision_of_service_", "_report.xlsx");
            templateService.processProvisionOfServiceTemplate(
                    partyModel,
                    shopAccountingModel,
                    fromTime,
                    toTime,
                    Files.newOutputStream(reportFile)
            );

            try(InputStream inputStream = Files.newInputStream(reportFile)) {
                //todo sign and save in ceph
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
