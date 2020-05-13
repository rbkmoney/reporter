package com.rbkmoney.reporter.dao.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentPartyData {

    private UUID partyId;
    private String partyShopId;
    private Long paymentAmount;
    private String paymentCurrencyCode;

}
