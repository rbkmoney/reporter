package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Invoice;
import com.rbkmoney.reporter.domain.tables.pojos.Payment;
import com.rbkmoney.reporter.domain.tables.pojos.Refund;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.util.FinalCashFlow;
import com.rbkmoney.reporter.util.FinalCashFlow.FinalCashFlowPosting;
import com.rbkmoney.reporter.util.FinalCashFlow.FinalCashFlowPosting.Cash;
import com.rbkmoney.reporter.util.FinalCashFlow.FinalCashFlowPosting.FinalCashFlowAccount;
import com.rbkmoney.reporter.util.FinalCashFlow.FinalCashFlowPosting.FinalCashFlowAccount.CashFlowAccount.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DaoTests extends AbstractAppDaoTests {

    @Autowired
    private AdjustmentDao adjustmentDao;

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private RefundDao refundDao;

    @Test
    public void adjustmentDaoTest() throws DaoException {
        Adjustment adjustment = random(Adjustment.class, "adjustmentProviderCashFlow", "adjustmentExternalCashFlow");
        adjustment.setId(null);
        adjustment.setCurrent(true);
        Long id = adjustmentDao.save(adjustment);
        adjustment.setId(id);
        assertEquals(adjustment, adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()));
        adjustmentDao.updateNotCurrent(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId());
        assertNull(adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()));
    }

    @Test
    public void invoiceDaoTest() throws DaoException {
        Invoice invoice = random(Invoice.class);
        invoice.setId(null);
        invoice.setCurrent(true);
        Long id = invoiceDao.save(invoice);
        invoice.setId(id);
        assertEquals(invoice, invoiceDao.get(invoice.getInvoiceId()));
        invoiceDao.updateNotCurrent(invoice.getInvoiceId());
        assertNull(invoiceDao.get(invoice.getInvoiceId()));
    }

    @Test
    public void paymentDaoTest() throws DaoException {
        Payment payment = random(Payment.class, "paymentProviderCashFlow", "paymentExternalCashFlow");
        payment.setId(null);
        payment.setCurrent(true);
        Long id = paymentDao.save(payment);
        payment.setId(id);
        assertEquals(payment, paymentDao.get(payment.getInvoiceId(), payment.getPaymentId()));
        paymentDao.updateNotCurrent(payment.getInvoiceId(), payment.getPaymentId());
        assertNull(paymentDao.get(payment.getInvoiceId(), payment.getPaymentId()));
    }

    @Test
    public void refundDaoTest() throws DaoException {
        Refund refund = random(Refund.class, "refundProviderCashFlow", "refundExternalCashFlow");
        refund.setId(null);
        refund.setCurrent(true);
        Long id = refundDao.save(refund);
        refund.setId(id);
        assertEquals(refund, refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId()));
        refundDao.updateNotCurrent(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId());
        assertNull(refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId()));
    }

    @Test
    public void jsonbTest() throws DaoException {
        FinalCashFlow finalCashFlow = new FinalCashFlow();
        finalCashFlow.setCashFlows(getPostings());

        Adjustment adjustment = random(Adjustment.class, "adjustmentProviderCashFlow", "adjustmentExternalCashFlow");
        adjustment.setId(null);
        adjustment.setCurrent(true);
        adjustment.setAdjustmentExternalCashFlow(finalCashFlow);
        Long id = adjustmentDao.save(adjustment);
        adjustment.setId(id);
        assertEquals(adjustment.getAdjustmentExternalCashFlow().getCashFlows().get(0).getSource().getAccountId(), adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()).getAdjustmentExternalCashFlow().getCashFlows().get(0).getSource().getAccountId());
    }

    private List<FinalCashFlowPosting> getPostings() {
        List<FinalCashFlowPosting> postings = new ArrayList<>();
        postings.add(getCashFlowPosting(13444L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 6L, SystemCashFlowAccount.SystemCashFlowAccountType.SETTLEMENT, 1425L, "RUB"));
        postings.add(getCashFlowPosting(13444L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 6L, SystemCashFlowAccount.SystemCashFlowAccountType.SETTLEMENT, 1425L, "RUB"));
        postings.add(getCashFlowPosting(5681L, ProviderCashFlowAccount.ProviderCashFlowAccountType.SETTLEMENT, 13444L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 50000L, "RUB"));
        postings.add(getCashFlowPosting(5681L, ProviderCashFlowAccount.ProviderCashFlowAccountType.SETTLEMENT, 13444L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 50000L, "RUB"));
        postings.add(getCashFlowPosting(6L, SystemCashFlowAccount.SystemCashFlowAccountType.SETTLEMENT, 5681L, ProviderCashFlowAccount.ProviderCashFlowAccountType.SETTLEMENT, 1100L, "RUB"));
        return postings;
    }

    private FinalCashFlowPosting getCashFlowPosting(long accountIdSource, CashFlowAccountType accountTypeSource, long accountIdDestination, CashFlowAccountType accountTypeDestination, long amount, String symbolicCode) {
        FinalCashFlowPosting posting = new FinalCashFlowPosting();
        posting.setSource(getFinalCashFlowAccount(accountIdSource, accountTypeSource));
        posting.setDestination(getFinalCashFlowAccount(accountIdDestination, accountTypeDestination));
        posting.setVolume(getCash(amount, symbolicCode));
        return posting;
    }

    private FinalCashFlowAccount getFinalCashFlowAccount(long accountId, CashFlowAccountType accountType) {
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

    private MerchantCashFlowAccount getCashFlowAccount(MerchantCashFlowAccount.MerchantCashFlowAccountType flowAccountEnum) {
        MerchantCashFlowAccount account = new MerchantCashFlowAccount();
        account.setMerchant(flowAccountEnum);
        return account;
    }

    private ProviderCashFlowAccount getCashFlowAccount(ProviderCashFlowAccount.ProviderCashFlowAccountType flowAccountEnum) {
        ProviderCashFlowAccount account = new ProviderCashFlowAccount();
        account.setProvider(flowAccountEnum);
        return account;
    }

    private SystemCashFlowAccount getCashFlowAccount(SystemCashFlowAccount.SystemCashFlowAccountType flowAccountEnum) {
        SystemCashFlowAccount account = new SystemCashFlowAccount();
        account.setSystem(flowAccountEnum);
        return account;
    }

    private ExternalCashFlowAccount getCashFlowAccount(ExternalCashFlowAccount.ExternalCashFlowAccountType flowAccountEnum) {
        ExternalCashFlowAccount account = new ExternalCashFlowAccount();
        account.setExternal(flowAccountEnum);
        return account;
    }

    private WalletCashFlowAccount getCashFlowAccount(WalletCashFlowAccount.WalletCashFlowAccountType flowAccountEnum) {
        WalletCashFlowAccount account = new WalletCashFlowAccount();
        account.setWallet(flowAccountEnum);
        return account;
    }

    private Cash getCash(Long amount, String symbolicCode) {
        Cash.CurrencyRef currencyRef = new Cash.CurrencyRef();
        currencyRef.setSymbolicCode(symbolicCode);

        Cash cash = new Cash();
        cash.setAmount(amount);
        cash.setCurrency(currencyRef);
        return cash;
    }
}
