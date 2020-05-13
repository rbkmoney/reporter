package com.rbkmoney.reporter.handler.payout;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payout_processing.*;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.dao.query.PayoutQueryTemplator;
import com.rbkmoney.reporter.domain.enums.PayoutAccountType;
import com.rbkmoney.reporter.domain.enums.PayoutStatus;
import com.rbkmoney.reporter.domain.enums.PayoutType;
import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.domain.tables.pojos.PayoutState;
import com.rbkmoney.reporter.service.BatchService;
import com.rbkmoney.reporter.util.DamselUtil;
import com.rbkmoney.sink.common.handle.stockevent.event.change.PayoutChangeEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutCreatedChangeEventHandler implements PayoutChangeEventHandler {

    private final PayoutQueryTemplator payoutQueryTemplator;
    private final BatchService batchService;

    @Override
    public void handle(PayoutChange payload, StockEvent baseEvent, Integer changeId) {
        Event event = baseEvent.getSourceEvent().getPayoutEvent();

        var damselPayout = payload.getPayoutCreated().getPayout();
        var damselPayoutType = damselPayout.getType();
        String payoutId = event.getSource().getPayoutId();

        log.info("Start payout created handling, payoutId={}", payoutId);

        Payout payout = getPayout(damselPayout, damselPayoutType, payoutId);

        PayoutState payoutState = getPayoutState(event, damselPayout, payoutId);

        Query savePayoutQuery = payoutQueryTemplator.getSavePayoutQuery(payout);
        Query savePayoutStateQuery = payoutQueryTemplator.getSavePayoutStateQuery(payoutState);

        List<Query> queries = List.of(savePayoutQuery, savePayoutStateQuery);

        batchService.save(queries);

        log.info("Payout has been created, payoutId={}", payoutId);
    }

    @Override
    public boolean accept(PayoutChange payload) {
        return payload.isSetPayoutCreated();
    }

    private Payout getPayout(com.rbkmoney.damsel.payout_processing.Payout damselPayout,
                             com.rbkmoney.damsel.payout_processing.PayoutType damselPayoutType,
                             String payoutId) {
        Payout payout = new Payout();
        payout.setPartyId(UUID.fromString(damselPayout.getPartyId()));
        payout.setPartyShopId(damselPayout.getShopId());
        payout.setPayoutId(payoutId);
        payout.setContractId(damselPayout.getContractId());
        payout.setCreatedAt(TypeUtil.stringToLocalDateTime(damselPayout.getCreatedAt()));
        payout.setAmount(damselPayout.getAmount());
        payout.setFee(damselPayout.getFee());
        payout.setCurrencyCode(damselPayout.getCurrency().getSymbolicCode());
        payout.setType(TBaseUtil.unionFieldToEnum(damselPayoutType, PayoutType.class));
        if (damselPayoutType.isSetWallet()) {
            payout.setWalletId(damselPayoutType.getWallet().getWalletId());
        } else if (damselPayoutType.isSetBankAccount()) {
            fillPayoutAccount(damselPayoutType, payout);
        }
        if (damselPayout.isSetSummary()) {
            List<PayoutSummaryItem> payoutSummaryItems = damselPayout.getSummary().stream()
                    .filter(payoutSummaryItem -> payoutSummaryItem.getOperationType() != OperationType.adjustment)
                    .collect(Collectors.toList());
            payout.setSummary(DamselUtil.toPayoutSummaryStatString(payoutSummaryItems));
        }
        return payout;
    }

    private void fillPayoutAccount(com.rbkmoney.damsel.payout_processing.PayoutType damselPayoutType,
                                   Payout payout) {
        PayoutAccount payoutAccount = damselPayoutType.getBankAccount();

        if (payoutAccount.isSetRussianPayoutAccount()) {
            fillRussianPayoutAccount(payoutAccount, payout);
        } else if (payoutAccount.isSetInternationalPayoutAccount()) {
            fillInternationPayoutAccount(payoutAccount, payout);
        }
    }

    private void fillRussianPayoutAccount(PayoutAccount payoutAccount,
                                          Payout payout) {
        RussianPayoutAccount account = payoutAccount.getRussianPayoutAccount();
        RussianBankAccount bankAccount = account.getBankAccount();
        LegalAgreement legalAgreement = account.getLegalAgreement();

        payout.setAccountType(PayoutAccountType.RUSSIAN_PAYOUT_ACCOUNT);
        payout.setAccountBankId(bankAccount.getAccount());
        payout.setAccountBankCorrId(bankAccount.getBankPostAccount());
        payout.setAccountBankLocalCode(bankAccount.getBankBik());
        payout.setAccountBankName(bankAccount.getBankName());
        payout.setAccountPurpose(account.getPurpose());
        payout.setAccountInn(account.getInn());
        payout.setAccountLegalAgreementId(legalAgreement.getLegalAgreementId());
        payout.setAccountLegalAgreementSignedAt(TypeUtil.stringToLocalDateTime(legalAgreement.getSignedAt()));
    }

    private void fillInternationPayoutAccount(PayoutAccount payoutAccount,
                                              Payout payout) {
        InternationalPayoutAccount account = payoutAccount.getInternationalPayoutAccount();
        InternationalLegalEntity legalEntity = account.getLegalEntity();
        InternationalBankAccount bankAccount = account.getBankAccount();
        LegalAgreement legalAgreement = account.getLegalAgreement();

        payout.setAccountType(PayoutAccountType.INTERNATIONAL_PAYOUT_ACCOUNT);
        payout.setAccountTradingName(legalEntity.getTradingName());
        payout.setAccountLegalName(legalEntity.getLegalName());
        payout.setAccountActualAddress(legalEntity.getActualAddress());
        payout.setAccountRegisteredAddress(legalEntity.getRegisteredAddress());
        payout.setAccountRegisteredNumber(legalEntity.getRegisteredNumber());
        payout.setAccountPurpose(account.getPurpose());
        payout.setAccountBankId(bankAccount.getAccountHolder());
        payout.setAccountBankIban(bankAccount.getIban());
        payout.setAccountBankNumber(bankAccount.getNumber());
        if (bankAccount.isSetBank()) {
            fillBankInfo(bankAccount.getBank(), payout);
        }
        if (bankAccount.isSetCorrespondentAccount()) {
            fillCorrespondentAccount(bankAccount.getCorrespondentAccount(), payout);
        }
        payout.setAccountLegalAgreementId(legalAgreement.getLegalAgreementId());
        payout.setAccountLegalAgreementSignedAt(TypeUtil.stringToLocalDateTime(legalAgreement.getSignedAt()));
    }

    private void fillCorrespondentAccount(InternationalBankAccount correspondentAccount,
                                          Payout payout) {
        payout.setInternationalCorrespondentAccountBankAccount(correspondentAccount.getAccountHolder());
        payout.setInternationalCorrespondentAccountBankNumber(correspondentAccount.getNumber());
        payout.setInternationalCorrespondentAccountBankIban(correspondentAccount.getIban());
        if (correspondentAccount.isSetBank()) {
            InternationalBankDetails corrBankDetails = correspondentAccount.getBank();

            payout.setInternationalCorrespondentAccountBankName(corrBankDetails.getName());
            payout.setInternationalCorrespondentAccountBankAddress(corrBankDetails.getAddress());
            payout.setInternationalCorrespondentAccountBankBic(corrBankDetails.getBic());
            payout.setInternationalCorrespondentAccountBankAbaRtn(corrBankDetails.getAbaRtn());
            if (corrBankDetails.isSetCountry()) {
                String country = corrBankDetails.getCountry().toString();

                payout.setInternationalCorrespondentAccountBankCountryCode(country);
            }
        }
    }

    private void fillBankInfo(InternationalBankDetails bankDetails,
                              Payout payout) {
        payout.setAccountBankName(bankDetails.getName());
        payout.setAccountBankAddress(bankDetails.getAddress());
        payout.setAccountBankBic(bankDetails.getBic());
        payout.setAccountBankAbaRtn(bankDetails.getAbaRtn());
        if (bankDetails.isSetCountry()) {
            String country = bankDetails.getCountry().toString();

            payout.setAccountBankCountryCode(country);
        }
    }

    private PayoutState getPayoutState(Event event,
                                       com.rbkmoney.damsel.payout_processing.Payout damselPayout,
                                       String payoutId) {
        PayoutState payoutState = new PayoutState();
        payoutState.setEventId(event.getId());
        payoutState.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payoutState.setPayoutId(payoutId);
        payoutState.setStatus(TBaseUtil.unionFieldToEnum(damselPayout.getStatus(), PayoutStatus.class));
        return payoutState;
    }
}

