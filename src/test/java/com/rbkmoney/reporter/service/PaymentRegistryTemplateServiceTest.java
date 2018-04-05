package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.AbstractIntegrationTest;
import com.rbkmoney.reporter.model.Payment;
import com.rbkmoney.reporter.model.Refund;
import com.rbkmoney.reporter.service.impl.PaymentRegistryTemplateImpl;
import com.rbkmoney.reporter.util.TimeUtil;
import org.apache.thrift.TException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static io.github.benas.randombeans.api.EnhancedRandom.random;

/**
 * Created by tolkonepiu on 12/07/2017.
 */
public class PaymentRegistryTemplateServiceTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentRegistryTemplateImpl templateService;

    @Test
    public void testProcessPaymentRegistryTemplate() throws IOException, TException {
        Path tempFile = Files.createTempFile("registry_of_act_", "_test_report.xlsx");
        System.out.println("Registry of act report generated on " + tempFile.toAbsolutePath().toString());

        Instant fromTime = random(Instant.class);
        Instant toTime = random(Instant.class);
        ZoneId zoneId = ZoneId.of("Europe/Moscow");

        List<Payment> paymentList = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            Payment payment = new Payment();
            payment.setId("id" + i);
            payment.setCapturedAt(TimeUtil.toLocalizedDateTime("201" + i + "-03-22T06:12:27Z", zoneId));
            payment.setCardNum("4242****56789" +  i);
            payment.setAmount(123L + i);
            payment.setPayoutAmount(120L + i);
            payment.setPayerEmail("abc" + i + "@mail.ru");
            payment.setShopUrl("2ch" + i + ".ru");
            payment.setPurpose("purpose" + i);
            paymentList.add(payment);
        }

        List<Refund> refundList = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            Refund refund = new Refund();
            refund.setId("id" + i);
            refund.setPaymentId("paymentId" + i);
            refund.setPaymentCapturedAt(TimeUtil.toLocalizedDateTime("201" + i + "-03-22T06:12:27Z", zoneId));
            refund.setSucceededAt(TimeUtil.toLocalizedDateTime("201" + i + "-03-22T06:12:27Z", zoneId));
            refund.setCardNum("4242****56789" + i);
            refund.setAmount(123L + i);
            refund.setPayerEmail("abc" + i + "@mail.ru");
            refund.setShopUrl("2ch" + i + ".ru");
            refund.setPaymentPurpose("purpose" + i);
            refundList.add(refund);
        }

        try {
//            templateService.processReportTemplate(
//                    paymentList,
//                    refundList,
//                    fromTime,
//                    toTime,
//                    zoneId,
//                    Files.newOutputStream(tempFile));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

}
