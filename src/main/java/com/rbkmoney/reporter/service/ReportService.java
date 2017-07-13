package com.rbkmoney.reporter.service;

import com.amazonaws.services.s3.AmazonS3;
import com.rbkmoney.damsel.merch_stat.StatPayment;
import com.rbkmoney.reporter.DateTimeParser;
import com.rbkmoney.reporter.ReportType;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.Instant;
import java.util.List;

/**
 * Created by tolkonepiu on 11/07/2017.
 */
@Service
public class ReportService {

    @Autowired
    StatisticService statisticService;

    public String generateProvisionReport(String partyId, String shopId, Instant fromTime, Instant toTime) {
        try {
            Path reportFile = Files.createTempFile("report_", "_provision_of_service_act.xlsx");
            List<StatPayment> statPayments = statisticService.getPayments(partyId, shopId, fromTime, toTime);
            Context context = new Context();
            context.putVar("fromTime", Date.from(fromTime));
            context.putVar("toTime", Date.from(toTime));
            context.putVar("dateTimeParser", new DateTimeParser());
            context.putVar("payments", statPayments);
            JxlsHelper.getInstance()
                    .processTemplate(
                            ReportType.PROVISION_OF_SERVICE.getTemplateResource().getInputStream(),
                            Files.newOutputStream(reportFile),
                            context
                    );
            return reportFile.toAbsolutePath().toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


}
