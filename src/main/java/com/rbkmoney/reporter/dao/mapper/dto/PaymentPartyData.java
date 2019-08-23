package com.rbkmoney.reporter.dao.mapper.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentPartyData {

    private UUID partyId;
    private String partyShopId;
    private Long paymentAmount;
    private String paymentCurrencyCode;

}
