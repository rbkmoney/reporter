package com.rbkmoney.reporter.service.impl;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.exception.ContractNotFoundException;
import com.rbkmoney.reporter.exception.PartyNotFoundException;
import com.rbkmoney.reporter.exception.ShopNotFoundException;
import com.rbkmoney.reporter.model.PartyModel;
import com.rbkmoney.reporter.service.DomainConfigService;
import com.rbkmoney.reporter.service.PartyService;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PartyServiceImpl implements PartyService {

    private final UserInfo userInfo = new UserInfo("admin", UserType.internal_user(new InternalUser()));

    private final PartyManagementSrv.Iface partyManagementClient;

    private final DomainConfigService domainConfigService;

    @Autowired
    public PartyServiceImpl(PartyManagementSrv.Iface partyManagementClient, DomainConfigService domainConfigService) {
        this.partyManagementClient = partyManagementClient;
        this.domainConfigService = domainConfigService;
    }

    @Override
    public PartyModel getPartyRepresentation(String partyId, String shopId, Instant timestamp) throws PartyNotFoundException, ShopNotFoundException {
        try {
            Party party = partyManagementClient.checkout(userInfo, partyId, PartyRevisionParam.timestamp(TypeUtil.temporalToString(timestamp)));

            PartyModel partyModel = new PartyModel();
            partyModel.setMerchantId(partyId);

            Shop shop = party.getShops().get(shopId);

            if (shop == null) {
                throw new ShopNotFoundException(String.format("Shop not found, shopId='%s', partyId='%s', time='%s'", shopId, partyId, timestamp));
            }

            if (shop.getLocation().isSetUrl()) {
                partyModel.setShopUrl(shop.getLocation().getUrl());
            }

            partyModel.setShopId(shop.getId());

            ShopDetails details = shop.getDetails();
            if (details != null) {
                partyModel.setShopName(details.getName());
                partyModel.setShopDescription(details.getDescription());
            }

            CategoryType shopCategoryType = domainConfigService.getCategoryType(shop.getCategory());
            partyModel.setShopCategoryType(shopCategoryType);

            String contractId = shop.getContractId();
            Contract contract = party.getContracts().get(contractId);
            if (contract == null) {
                throw new ShopNotFoundException(String.format("Contract on shop not found, contractId='%s', shopId='%s', partyId='%s', time='%s'", contractId, shopId, partyId, timestamp));
            }

            if (contract.isSetLegalAgreement()) {
                LegalAgreement legalAgreement = contract.getLegalAgreement();
                partyModel.setMerchantContractId(legalAgreement.getLegalAgreementId());
                partyModel.setMerchantContractSignedAt(
                        Date.from(TypeUtil.stringToInstant(legalAgreement.getSignedAt()))
                );
            }

            if (contract.isSetContractor()
                    && contract.getContractor().isSetLegalEntity()
                    && contract.getContractor().getLegalEntity().isSetRussianLegalEntity()) {
                RussianLegalEntity entity = contract.getContractor()
                        .getLegalEntity()
                        .getRussianLegalEntity();
                partyModel.setMerchantName(entity.getRegisteredName());
                partyModel.setMerchantRepresentativeFullName(entity.getRepresentativeFullName());
                partyModel.setMerchantRepresentativePosition(entity.getRepresentativePosition());
            }

            return partyModel;
        } catch (PartyNotFound ex) {
            throw new PartyNotFoundException(String.format("Party not found, partyId='%s'", partyId), ex);
        } catch (PartyNotExistsYet ex) {
            throw new PartyNotFoundException(String.format("Party not exists at this time, partyId='%s', timestamp='%s'", partyId, timestamp), ex);
        } catch (TException ex) {
            throw new RuntimeException("Exception with get party from hg", ex);
        }
    }

    @Override
    public Map<String, String> getShopUrls(String partyId, String contractId, Instant timestamp) throws PartyNotFoundException, ContractNotFoundException {
        Party party;
        try {
            party = partyManagementClient.checkout(userInfo, partyId, PartyRevisionParam.timestamp(TypeUtil.temporalToString(timestamp)));
        } catch (PartyNotFound ex) {
            throw new PartyNotFoundException(String.format("Party not found, partyId='%s'", partyId), ex);
        } catch (ContractNotFound ex) {
            throw new ContractNotFoundException(String.format("Contract not found, contractId='%s'", contractId), ex);
        } catch (TException ex) {
            throw new RuntimeException("Exception with get party from hg", ex);
        }
        Map<String, String> shopUrls = new HashMap<>();
        party.getShops().forEach((id, shop) -> {
            if (shop.getLocation().isSetUrl()) {
                shopUrls.put(id, shop.getLocation().getUrl());
            }
        });
        return shopUrls;
    }

}
