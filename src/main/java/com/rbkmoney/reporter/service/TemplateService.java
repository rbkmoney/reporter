package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.model.PartyRepresentation;
import com.rbkmoney.reporter.model.ShopAccounting;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.time.Instant;

/**
 * Created by tolkonepiu on 11/07/2017.
 */
@Service
public class TemplateService {

    public void processProvisionOfServiceTemplate(PartyRepresentation partyRepresentation,
                                                  ShopAccounting shopAccounting,
                                                  Instant fromTime,
                                                  Instant toTime,
                                                  OutputStream outputStream) {
        Context context = new Context();
        context.putVar("shopAccounting", shopAccounting);
        context.putVar("partyRepresentation", partyRepresentation);
        context.putVar("fromTime", Date.from(fromTime));
        context.putVar("toTime", Date.from(toTime));

        try {
            processTemplate(
                    context,
                    ReportType.PROVISION_OF_SERVICE.getTemplateResource().getInputStream(),
                    outputStream
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void processTemplate(Context context, InputStream inputStream, OutputStream outputStream) {
        try {
            JxlsHelper.getInstance()
                    .processTemplate(
                            inputStream,
                            outputStream,
                            context
                    );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


}
