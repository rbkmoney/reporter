package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.Party;
import com.rbkmoney.damsel.domain.RussianLegalEntity;
import com.rbkmoney.damsel.domain.Shop;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.model.PartyRepresentation;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.util.Objects;

/**
 * Created by tolkonepiu on 17/07/2017.
 */
@Service
public class PartyService {

    private final UserInfo userInfo = new UserInfo("admin", UserType.internal_user(new InternalUser()));

    @Autowired
    PartyManagementSrv.Iface partyManagementSrv;

    public PartyRepresentation getPartyRepresentation(String partyId, String shopId, Instant timestamp) throws RuntimeException {
        try {
            Party party = partyManagementSrv.checkout(userInfo, partyId, TypeUtil.temporalToString(timestamp));


            PartyRepresentation partyRepresentation = new PartyRepresentation();
            partyRepresentation.setMerchantId(partyId);

            Shop shop = party.getShops().get(shopId);
            Objects.requireNonNull(shop, "shop must be not null");

            String contractId = shop.getContractId();
            Contract contract = party.getContracts().get(contractId);
            Objects.requireNonNull(contract, "contract must be not null");

            partyRepresentation.setMerchantContractId(contractId);
            partyRepresentation.setMerchantContractCreatedAt(
                    Date.from(TypeUtil.stringToInstant(contract.getCreatedAt()))
            );

            partyRepresentation.setMerchantContractId(contract.getId());
            if (contract.isSetContractor()
                    && contract.getContractor().isSetLegalEntity()
                    && contract.getContractor().getLegalEntity().isSetRussianLegalEntity()) {
                RussianLegalEntity entity = contract.getContractor()
                        .getLegalEntity()
                        .getRussianLegalEntity();
                partyRepresentation.setMerchantName(entity.getRegisteredName());
                partyRepresentation.setMerchantRepresentativeFullName(entity.getRepresentativeFullName());
                partyRepresentation.setMerchantRepresentativePosition(entity.getRepresentativePosition());
                partyRepresentation.setMerchantRepresentativeDocument(entity.getRepresentativeDocument());
            }

            return partyRepresentation;
        } catch (PartyNotFound ex) {
            throw new RuntimeException(String.format("Party not found, partyId='%s'", partyId), ex);
        } catch (PartyNotExistsYet ex) {
            throw new RuntimeException(String.format("Party not exists at this time, partyId='%s', timestamp='%s'", partyId, timestamp), ex);
        } catch (TException ex) {
            throw new RuntimeException("Exception with get party from hg", ex);
        }
    }

}
