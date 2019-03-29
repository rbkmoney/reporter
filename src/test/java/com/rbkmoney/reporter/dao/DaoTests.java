package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.domain.tables.pojos.*;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.util.json.FinalCashFlow;
import com.rbkmoney.reporter.util.json.FinalCashFlow.FinalCashFlowPosting;
import com.rbkmoney.reporter.util.json.FinalCashFlow.FinalCashFlowPosting.FinalCashFlowAccount.CashFlowAccount.MerchantCashFlowAccount;
import com.rbkmoney.reporter.util.json.FinalCashFlow.FinalCashFlowPosting.FinalCashFlowAccount.CashFlowAccount.ProviderCashFlowAccount;
import com.rbkmoney.reporter.util.json.FinalCashFlow.FinalCashFlowPosting.FinalCashFlowAccount.CashFlowAccount.SystemCashFlowAccount;
import com.rbkmoney.reporter.util.json.PayoutSummary;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.rbkmoney.reporter.util.json.FinalCashFlowUtil.getCashFlowPosting;
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

    @Autowired
    private PayoutDao payoutDao;

    @Test
    public void adjustmentDaoTest() throws DaoException {
        Adjustment adjustment = random(Adjustment.class, "adjustmentCashFlow", "adjustmentCashFlowInverseOld");
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
        Payment payment = random(Payment.class, "paymentCashFlow");
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
        Refund refund = random(Refund.class, "refundCashFlow");
        refund.setId(null);
        refund.setCurrent(true);
        Long id = refundDao.save(refund);
        refund.setId(id);
        assertEquals(refund, refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId()));
        refundDao.updateNotCurrent(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId());
        assertNull(refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId()));
    }

    @Test
    public void payoutDaoTest() throws DaoException {
        Payout payout = random(Payout.class, "payoutCashFlow", "payoutSummary");
        payout.setId(null);
        payout.setCurrent(true);
        Long id = payoutDao.save(payout);
        payout.setId(id);
        assertEquals(payout, payoutDao.get(payout.getPayoutId()));
        payoutDao.updateNotCurrent(payout.getPayoutId());
        assertNull(payoutDao.get(payout.getPayoutId()));
    }

    @Test
    public void jsonbTest() throws DaoException {
        FinalCashFlow finalCashFlow = getFinalCashFlow();

        Adjustment adjustment = random(Adjustment.class, "adjustmentCashFlow", "adjustmentCashFlowInverseOld");
        adjustment.setId(null);
        adjustment.setCurrent(true);
        adjustment.setAdjustmentCashFlow(finalCashFlow);
        Long id = adjustmentDao.save(adjustment);
        adjustment.setId(id);
        assertEquals(adjustment.getAdjustmentCashFlow().getCashFlows().get(0).getSource().getAccountId(), adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()).getAdjustmentCashFlow().getCashFlows().get(0).getSource().getAccountId());

        PayoutSummary payoutSummary = getPayoutSummary();
        Payout payout = random(Payout.class, "payoutCashFlow", "payoutSummary");
        payout.setId(null);
        payout.setCurrent(true);
        payout.setPayoutSummary(payoutSummary);
        id = payoutDao.save(payout);
        payout.setId(id);
        assertEquals(payout.getPayoutSummary().getPayoutSummaryItems().get(0).getFromTime(), payoutDao.get(payout.getPayoutId()).getPayoutSummary().getPayoutSummaryItems().get(0).getFromTime());
    }

    private FinalCashFlow getFinalCashFlow() {
        List<FinalCashFlowPosting> postings = new ArrayList<>();
        postings.add(getCashFlowPosting(13444L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 6L, SystemCashFlowAccount.SystemCashFlowAccountType.SETTLEMENT, 1425L, "RUB"));
        postings.add(getCashFlowPosting(13444L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 6L, SystemCashFlowAccount.SystemCashFlowAccountType.SETTLEMENT, 1425L, "RUB"));
        postings.add(getCashFlowPosting(5681L, ProviderCashFlowAccount.ProviderCashFlowAccountType.SETTLEMENT, 13444L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 50000L, "RUB"));
        postings.add(getCashFlowPosting(5681L, ProviderCashFlowAccount.ProviderCashFlowAccountType.SETTLEMENT, 13444L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 50000L, "RUB"));
        postings.add(getCashFlowPosting(6L, SystemCashFlowAccount.SystemCashFlowAccountType.SETTLEMENT, 5681L, ProviderCashFlowAccount.ProviderCashFlowAccountType.SETTLEMENT, 1100L, "RUB"));

        FinalCashFlow finalCashFlow = new FinalCashFlow();
        finalCashFlow.setCashFlows(postings);
        return finalCashFlow;
    }

    private PayoutSummary getPayoutSummary() {
        List<PayoutSummary.PayoutSummaryItem> payoutSummaryItems = new ArrayList<>();
        payoutSummaryItems.add(getPayoutSummaryItem(0L, 0L, 0, PayoutSummary.PayoutSummaryItem.OperationType.PAYMENT, LocalDateTime.now(), LocalDateTime.now(), "RUB"));
        payoutSummaryItems.add(getPayoutSummaryItem(0L, 0L, 0, PayoutSummary.PayoutSummaryItem.OperationType.PAYMENT, LocalDateTime.now(), LocalDateTime.now(), "RUB"));
        payoutSummaryItems.add(getPayoutSummaryItem(0L, 0L, 0, PayoutSummary.PayoutSummaryItem.OperationType.PAYMENT, LocalDateTime.now(), LocalDateTime.now(), "RUB"));
        payoutSummaryItems.add(getPayoutSummaryItem(0L, 0L, 0, PayoutSummary.PayoutSummaryItem.OperationType.PAYMENT, LocalDateTime.now(), LocalDateTime.now(), "RUB"));

        PayoutSummary payoutSummary = new PayoutSummary();
        payoutSummary.setPayoutSummaryItems(payoutSummaryItems);
        return payoutSummary;
    }

    private PayoutSummary.PayoutSummaryItem getPayoutSummaryItem(long amount, long fee, int count, PayoutSummary.PayoutSummaryItem.OperationType operationType, LocalDateTime toTime, LocalDateTime fromTime, String symbolicCode) {
        PayoutSummary.PayoutSummaryItem.CurrencyRef currency = new PayoutSummary.PayoutSummaryItem.CurrencyRef();
        currency.setSymbolicCode(symbolicCode);

        PayoutSummary.PayoutSummaryItem payoutSummaryItem = new PayoutSummary.PayoutSummaryItem();
        payoutSummaryItem.setAmount(amount);
        payoutSummaryItem.setFee(fee);
        payoutSummaryItem.setCount(count);
        payoutSummaryItem.setOperationType(operationType);
        payoutSummaryItem.setToTime(toTime);
        payoutSummaryItem.setFromTime(fromTime);
        payoutSummaryItem.setCurrency(currency);
        return payoutSummaryItem;
    }
}
