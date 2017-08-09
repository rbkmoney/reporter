package com.rbkmoney.reporter.util;

import com.rbkmoney.damsel.reports.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.reporter.domain.tables.pojos.File;

import java.util.List;
import java.util.stream.Collectors;

public class DamselUtil {

    public static Report toDamselReport(com.rbkmoney.reporter.domain.tables.pojos.Report report, List<File> files) {
        Report dReport = new Report();
        dReport.setReportId(report.getId());
        dReport.setStatus(ReportStatus.valueOf(report.getStatus().getName()));
        ReportTimeRange timeRange = new ReportTimeRange(
                TypeUtil.temporalToString(report.getFromTime()),
                TypeUtil.temporalToString(report.getToTime())
        );
        dReport.setTimeRange(timeRange);
        dReport.setReportType(ReportType.valueOf(report.getType()));
        dReport.setCreatedAt(TypeUtil.temporalToString(report.getCreatedAt()));

        dReport.setFiles(files.stream()
                .map(DamselUtil::toDamselFile)
                .collect(Collectors.toList()));

        return dReport;
    }

    public static FileMeta toDamselFile(File file) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setFileId(file.getId());
        fileMeta.setFilename(file.getFilename());
        Signature signature = new Signature();
        signature.setMd5(file.getMd5());
        signature.setSha256(file.getSha256());
        fileMeta.setSignature(signature);
        return fileMeta;
    }

}
