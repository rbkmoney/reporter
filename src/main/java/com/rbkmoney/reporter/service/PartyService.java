package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.payment_processing.ContractNotFound;
import com.rbkmoney.reporter.exception.ContractNotFoundException;
import com.rbkmoney.reporter.exception.PartyNotFoundException;
import com.rbkmoney.reporter.exception.ShopNotFoundException;
import com.rbkmoney.reporter.model.PartyModel;

import java.time.Instant;
import java.util.Map;

/**
 * Created by tolkonepiu on 17/07/2017.
 */
public interface PartyService {

    PartyModel getPartyRepresentation(String partyId, String shopId, Instant timestamp) throws PartyNotFoundException, ShopNotFoundException;

    Map<String, String> getShopUrls(String partyId, String contractId, Instant timestamp) throws PartyNotFoundException, ContractNotFoundException;
}
