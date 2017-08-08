package com.rbkmoney.reporter.service;

import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.Party;
import com.rbkmoney.damsel.domain.RussianLegalEntity;
import com.rbkmoney.damsel.domain.Shop;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.exception.PartyNotFoundException;
import com.rbkmoney.reporter.exception.ShopNotFoundException;
import com.rbkmoney.reporter.model.PartyModel;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final UserInfo userInfo = new UserInfo("admin", UserType.internal_user(new InternalUser()));

    private final PartyManagementSrv.Iface partyManagementSrv;

    @Autowired
    public PartyService(PartyManagementSrv.Iface partyManagementSrv) {
        this.partyManagementSrv = partyManagementSrv;
    }

    public PartyModel getPartyRepresentation(String partyId, String shopId, Instant timestamp) throws PartyNotFoundException, ShopNotFoundException, RuntimeException {
        try {
            Party party = partyManagementSrv.checkout(userInfo, partyId, TypeUtil.temporalToString(timestamp));

            PartyModel partyModel = new PartyModel();
            partyModel.setMerchantId(partyId);

            Shop shop = party.getShops().get(shopId);

            if (shop == null) {
                throw new ShopNotFoundException(
                        String.format("Shop not found, shopId='%s', partyId='%s', time='%s'", shopId, partyId, timestamp)
                );
            }

            String contractId = shop.getContractId();
            Contract contract = party.getContracts().get(contractId);
            if (contract == null) {
                throw new ShopNotFoundException(
                        String.format("Contract on shop not found, contractId='%s', shopId='%s', partyId='%s', time='%s'", contractId, shopId, partyId, timestamp)
                );
            }

            partyModel.setMerchantContractId(contractId);
            partyModel.setMerchantContractCreatedAt(
                    Date.from(TypeUtil.stringToInstant(contract.getCreatedAt()))
            );

            partyModel.setMerchantContractId(contract.getId());
            if (contract.isSetContractor()
                    && contract.getContractor().isSetLegalEntity()
                    && contract.getContractor().getLegalEntity().isSetRussianLegalEntity()) {
                RussianLegalEntity entity = contract.getContractor()
                        .getLegalEntity()
                        .getRussianLegalEntity();
                partyModel.setMerchantName(entity.getRegisteredName());
                partyModel.setMerchantRepresentativeFullName(entity.getRepresentativeFullName());
                partyModel.setMerchantRepresentativePosition(entity.getRepresentativePosition());
                partyModel.setMerchantRepresentativeDocument(entity.getRepresentativeDocument());
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

}
