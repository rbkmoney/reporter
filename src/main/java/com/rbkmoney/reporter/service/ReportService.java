package com.rbkmoney.reporter.service;

import com.rbkmoney.reporter.ReportType;
import com.rbkmoney.reporter.model.ShopAccounting;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by tolkonepiu on 11/07/2017.
 */
@Service
public class ReportService {

    public void generateProvisionOfServiceReport(ShopAccounting shopAccounting, OutputStream outputStream) {
        Context context = new Context();
        context.putVar("shopAccounting", shopAccounting);

        try {
            generateReport(
                    context,
                    ReportType.PROVISION_OF_SERVICE.getTemplateResource().getInputStream(),
                    outputStream
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void generateReport(Context context, InputStream inputStream, OutputStream outputStream) {
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
