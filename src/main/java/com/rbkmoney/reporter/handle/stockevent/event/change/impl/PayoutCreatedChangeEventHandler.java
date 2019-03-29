package com.rbkmoney.reporter.handle.stockevent.event.change.impl;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payout_processing.*;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.enums.PayoutStatus;
import com.rbkmoney.reporter.domain.enums.PayoutType;
import com.rbkmoney.reporter.domain.enums.*;
import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.handle.stockevent.event.change.PayoutChangeEventsHandler;
import com.rbkmoney.reporter.service.PayoutService;
import com.rbkmoney.reporter.util.json.FinalCashFlowUtil;
import com.rbkmoney.reporter.util.json.PayoutSummaryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayoutCreatedChangeEventHandler implements PayoutChangeEventsHandler {

    private final PayoutService payoutService;

    @Override
    public boolean accept(PayoutChange specific) {
        return specific.isSetPayoutCreated();
    }

    @Override
    public void handle(PayoutChange specific, StockEvent stockEvent) {
        Event event = stockEvent.getSourceEvent().getPayoutEvent();

        com.rbkmoney.damsel.payout_processing.Payout damselPayout = specific.getPayoutCreated().getPayout();
        com.rbkmoney.damsel.payout_processing.PayoutType damselPayoutType = damselPayout.getType();
        String payoutId = event.getSource().getPayoutId();

        log.info("Start payout created handling, payoutId={}", payoutId);

        Payout payout = new Payout();
        payout.setEventId(event.getId());
        payout.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payout.setEventType(PayoutEventType.PAYOUT_CREATED);
        payout.setEventCategory(PayoutEventCategory.PAYOUT);
//        payout.setSequenceId(event.getSequence());
        payout.setPayoutId(payoutId);
        payout.setPartyId(UUID.fromString(damselPayout.getPartyId()));
        payout.setPartyShopId(damselPayout.getShopId());
        payout.setContractId(damselPayout.getContractId());
        payout.setPayoutCreatedAt(TypeUtil.stringToLocalDateTime(damselPayout.getCreatedAt()));
        payout.setPayoutStatus(TBaseUtil.unionFieldToEnum(damselPayout.getStatus(), PayoutStatus.class));
        payout.setPayoutAmount(damselPayout.getAmount());
        payout.setPayoutFee(damselPayout.getFee());
        payout.setPayoutCurrencyCode(damselPayout.getCurrency().getSymbolicCode());
        payout.setPayoutCashFlow(FinalCashFlowUtil.toDtoFinalCashFlow(damselPayout.getPayoutFlow()));
        payout.setPayoutType(TBaseUtil.unionFieldToEnum(damselPayoutType, PayoutType.class));
        if (damselPayoutType.isSetWallet()) {
            payout.setPayoutWalletId(damselPayoutType.getWallet().getWalletId());
        } else if (damselPayoutType.isSetBankAccount()) {
            PayoutAccount payoutAccount = damselPayoutType.getBankAccount();

            if (payoutAccount.isSetRussianPayoutAccount()) {
                RussianPayoutAccount account = payoutAccount.getRussianPayoutAccount();
                RussianBankAccount bankAccount = account.getBankAccount();
                LegalAgreement legalAgreement = account.getLegalAgreement();

                payout.setPayoutAccountType(PayoutAccountType.RUSSIAN_PAYOUT_ACCOUNT);
                payout.setPayoutAccountBankId(bankAccount.getAccount());
                payout.setPayoutAccountBankCorrId(bankAccount.getBankPostAccount());
                payout.setPayoutAccountBankLocalCode(bankAccount.getBankBik());
                payout.setPayoutAccountBankName(bankAccount.getBankName());
                payout.setPayoutAccountPurpose(account.getPurpose());
                payout.setPayoutAccountInn(account.getInn());
                payout.setPayoutAccountLegalAgreementId(legalAgreement.getLegalAgreementId());
                payout.setPayoutAccountLegalAgreementSignedAt(TypeUtil.stringToLocalDateTime(legalAgreement.getSignedAt()));
            } else if (payoutAccount.isSetInternationalPayoutAccount()) {
                InternationalPayoutAccount account = payoutAccount.getInternationalPayoutAccount();
                InternationalLegalEntity legalEntity = account.getLegalEntity();
                InternationalBankAccount bankAccount = account.getBankAccount();
                LegalAgreement legalAgreement = account.getLegalAgreement();

                payout.setPayoutAccountType(PayoutAccountType.INTERNATIONAL_PAYOUT_ACCOUNT);
                payout.setPayoutAccountTradingName(legalEntity.getTradingName());
                payout.setPayoutAccountLegalName(legalEntity.getLegalName());
                payout.setPayoutAccountActualAddress(legalEntity.getActualAddress());
                payout.setPayoutAccountRegisteredAddress(legalEntity.getRegisteredAddress());
                payout.setPayoutAccountRegisteredNumber(legalEntity.getRegisteredNumber());
                payout.setPayoutAccountPurpose(account.getPurpose());
                payout.setPayoutAccountBankId(bankAccount.getAccountHolder());
                payout.setPayoutAccountBankIban(bankAccount.getIban());
                payout.setPayoutAccountBankNumber(bankAccount.getNumber());
                if (bankAccount.isSetBank()) {
                    InternationalBankDetails bankDetails = bankAccount.getBank();

                    payout.setPayoutAccountBankName(bankDetails.getName());
                    payout.setPayoutAccountBankAddress(bankDetails.getAddress());
                    payout.setPayoutAccountBankBic(bankDetails.getBic());
                    payout.setPayoutAccountBankAbaRtn(bankDetails.getAbaRtn());
                    if (bankDetails.isSetCountry()) {
                        String country = bankDetails.getCountry().toString();

                        payout.setPayoutAccountBankCountryCode(country);
                    }
                }
                if (bankAccount.isSetCorrespondentAccount()) {
                    InternationalBankAccount correspondentAccount = bankAccount.getCorrespondentAccount();

                    payout.setPayoutInternationalCorrespondentAccountBankAccount(correspondentAccount.getAccountHolder());
                    payout.setPayoutInternationalCorrespondentAccountBankNumber(correspondentAccount.getNumber());
                    payout.setPayoutInternationalCorrespondentAccountBankIban(correspondentAccount.getIban());
                    if (correspondentAccount.isSetBank()) {
                        InternationalBankDetails corrBankDetails = correspondentAccount.getBank();

                        payout.setPayoutInternationalCorrespondentAccountBankName(corrBankDetails.getName());
                        payout.setPayoutInternationalCorrespondentAccountBankAddress(corrBankDetails.getAddress());
                        payout.setPayoutInternationalCorrespondentAccountBankBic(corrBankDetails.getBic());
                        payout.setPayoutInternationalCorrespondentAccountBankAbaRtn(corrBankDetails.getAbaRtn());
                        if (corrBankDetails.isSetCountry()) {
                            String country = corrBankDetails.getCountry().toString();

                            payout.setPayoutInternationalCorrespondentAccountBankCountryCode(country);
                        }
                    }
                }
                payout.setPayoutAccountLegalAgreementId(legalAgreement.getLegalAgreementId());
                payout.setPayoutAccountLegalAgreementSignedAt(TypeUtil.stringToLocalDateTime(legalAgreement.getSignedAt()));
            }
        }
        if (damselPayout.isSetSummary()) {
            payout.setPayoutSummary(PayoutSummaryUtil.toDtoPayoutSummary(damselPayout.getSummary()));
        }

        payoutService.save(payout);
        log.info("Payout has been created, payoutId={}", payoutId);
    }
}
