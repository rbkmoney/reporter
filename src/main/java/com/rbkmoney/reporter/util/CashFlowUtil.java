package com.rbkmoney.reporter.util;

import com.rbkmoney.reporter.util.FinalCashFlow.FinalCashFlowPosting;
import com.rbkmoney.reporter.util.FinalCashFlow.FinalCashFlowPosting.Cash;
import com.rbkmoney.reporter.util.FinalCashFlow.FinalCashFlowPosting.FinalCashFlowAccount;
import com.rbkmoney.reporter.util.FinalCashFlow.FinalCashFlowPosting.FinalCashFlowAccount.CashFlowAccount.*;

import java.util.List;
import java.util.stream.Collectors;

public class CashFlowUtil {

    public static FinalCashFlow toDtoFinalCashFlow(List<com.rbkmoney.damsel.domain.FinalCashFlowPosting> finalCashFlowPostings) {
        List<FinalCashFlowPosting> dtoFinalCashFlowPostings = finalCashFlowPostings.stream()
                .map(
                        finalCashFlowPosting -> {
                            long sourceAccountId = finalCashFlowPosting.getSource().getAccountId();
                            CashFlowAccountType accountTypeSource = getCashFlowAccountType(finalCashFlowPosting.getSource().getAccountType());
                            long destinationAccountId = finalCashFlowPosting.getDestination().getAccountId();
                            CashFlowAccountType accountTypeDestination = getCashFlowAccountType(finalCashFlowPosting.getDestination().getAccountType());
                            long amount = finalCashFlowPosting.getVolume().getAmount();
                            String symbolicCode = finalCashFlowPosting.getVolume().getCurrency().getSymbolicCode();
                            String details = finalCashFlowPosting.getDetails();
                            return getCashFlowPosting(sourceAccountId, accountTypeSource, destinationAccountId, accountTypeDestination, amount, symbolicCode, details);
                        }
                )
                .collect(Collectors.toList());
        FinalCashFlow finalCashFlow = new FinalCashFlow();
        finalCashFlow.setCashFlows(dtoFinalCashFlowPostings);
        return finalCashFlow;
    }

    public static FinalCashFlowPosting getCashFlowPosting(long accountIdSource, CashFlowAccountType accountTypeSource, long accountIdDestination, CashFlowAccountType accountTypeDestination, long amount, String symbolicCode) {
        return getCashFlowPosting(accountIdSource, accountTypeSource, accountIdDestination, accountTypeDestination, amount, symbolicCode, null);
    }

    public static FinalCashFlowPosting getCashFlowPosting(long accountIdSource, CashFlowAccountType accountTypeSource, long accountIdDestination, CashFlowAccountType accountTypeDestination, long amount, String symbolicCode, String details) {
        FinalCashFlowPosting posting = new FinalCashFlowPosting();
        posting.setSource(getFinalCashFlowAccount(accountIdSource, accountTypeSource));
        posting.setDestination(getFinalCashFlowAccount(accountIdDestination, accountTypeDestination));
        posting.setVolume(getCash(amount, symbolicCode));
        posting.setDetails(details);
        return posting;
    }

    private static CashFlowAccountType getCashFlowAccountType(com.rbkmoney.damsel.domain.CashFlowAccount accountType) {
        CashFlowAccountType cashFlowAccountType;
        if (accountType.isSetMerchant()) {
            String name = accountType.getMerchant().name().toUpperCase();
            cashFlowAccountType = MerchantCashFlowAccount.MerchantCashFlowAccountType.valueOf(name);
        } else if (accountType.isSetProvider()) {
            String name = accountType.getProvider().name().toUpperCase();
            cashFlowAccountType = ProviderCashFlowAccount.ProviderCashFlowAccountType.valueOf(name);
        } else if (accountType.isSetSystem()) {
            String name = accountType.getSystem().name().toUpperCase();
            cashFlowAccountType = SystemCashFlowAccount.SystemCashFlowAccountType.valueOf(name);
        } else if (accountType.isSetExternal()) {
            String name = accountType.getExternal().name().toUpperCase();
            cashFlowAccountType = ExternalCashFlowAccount.ExternalCashFlowAccountType.valueOf(name);
        } else if (accountType.isSetWallet()) {
            String name = accountType.getWallet().name().toUpperCase();
            cashFlowAccountType = WalletCashFlowAccount.WalletCashFlowAccountType.valueOf(name);
        } else {
            throw new IllegalArgumentException("Illegal fistful cash flow account type: " + accountType);
        }
        return cashFlowAccountType;
    }

