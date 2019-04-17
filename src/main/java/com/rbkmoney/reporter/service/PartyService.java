package com.rbkmoney.reporter.service;


import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.Party;
import com.rbkmoney.damsel.domain.PaymentInstitutionRef;
import com.rbkmoney.damsel.domain.Shop;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.payment_processing.PartyRevisionParam;
import com.rbkmoney.reporter.exception.ContractNotFoundException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.PartyNotFoundException;
import com.rbkmoney.reporter.exception.ShopNotFoundException;

import java.time.Instant;

/**
 * Created by tolkonepiu on 17/07/2017.
 */
public interface PartyService {

    Party getParty(String partyId) throws PartyNotFoundException;

    Party getParty(String partyId, Instant timestamp) throws PartyNotFoundException;

    Party getParty(String partyId, long partyRevision) throws PartyNotFoundException;

    Party getParty(String partyId, PartyRevisionParam partyRevisionParam) throws PartyNotFoundException;

    Shop getShop(String partyId, String shopId) throws ShopNotFoundException, PartyNotFoundException;

    Shop getShop(String partyId, String shopId, long partyRevision) throws ShopNotFoundException, PartyNotFoundException;

    Shop getShop(String partyId, String shopId, Instant timestamp) throws ShopNotFoundException, PartyNotFoundException;

    Shop getShop(String partyId, String shopId, PartyRevisionParam partyRevisionParam) throws ShopNotFoundException, PartyNotFoundException;

    Contract getContract(String partyId, String contractId) throws ContractNotFoundException, PartyNotFoundException;

    Contract getContract(String partyId, String contractId, long partyRevision) throws ContractNotFoundException, PartyNotFoundException;

    Contract getContract(String partyId, String contractId, Instant timestamp) throws ContractNotFoundException, PartyNotFoundException;

    Contract getContract(String partyId, String contractId, PartyRevisionParam partyRevisionParam) throws ContractNotFoundException, PartyNotFoundException;

    PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId) throws ContractNotFoundException, PartyNotFoundException;

    PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId, long partyRevision) throws ContractNotFoundException, PartyNotFoundException;

    PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId, Instant timestamp) throws ContractNotFoundException, PartyNotFoundException;

    PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId, PartyRevisionParam partyRevisionParam) throws ContractNotFoundException, PartyNotFoundException;

    String getShopUrl(String partyId, String shopId, Instant timestamp) throws PartyNotFoundException, ShopNotFoundException;

    Value getMetaData(String partyId, String namespace) throws NotFoundException;

}
