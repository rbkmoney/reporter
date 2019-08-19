package com.rbkmoney.reporter.dao;

import com.rbkmoney.reporter.dao.mapper.dto.PaymentRegistryReportData;
import com.rbkmoney.reporter.dao.mapper.dto.RefundPaymentRegistryReportData;
import com.rbkmoney.reporter.domain.enums.InvoiceEventType;
import com.rbkmoney.reporter.domain.enums.PayoutEventCategory;
import com.rbkmoney.reporter.domain.tables.pojos.Adjustment;
import com.rbkmoney.reporter.domain.tables.pojos.Payout;
import com.rbkmoney.reporter.exception.DaoException;
import com.rbkmoney.reporter.util.json.FinalCashFlow;
import com.rbkmoney.reporter.util.json.FinalCashFlow.FinalCashFlowPosting;
import com.rbkmoney.reporter.util.json.FinalCashFlow.FinalCashFlowPosting.FinalCashFlowAccount.CashFlowAccount.MerchantCashFlowAccount;
import com.rbkmoney.reporter.util.json.FinalCashFlow.FinalCashFlowPosting.FinalCashFlowAccount.CashFlowAccount.ProviderCashFlowAccount;
import com.rbkmoney.reporter.util.json.FinalCashFlow.FinalCashFlowPosting.FinalCashFlowAccount.CashFlowAccount.SystemCashFlowAccount;
import com.rbkmoney.reporter.util.json.PayoutSummary;
import lombok.Data;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rbkmoney.geck.common.util.TypeUtil.stringToTemporal;
import static com.rbkmoney.geck.common.util.TypeUtil.toLocalDateTime;
import static com.rbkmoney.reporter.util.json.FinalCashFlowUtil.getCashFlowPosting;
import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.*;

