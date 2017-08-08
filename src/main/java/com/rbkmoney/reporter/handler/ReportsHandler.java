package com.rbkmoney.reporter.handler;

import com.rbkmoney.damsel.base.InvalidRequest;
import com.rbkmoney.damsel.reports.*;
import com.rbkmoney.reporter.exception.FileNotFoundException;
import com.rbkmoney.reporter.exception.PartyNotFoundException;
import com.rbkmoney.reporter.exception.ReportNotFoundException;
import com.rbkmoney.reporter.exception.ShopNotFoundException;
import org.apache.thrift.TException;
import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tolkonepiu on 18/07/2017.
 */
@Component
public class ReportsHandler implements ReportingSrv.Iface {

    @Override
    public List<Report> getReports(ReportRequest reportRequest, List<ReportType> reportTypes) throws DatasetTooBig, InvalidRequest, TException {
        return new ArrayList<>();
    }

    @Override
    public long generateReport(ReportRequest reportRequest, ReportType reportType) throws PartyNotFound, ShopNotFound, InvalidRequest, TException {
        try {
            return 0L;
        } catch (PartyNotFoundException ex) {
            throw new PartyNotFound();
        } catch (ShopNotFoundException ex) {
            throw new ShopNotFound();
        }
    }

    @Override
    public Report getReport(long reportId) throws ReportNotFound, TException {
        try {
            return null;
        } catch (ReportNotFoundException ex) {
            throw new ReportNotFound();
        }
    }

    @Override
    public String generatePresignedUrl(String fileId, String expiresAt) throws FileNotFound, InvalidRequest, TException {
        try {
            return null;
        } catch (FileNotFoundException ex) {
            throw new FileNotFound();
        }
    }
}
