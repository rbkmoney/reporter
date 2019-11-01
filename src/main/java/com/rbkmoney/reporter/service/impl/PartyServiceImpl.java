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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartyServiceImpl implements PartyService {

    private final UserInfo userInfo = new UserInfo("reporter", UserType.internal_user(new InternalUser()));

    private final PartyManagementSrv.Iface partyManagementClient;

    @Override
    public Party getParty(String partyId, Instant timestamp) throws PartyNotFoundException {
        return getParty(partyId, PartyRevisionParam.timestamp(TypeUtil.temporalToString(timestamp)));
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
        log.info("Trying to get shop, partyId='{}', shopId='{}'", partyId, shopId);
        try {
            Shop shop = partyManagementClient.getShop(userInfo, partyId, shopId);
            log.info("Shop has been found, partyId='{}', shopId='{}'", partyId, shopId);
            return shop;
        } catch (PartyNotFound ex) {
            throw new PartyNotFoundException(String.format("Party not found, partyId='%s'", partyId), ex);
        } catch (ShopNotFound ex) {
            throw new ShopNotFoundException(String.format("Shop not found, partyId='%s', shopId='%s'", partyId, shopId));
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get shop, partyId='%s', shopId='%s'", partyId, shopId), ex);
        }
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
    public String getShopUrl(String partyId, String shopId, Instant timestamp) throws PartyNotFoundException, ShopNotFoundException {
        Shop shop = getShop(partyId, shopId, timestamp);
        if (shop.getLocation().isSetUrl()) {
            return shop.getLocation().getUrl();
        } else {
            return null;
        }
    }
}