    private static FinalCashFlowAccount getFinalCashFlowAccount(long accountId, CashFlowAccountType accountType) {
        FinalCashFlowAccount finalCashFlowAccount = new FinalCashFlowAccount();
        finalCashFlowAccount.setAccountId(accountId);
        if (accountType instanceof MerchantCashFlowAccount.MerchantCashFlowAccountType) {
            finalCashFlowAccount.setAccountType(getCashFlowAccount((MerchantCashFlowAccount.MerchantCashFlowAccountType) accountType));
        } else if (accountType instanceof ProviderCashFlowAccount.ProviderCashFlowAccountType) {
            finalCashFlowAccount.setAccountType(getCashFlowAccount((ProviderCashFlowAccount.ProviderCashFlowAccountType) accountType));
        } else if (accountType instanceof SystemCashFlowAccount.SystemCashFlowAccountType) {
            finalCashFlowAccount.setAccountType(getCashFlowAccount((SystemCashFlowAccount.SystemCashFlowAccountType) accountType));
        } else if (accountType instanceof ExternalCashFlowAccount.ExternalCashFlowAccountType) {
            finalCashFlowAccount.setAccountType(getCashFlowAccount((ExternalCashFlowAccount.ExternalCashFlowAccountType) accountType));
        } else if (accountType instanceof WalletCashFlowAccount.WalletCashFlowAccountType) {
            finalCashFlowAccount.setAccountType(getCashFlowAccount((WalletCashFlowAccount.WalletCashFlowAccountType) accountType));
        }
        return finalCashFlowAccount;
    }

    private static MerchantCashFlowAccount getCashFlowAccount(MerchantCashFlowAccount.MerchantCashFlowAccountType flowAccountEnum) {
        MerchantCashFlowAccount account = new MerchantCashFlowAccount();
        account.setMerchant(flowAccountEnum);
        return account;
    }

    private static ProviderCashFlowAccount getCashFlowAccount(ProviderCashFlowAccount.ProviderCashFlowAccountType flowAccountEnum) {
        ProviderCashFlowAccount account = new ProviderCashFlowAccount();
        account.setProvider(flowAccountEnum);
        return account;
    }

    private static SystemCashFlowAccount getCashFlowAccount(SystemCashFlowAccount.SystemCashFlowAccountType flowAccountEnum) {
        SystemCashFlowAccount account = new SystemCashFlowAccount();
        account.setSystem(flowAccountEnum);
        return account;
    }

    private static ExternalCashFlowAccount getCashFlowAccount(ExternalCashFlowAccount.ExternalCashFlowAccountType flowAccountEnum) {
        ExternalCashFlowAccount account = new ExternalCashFlowAccount();
        account.setExternal(flowAccountEnum);
        return account;
    }

    private static WalletCashFlowAccount getCashFlowAccount(WalletCashFlowAccount.WalletCashFlowAccountType flowAccountEnum) {
        WalletCashFlowAccount account = new WalletCashFlowAccount();
        account.setWallet(flowAccountEnum);
        return account;
    }

    private static Cash getCash(Long amount, String symbolicCode) {
        Cash.CurrencyRef currencyRef = new Cash.CurrencyRef();
        currencyRef.setSymbolicCode(symbolicCode);

        Cash cash = new Cash();
        cash.setAmount(amount);
        cash.setCurrency(currencyRef);
        return cash;
    }
}