public class CashFlowTests extends AbstractAppDaoTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdjustmentDao adjustmentDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private RefundDao refundDao;

    @Autowired
    private PayoutDao payoutDao;

    @Test
    public void jsonbMappingTest() throws DaoException {
        FinalCashFlow finalCashFlow = getFinalCashFlow();

        Adjustment adjustment = random(Adjustment.class, "adjustmentCashFlow", "adjustmentCashFlowInverseOld");
        adjustment.setId(null);
        adjustment.setAdjustmentCashFlow(finalCashFlow);
        Long id = adjustmentDao.save(adjustment);
        adjustment.setId(id);
        assertEquals(adjustment.getAdjustmentCashFlow().getCashFlows().get(0).getSource().getAccountId(), adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()).getAdjustmentCashFlow().getCashFlows().get(0).getSource().getAccountId());

        PayoutSummary payoutSummary = getPayoutSummary();
        Payout payout = random(Payout.class, "payoutCashFlow", "payoutSummary");
        payout.setId(null);
        payout.setPayoutSummary(payoutSummary);
        payout.setEventCategory(PayoutEventCategory.PAYOUT);
        id = payoutDao.save(payout);
        payout.setId(id);
        assertEquals(payout.getPayoutSummary().getPayoutSummaryItems().get(0).getFromTime().getSecond(), payoutDao.get(payout.getPayoutId()).getPayoutSummary().getPayoutSummaryItems().get(0).getFromTime().getSecond());
    }

    @Test
    @Sql("classpath:data/sql/functions_pgsql_cash_flows.sql")
    public void cashFlowInnerFunctionsTest() {
        List<PaymentQueryResultDto> paymentQueryResultDtos = jdbcTemplate.query(
                "select\n" +
                        "rpt.is_cash_flow_contains_amount_by_types(payment_cash_flow,'provider','settlement','merchant','settlement') as is_amount_contains,\n" +
                        "rpt.get_cash_flow_value(payment_cash_flow,'provider','settlement','merchant','settlement',payment_amount) as amount_sum,\n" +
                        "rpt.get_cash_flow_amount(payment_cash_flow,payment_amount) as amount_sum_by_func,\n" +
                        "rpt.is_cash_flow_contains_amount_by_types(payment_cash_flow,'merchant','settlement','system','settlement') as is_fee_contains,\n" +
                        "rpt.get_cash_flow_value(payment_cash_flow,'merchant','settlement','system','settlement',0) as fee_sum,\n" +
                        "rpt.get_cash_flow_fee(payment_cash_flow,0) as fee_sum_by_func\n" +
                        " from rpt.payment",
                new BeanPropertyRowMapper<>(PaymentQueryResultDto.class)
        );
        assertEquals(true, paymentQueryResultDtos.get(0).getIsAmountContains());
        assertEquals((long) 111, (long) paymentQueryResultDtos.get(0).getAmountSum());
        assertEquals((long) 111, (long) paymentQueryResultDtos.get(0).getAmountSumByFunc());
        assertEquals(false, paymentQueryResultDtos.get(0).getIsFeeContains());
        assertEquals((long) 0, (long) paymentQueryResultDtos.get(0).getFeeSum());
        assertEquals((long) 0, (long) paymentQueryResultDtos.get(0).getFeeSumByFunc());

        assertEquals(false, paymentQueryResultDtos.get(1).getIsAmountContains());
        assertEquals((long) 2000, (long) paymentQueryResultDtos.get(1).getAmountSum());
        assertEquals((long) 2000, (long) paymentQueryResultDtos.get(1).getAmountSumByFunc());
        assertEquals(true, paymentQueryResultDtos.get(1).getIsFeeContains());
        assertEquals((long) 333, (long) paymentQueryResultDtos.get(1).getFeeSum());
        assertEquals((long) 333, (long) paymentQueryResultDtos.get(1).getFeeSumByFunc());

        assertEquals(true, paymentQueryResultDtos.get(2).getIsAmountContains());
        assertEquals((long) 2000, (long) paymentQueryResultDtos.get(2).getAmountSum());
        assertEquals((long) 2000, (long) paymentQueryResultDtos.get(2).getAmountSumByFunc());
        assertEquals(true, paymentQueryResultDtos.get(2).getIsFeeContains());
        assertEquals((long) 50, (long) paymentQueryResultDtos.get(2).getFeeSum());
        assertEquals((long) 50, (long) paymentQueryResultDtos.get(2).getFeeSumByFunc());
    }

    @Test
    @Sql("classpath:data/sql/payment_case_1_data_with_full_filled_cash_flows.sql")
    public void whenFullFilledPaymentCashFlowsAggregateFunctionsTest() {
        PaymentQueryResultDto dto = jdbcTemplate.queryForObject(
                "select rpt.get_payment_amount(rpt.payment.*) as amount,\n" +
                        "       rpt.get_payment_fee(rpt.payment.*) as fee\n" +
                        "from rpt.payment",
                new BeanPropertyRowMapper<>(PaymentQueryResultDto.class)
        );
        assertEquals((long) 9000, (long) dto.getAmount());
        assertEquals((long) 225, (long) dto.getFee());

        dto = jdbcTemplate.queryForObject(
                "select rpt.get_payment_amount(paym.*) as amount,\n" +
                        "       rpt.get_payment_fee(paym.*) as fee\n" +
                        "from rpt.payment as paym\n" +
                        "where paym.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "  and paym.party_shop_id = 'test_shop_1'\n" +
                        "  and paym.payment_currency_code = 'RUB'\n" +
                        "  and paym.payment_status = 'captured'\n" +
                        "  and paym.event_type = 'INVOICE_PAYMENT_STATUS_CHANGED'\n" +
                        "  and paym.event_created_at < '2017-08-31T21:00:00Z'",
                new BeanPropertyRowMapper<>(PaymentQueryResultDto.class)
        );
        assertEquals((long) 3000, (long) dto.getAmount());
        assertEquals((long) 75, (long) dto.getFee());
    }

    @Test
    @Sql("classpath:data/sql/payment_case_2_data_with_partially_null_filled_cash_flows.sql")
    public void whenPartiallyNullFilledPaymentCashFlowsAggregateFunctionsTest() {
        PaymentQueryResultDto dto = jdbcTemplate.queryForObject(
                "select rpt.get_payment_amount(rpt.payment.*) as amount,\n" +
                        "       rpt.get_payment_fee(rpt.payment.*) as fee\n" +
                        "from rpt.payment",
                new BeanPropertyRowMapper<>(PaymentQueryResultDto.class)
        );
        assertEquals((long) 9000, (long) dto.getAmount());
        assertEquals((long) 150, (long) dto.getFee());

        dto = jdbcTemplate.queryForObject(
                "select rpt.get_payment_amount(paym.*) as amount,\n" +
                        "       rpt.get_payment_fee(paym.*) as fee\n" +
                        "from rpt.payment as paym\n" +
                        "where paym.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "  and paym.party_shop_id = 'test_shop_1'\n" +
                        "  and paym.payment_currency_code = 'RUB'\n" +
                        "  and paym.payment_status = 'captured'\n" +
                        "  and paym.event_type = 'INVOICE_PAYMENT_STATUS_CHANGED'\n" +
                        "  and paym.event_created_at < '2017-08-31T21:00:00Z'",
                new BeanPropertyRowMapper<>(PaymentQueryResultDto.class)
        );
        assertEquals((long) 3000, (long) dto.getAmount());
        assertEquals((long) 50, (long) dto.getFee());
    }

    @Test
    @Sql("classpath:data/sql/payment_case_3_data_with_partially_filled_cash_flows.sql")
    public void whenPartiallyFilledPaymentCashFlowsAggregateFunctionsTest() {
        PaymentQueryResultDto dto = jdbcTemplate.queryForObject(
                "select rpt.get_payment_amount(rpt.payment.*) as amount,\n" +
                        "       rpt.get_payment_fee(rpt.payment.*) as fee\n" +
                        "from rpt.payment",
                new BeanPropertyRowMapper<>(PaymentQueryResultDto.class)
        );
        assertEquals((long) 4111, (long) dto.getAmount());
        assertEquals((long) 383, (long) dto.getFee());

        dto = jdbcTemplate.queryForObject(
                "select rpt.get_payment_amount(paym.*) as amount,\n" +
                        "       rpt.get_payment_fee(paym.*) as fee\n" +
                        "from rpt.payment as paym\n" +
                        "where paym.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "  and paym.party_shop_id = 'test_shop_1'\n" +
                        "  and paym.payment_currency_code = 'RUB'\n" +
                        "  and paym.payment_status = 'captured'\n" +
                        "  and paym.event_type = 'INVOICE_PAYMENT_STATUS_CHANGED'\n" +
                        "  and paym.event_created_at < '2017-08-31T21:00:00Z'",
                new BeanPropertyRowMapper<>(PaymentQueryResultDto.class)
        );
        assertEquals((long) 111, (long) dto.getAmount());
        assertEquals((long) 0, (long) dto.getFee());
    }

    @Test
    @Sql("classpath:data/sql/payment_case_4_data_with_partially_filled_duplicate_cash_flows.sql")
    public void whenPartiallyFilledDuplicatePaymentCashFlowsAggregateFunctionsTest() {
        PaymentQueryResultDto dto = jdbcTemplate.queryForObject(
                "select rpt.get_payment_amount(rpt.payment.*) as amount,\n" +
                        "       rpt.get_payment_fee(rpt.payment.*) as fee\n" +
                        "from rpt.payment",
                new BeanPropertyRowMapper<>(PaymentQueryResultDto.class)
        );
        assertEquals((long) 8222, (long) dto.getAmount());
        assertEquals((long) 766, (long) dto.getFee());

        // тест на группировку
        List<PaymentQueryResultDto> dtos = jdbcTemplate.query(
                "select rpt.get_payment_amount(rpt.payment.*) as amount,\n" +
                        "       rpt.get_payment_fee(rpt.payment.*) as fee\n" +
                        "from rpt.payment\n" +
                        "group by party_id,party_shop_id,payment_currency_code",
                new BeanPropertyRowMapper<>(PaymentQueryResultDto.class)
        );
        assertEquals((long) 4111, (long) dtos.get(0).getAmount());
        assertEquals((long) 383, (long) dtos.get(0).getFee());
        assertEquals((long) 4111, (long) dtos.get(1).getAmount());
        assertEquals((long) 383, (long) dtos.get(1).getFee());
    }

    @Test
    @Sql("classpath:data/sql/shop_accounting_payment_case_1_data.sql")
    public void whenFullFilledShopAccountingReportPaymentDaoTest() throws DaoException {
        Map<String, Long> data = paymentDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.empty(),
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );
        assertEquals((long) 3000, (long) data.get("funds_acquired"));
        assertEquals((long) 75, (long) data.get("fee_charged"));
    }

    @Test
    @Sql("classpath:data/sql/shop_accounting_payment_case_2_data.sql")
    public void whenPartiallyNullFilledShopAccountingReportPaymentDaoTest() throws DaoException {
        Map<String, Long> data = paymentDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.empty(),
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );
        assertEquals((long) 3000, (long) data.get("funds_acquired"));
        assertEquals((long) 50, (long) data.get("fee_charged"));
    }

    @Test
    @Sql("classpath:data/sql/shop_accounting_payment_case_3_data.sql")
    public void whenPartiallyFilledShopAccountingReportPaymentDaoTest() throws DaoException {
        Map<String, Long> data = paymentDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.empty(),
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );
        assertEquals((long) 111, (long) data.get("funds_acquired"));
        assertEquals((long) 0, (long) data.get("fee_charged"));
    }

    @Test
    @Sql("classpath:data/sql/adjustment_case_1_data_with_full_filled_cash_flows.sql")
    public void whenFullFilledAdjustmentCashFlowsAggregateFunctionsTest() {
        Long fee = jdbcTemplate.queryForObject(
                "select rpt.get_adjustment_fee(rpt.adjustment.*) as fee\n" +
                        "from rpt.adjustment",
                new SingleColumnRowMapper<>()
        );
        assertEquals((long) 46, (long) fee);

        fee = jdbcTemplate.queryForObject(
                "select rpt.get_adjustment_fee(rpt.adjustment.*) as fee\n" +
                        "from rpt.adjustment\n" +
                        "where party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "  and party_shop_id = 'test_shop_1'\n" +
                        "  and event_created_at < '2017-08-31T21:00:00Z'\n" +
                        "  and adjustment_status = 'captured'",
                new SingleColumnRowMapper<>()
        );
        assertEquals((long) 23, (long) fee);
    }

    @Test
    @Sql("classpath:data/sql/adjustment_case_2_data_with_full_filled_cash_flows_and_payments.sql")
    public void whenFullFilledAdjustmentCashFlowsAndPaymentCashFlowsAggregateFunctionsTest() {
        Long funds = jdbcTemplate.queryForObject(
                "select cast(rpt.get_payment_fee(paym.*) as bigint) - cast(rpt.get_adjustment_fee(adj.*) as bigint)\n" +
                        "from rpt.adjustment as adj\n" +
                        "       join rpt.payment as paym\n" +
                        "            on adj.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "              and adj.party_shop_id = 'test_shop_1'\n" +
                        "              and adj.invoice_id = paym.invoice_id\n" +
                        "              and adj.payment_id = paym.payment_id\n" +
                        "              and adj.adjustment_status = 'captured'\n" +
                        "              and adj.event_created_at < '2017-08-31T21:00:00Z'\n" +
                        "              and paym.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "              and paym.party_shop_id = 'test_shop_1'\n" +
                        "              and paym.payment_currency_code = 'RUB'\n" +
                        "              and paym.payment_status = 'captured'\n" +
                        "              and paym.event_type = 'INVOICE_PAYMENT_STATUS_CHANGED'\n" +
                        "              and paym.event_created_at < '2017-08-31T21:00:00Z'",
                new SingleColumnRowMapper<>()
        );
        assertEquals((long) 2, (long) funds);
    }

    @Test
    @Sql("classpath:data/sql/shop_accounting_adjustment_case_1_data.sql")
    public void whenFullFilledShopAccountingReportAdjustmentDaoTest() throws DaoException {
        Map<String, Long> data = adjustmentDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.empty(),
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );
        assertEquals((long) 2, (long) data.get("funds_adjusted"));
    }

    @Test
    @Sql("classpath:data/sql/refund_case_1_data_with_full_filled_cash_flows.sql")
    public void whenFullFilledRefundCashFlowsAggregateFunctionsTest() {
        Long funds = jdbcTemplate.queryForObject(
                "select cast(rpt.get_refund_amount(ref.*) as bigint) - cast(rpt.get_refund_fee(ref.*) as bigint)\n" +
                        "from rpt.refund as ref\n" +
                        "where ref.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "  and ref.party_shop_id = 'test_shop_1'\n" +
                        "  and ref.refund_currency_code = 'RUB'\n" +
                        "  and ref.refund_status = 'succeeded'\n" +
                        "  and ref.event_created_at < '2017-08-31T21:00:00Z'",
                new SingleColumnRowMapper<>()
        );
        assertEquals((long) 1000, (long) funds);
    }

    @Test
    @Sql("classpath:data/sql/shop_accounting_refund_case_1_data.sql")
    public void whenFullFilledShopAccountingReportRefundDaoTest() throws DaoException {
        Map<String, Long> data = refundDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.empty(),
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );
        assertEquals((long) 1000, (long) data.get("funds_refunded"));
    }

    @Test
    @Sql("classpath:data/sql/payout_case_1_data_with_full_filled_cash_flows.sql")
    public void whenFullFilledPayoutCashFlowsAggregateFunctionsTest() {
        Long funds = jdbcTemplate.queryForObject(
                "select cast(rpt.get_payout_amount(pay.*) as bigint) - cast(rpt.get_payout_fee(pay.*) as bigint)\n" +
                        "from rpt.payout as pay\n" +
                        "where pay.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "  and pay.party_shop_id = 'test_shop_1'\n" +
                        "  and pay.payout_currency_code = 'RUB'\n" +
                        "  and pay.payout_status = 'paid'\n" +
                        "  and pay.event_created_at < '2017-08-31T21:00:00Z'",
                new SingleColumnRowMapper<>()
        );
        assertEquals((long) 950, (long) funds);
        List<Long> ids = jdbcTemplate.queryForList(
                "select distinct pay.payout_id\n" +
                        "from rpt.payout as pay\n" +
                        "where pay.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "  and pay.party_shop_id = 'test_shop_1'\n" +
                        "  and pay.payout_currency_code = 'RUB'\n" +
                        "  and pay.payout_status = 'paid'",
                Long.class
        );
        assertTrue(ids.contains((long) 1014));
        assertTrue(ids.contains((long) 1016));
        assertTrue(ids.contains((long) 1018));
        assertEquals(3, ids.size());
    }

    @Test
    @Sql("classpath:data/sql/payout_case_2_data_with_full_filled_cash_flows_and_additional_data.sql")
    public void whenFullFilledPayoutCashFlowsAndAdditionalDataAggregateFunctionsTest() {
        // cancelled
        List<Long> ids = jdbcTemplate.queryForList(
                "select pay.payout_id\n" +
                        "from rpt.payout as pay\n" +
                        "where pay.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "  and pay.party_shop_id = 'test_shop_1'\n" +
                        "  and pay.payout_currency_code = 'RUB'\n" +
                        "  and pay.payout_status = 'cancelled'\n" +
                        "  and pay.event_created_at < '2017-08-31T21:00:00Z'",
                Long.class
        );
        assertTrue(ids.contains((long) 1014));
        assertTrue(ids.contains((long) 51014));
        assertTrue(ids.contains((long) 751014));
        assertEquals(3, ids.size());

        // валидные cancelled (то есть те, по которым до этого был paid)
        ids = jdbcTemplate.queryForList(
                "select pay.payout_id\n" +
                        "from rpt.payout as pay\n" +
                        "where pay.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "  and pay.party_shop_id = 'test_shop_1'\n" +
                        "  and pay.payout_currency_code = 'RUB'\n" +
                        "  and pay.payout_status = 'cancelled'\n" +
                        "  and pay.event_created_at < '2017-08-31T21:00:00Z'\n" +
                        "  and pay.payout_id in (select distinct pay.payout_id\n" +
                        "                        from rpt.payout as pay\n" +
                        "                        where pay.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "                          and pay.party_shop_id = 'test_shop_1'\n" +
                        "                          and pay.payout_currency_code = 'RUB'\n" +
                        "                          and pay.payout_status = 'paid')",
                Long.class
        );
        assertTrue(ids.contains((long) 1014));
        assertTrue(ids.contains((long) 51014));
        assertEquals(2, ids.size());

        // разница между всеми paid и валидными cancelled
        Long funds = jdbcTemplate.queryForObject(
                "select paid_funds.result - cancelled_funds.result\n" +
                        "from (select cast(rpt.get_payout_amount(pay.*) as bigint) - cast(rpt.get_payout_fee(pay.*) as bigint) as result\n" +
                        "      from rpt.payout as pay\n" +
                        "      where pay.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "        and pay.party_shop_id = 'test_shop_1'\n" +
                        "        and pay.payout_currency_code = 'RUB'\n" +
                        "        and pay.payout_status = 'paid'\n" +
                        "        and pay.event_created_at < '2017-08-31T21:00:00Z') paid_funds\n" +
                        "       inner join (select cast(rpt.get_payout_amount(pay.*) as bigint) - cast(rpt.get_payout_fee(pay.*) as bigint) as result\n" +
                        "                   from rpt.payout as pay\n" +
                        "                   where pay.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "                     and pay.party_shop_id = 'test_shop_1'\n" +
                        "                     and pay.payout_currency_code = 'RUB'\n" +
                        "                     and pay.payout_status = 'cancelled'\n" +
                        "                     and pay.event_created_at < '2017-08-31T21:00:00Z'\n" +
                        "                     and pay.payout_id in (select distinct pay.payout_id\n" +
                        "                                           from rpt.payout as pay\n" +
                        "                                           where pay.party_id = 'db79ad6c-a507-43ed-9ecf-3bbd88475b32'\n" +
                        "                                             and pay.party_shop_id = 'test_shop_1'\n" +
                        "                                             and pay.payout_currency_code = 'RUB'\n" +
                        "                                             and pay.payout_status = 'paid')) cancelled_funds\n" +
                        "                  on true",
                new SingleColumnRowMapper<>()
        );
        assertEquals((long) 1900, (long) funds);
    }

    @Test
    @Sql("classpath:data/sql/shop_accounting_payout_case_1_data.sql")
    public void whenFullFilledShopAccountingReportPayoutDaoTest() throws DaoException {
        Map<String, Long> data = payoutDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.empty(),
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );
        assertEquals((long) 950, (long) data.get("funds_paid_out"));
    }

    @Test
    @Sql("classpath:data/sql/shop_accounting_payout_case_2_data.sql")
    public void whenFullFilledWithAdditionalDataShopAccountingReportPayoutDaoTest() throws DaoException {
        Map<String, Long> data = payoutDao.getShopAccountingReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                "RUB",
                Optional.empty(),
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );
        assertEquals((long) 1900, (long) data.get("funds_paid_out"));
    }

    @Test
    @Sql("classpath:data/sql/registry_payment_case_1_data.sql")
    public void registryReportPaymentDaoTest() throws DaoException {
        List<PaymentRegistryReportData> paymentData = paymentDao.getPaymentRegistryReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                toLocalDateTime(stringToTemporal("2014-08-31T21:00:00Z")),
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );

        assertEquals(LocalDateTime.parse("2017-08-23T08:30:56"), paymentData.get(0).getEventCreatedAt());
        assertEquals(InvoiceEventType.INVOICE_PAYMENT_STATUS_CHANGED, paymentData.get(0).getEventType());
        assertEquals("uWIbtnV7h2", paymentData.get(0).getInvoiceId());
        assertEquals("1", paymentData.get(0).getPaymentId());
        assertEquals("bank_card", paymentData.get(0).getPaymentTool().getLiteral());
        assertEquals((long) 1000, (long) paymentData.get(0).getPaymentAmount());
        assertEquals((long) 25, (long) paymentData.get(0).getPaymentFee());
        assertEquals((long) 46, (long) paymentData.get(0).getPaymentExternalFee());
        assertEquals((long) 46, (long) paymentData.get(0).getPaymentProviderFee());
        assertEquals("kektus", paymentData.get(0).getInvoiceProduct());
        assertEquals("RUB", paymentData.get(0).getPaymentCurrencyCode());
    }

    @Test
    @Sql("classpath:data/sql/registry_refund_case_1_data_with_payments_and_invoices.sql")
    public void registryReportRefundDaoTest() throws DaoException {
        List<RefundPaymentRegistryReportData> refundData = refundDao.getRefundPaymentRegistryReportData(
                "db79ad6c-a507-43ed-9ecf-3bbd88475b32",
                "test_shop_1",
                toLocalDateTime(stringToTemporal("2014-08-31T21:00:00Z")),
                toLocalDateTime(stringToTemporal("2017-08-31T21:00:00Z"))
        );

        RefundPaymentRegistryReportData reportData = refundData.get(0);
        assertEquals(LocalDateTime.parse("2017-08-24T16:13:23"), reportData.getRefundEventCreatedAt());
        assertEquals(InvoiceEventType.INVOICE_PAYMENT_REFUND_CREATED, reportData.getEventType());
        assertEquals("uWIbtnV7h2", reportData.getInvoiceId());
        assertEquals("1", reportData.getPaymentId());
        assertEquals("bank_card", reportData.getPaymentTool().getLiteral());
        assertEquals((long) 2000, (long) reportData.getRefundAmount());
        assertEquals("kektus", reportData.getInvoiceProduct());
        assertNull(refundData.get(1).getInvoiceProduct());
        assertEquals("1", reportData.getRefundId());
        assertEquals("You are the reason of my life", reportData.getRefundReason());
        assertEquals("RUB", reportData.getRefundCurrencyCode());
    }

    private FinalCashFlow getFinalCashFlow() {
        List<FinalCashFlowPosting> postings = new ArrayList<>();
        postings.add(getCashFlowPosting(13444L, ProviderCashFlowAccount.ProviderCashFlowAccountType.SETTLEMENT, 6L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 1000L, "RUB"));
        postings.add(getCashFlowPosting(13444L, ProviderCashFlowAccount.ProviderCashFlowAccountType.SETTLEMENT, 6L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 1000L, "RUB"));
        postings.add(getCashFlowPosting(13444L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 6L, SystemCashFlowAccount.SystemCashFlowAccountType.SETTLEMENT, 25L, "RUB"));
        postings.add(getCashFlowPosting(13444L, MerchantCashFlowAccount.MerchantCashFlowAccountType.SETTLEMENT, 6L, SystemCashFlowAccount.SystemCashFlowAccountType.SETTLEMENT, 25L, "RUB"));

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

    @Data
    public static class PaymentQueryResultDto {

        private Boolean isAmountContains;
        private Long amountSum;
        private Long amountSumByFunc;
        private Boolean isFeeContains;
        private Long feeSum;
        private Long feeSumByFunc;
        private Long amount;
        private Long fee;

    }
}
