package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.Party;
import com.rbkmoney.damsel.domain.PaymentInstitutionRef;
import com.rbkmoney.damsel.domain.Shop;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.exception.ContractNotFoundException;
import com.rbkmoney.reporter.exception.NotFoundException;
import com.rbkmoney.reporter.exception.PartyNotFoundException;
import com.rbkmoney.reporter.exception.ShopNotFoundException;
import com.rbkmoney.reporter.service.PartyService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class PartyServiceImpl implements PartyService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final UserInfo userInfo = new UserInfo("reporter", UserType.internal_user(new InternalUser()));

    private final PartyManagementSrv.Iface partyManagementClient;

    @Autowired
    public PartyServiceImpl(PartyManagementSrv.Iface partyManagementClient) {
        this.partyManagementClient = partyManagementClient;
    }

    @Override
    public Party getParty(String partyId) throws PartyNotFoundException {
        return getParty(partyId, Instant.now());
    }

    @Override
    public Party getParty(String partyId, Instant timestamp) throws PartyNotFoundException {
        return getParty(partyId, PartyRevisionParam.timestamp(TypeUtil.temporalToString(timestamp)));
    }

    @Override
    public Party getParty(String partyId, long partyRevision) throws PartyNotFoundException {
        return getParty(partyId, PartyRevisionParam.revision(partyRevision));
    }

    @Override
    public Party getParty(String partyId, PartyRevisionParam partyRevisionParam) throws PartyNotFoundException {
        log.info("Trying to get party, partyId='{}', partyRevisionParam='{}'", partyId, partyRevisionParam);
        try {
            Party party = partyManagementClient.checkout(userInfo, partyId, partyRevisionParam);
            log.info("Party has been found, partyId='{}', partyRevisionParam='{}'", partyId, partyRevisionParam);
            return party;
        } catch (PartyNotFound ex) {
            throw new PartyNotFoundException(
                    String.format("Party not found, partyId='%s', partyRevisionParam='%s'", partyId, partyRevisionParam), ex
            );
        } catch (InvalidPartyRevision ex) {
            throw new PartyNotFoundException(
                    String.format("Invalid party revision, partyId='%s', partyRevisionParam='%s'", partyId, partyRevisionParam), ex
            );
        } catch (TException ex) {
            throw new RuntimeException(
                    String.format("Failed to get party, partyId='%s', partyRevisionParam='%s'", partyId, partyRevisionParam), ex
            );
        }
    }

    @Override
    public Shop getShop(String partyId, String shopId) throws ShopNotFoundException, PartyNotFoundException {
        return getShop(partyId, shopId, Instant.now());
    }

    @Override
    public Shop getShop(String partyId, String shopId, long partyRevision) throws ShopNotFoundException, PartyNotFoundException {
        return getShop(partyId, shopId, PartyRevisionParam.revision(partyRevision));
    }

    @Override
    public Shop getShop(String partyId, String shopId, Instant timestamp) throws ShopNotFoundException, PartyNotFoundException {
        return getShop(partyId, shopId, PartyRevisionParam.timestamp(TypeUtil.temporalToString(timestamp)));
    }

    @Override
    public Shop getShop(String partyId, String shopId, PartyRevisionParam partyRevisionParam) throws ShopNotFoundException, PartyNotFoundException {
        log.info("Trying to get shop, partyId='{}', shopId='{}', partyRevisionParam='{}'", partyId, shopId, partyRevisionParam);
        Party party = getParty(partyId, partyRevisionParam);

        Shop shop = party.getShops().get(shopId);
        if (shop == null) {
            throw new ShopNotFoundException(String.format("Shop not found, partyId='%s', shopId='%s', partyRevisionParam='%s'", partyId, shopId, partyRevisionParam));
        }
        log.info("Shop has been found, partyId='{}', shopId='{}', partyRevisionParam='{}'", partyId, shopId, partyRevisionParam);
        return shop;
    }

    @Override
    public Contract getContract(String partyId, String contractId) throws ContractNotFoundException, PartyNotFoundException {
        return getContract(partyId, contractId, Instant.now());
    }

    @Override
    public Contract getContract(String partyId, String contractId, long partyRevision) throws ContractNotFoundException, PartyNotFoundException {
        return getContract(partyId, contractId, PartyRevisionParam.revision(partyRevision));
    }

    @Override
    public Contract getContract(String partyId, String contractId, Instant timestamp) throws ContractNotFoundException, PartyNotFoundException {
        return getContract(partyId, contractId, PartyRevisionParam.timestamp(TypeUtil.temporalToString(timestamp)));
    }

    @Override
    public Contract getContract(String partyId, String contractId, PartyRevisionParam partyRevisionParam) throws ContractNotFoundException, PartyNotFoundException {
        log.info("Trying to get contract, partyId='{}', contractId='{}', partyRevisionParam='{}'", partyId, contractId, partyRevisionParam);
        Party party = getParty(partyId, partyRevisionParam);

        Contract contract = party.getContracts().get(contractId);
        if (contract == null) {
            throw new ContractNotFoundException(String.format("Contract not found, partyId='%s', contractId='%s', partyRevisionParam='%s'", partyId, contractId, partyRevisionParam));
        }
        log.info("Contract has been found, partyId='{}', contractId='{}', partyRevisionParam='{}'", partyId, contractId, partyRevisionParam);
        return contract;
    }

    @Override
    public PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId) throws ContractNotFoundException, PartyNotFoundException {
        return getPaymentInstitutionRef(partyId, contractId, Instant.now());
    }

    @Override
    public PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId, long partyRevision) throws ContractNotFoundException, PartyNotFoundException {
        return getPaymentInstitutionRef(partyId, contractId, PartyRevisionParam.revision(partyRevision));
    }

    @Override
    public PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId, Instant timestamp) throws ContractNotFoundException, PartyNotFoundException {
        return getPaymentInstitutionRef(partyId, contractId, PartyRevisionParam.timestamp(TypeUtil.temporalToString(timestamp)));
    }

    @Override
    public PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId, PartyRevisionParam partyRevisionParam) throws ContractNotFoundException, PartyNotFoundException {
        log.debug("Trying to get paymentInstitutionRef, partyId='{}', contractId='{}', partyRevisionParam='{}'", partyId, contractId, partyRevisionParam);
        Contract contract = getContract(partyId, contractId, partyRevisionParam);

        if (!contract.isSetPaymentInstitution()) {
            throw new NotFoundException(String.format("PaymentInstitutionRef not found, partyId='%s', contractId='%s', partyRevisionParam='%s'", partyId, contractId, partyRevisionParam));
        }

        PaymentInstitutionRef paymentInstitutionRef = contract.getPaymentInstitution();
        log.info("PaymentInstitutionRef has been found, partyId='{}', contractId='{}', paymentInstitutionRef='{}', partyRevisionParam='{}'", partyId, contractId, paymentInstitutionRef, partyRevisionParam);
        return paymentInstitutionRef;
    }

    @Override
    public Map<String, String> getShopUrls(String partyId, String contractId, Instant timestamp) throws PartyNotFoundException, ContractNotFoundException {
        Party party = getParty(partyId, timestamp);
        Map<String, String> shopUrls = new HashMap<>();
        party.getShops().forEach((id, shop) -> {
            if (shop.getContractId().equals(contractId) && shop.getLocation().isSetUrl()) {
                shopUrls.put(id, shop.getLocation().getUrl());
            }
        });
        return shopUrls;
    }

}
