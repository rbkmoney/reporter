/*
 * This file is generated by jOOQ.
*/
package com.rbkmoney.reporter.domain;


import com.rbkmoney.reporter.domain.tables.File;
import com.rbkmoney.reporter.domain.tables.Report;
import com.rbkmoney.reporter.domain.tables.records.FileRecord;
import com.rbkmoney.reporter.domain.tables.records.ReportRecord;

import javax.annotation.Generated;

import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;


/**
 * A class modelling foreign key relationships between tables of the <code>rpt</code> 
 * schema
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<ReportRecord, Long> IDENTITY_REPORT = Identities0.IDENTITY_REPORT;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<FileRecord> FILE_PKEY = UniqueKeys0.FILE_PKEY;
    public static final UniqueKey<ReportRecord> REPORT_PKEY = UniqueKeys0.REPORT_PKEY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 extends AbstractKeys {
        public static Identity<ReportRecord, Long> IDENTITY_REPORT = createIdentity(Report.REPORT, Report.REPORT.ID);
    }

    private static class UniqueKeys0 extends AbstractKeys {
        public static final UniqueKey<FileRecord> FILE_PKEY = createUniqueKey(File.FILE, "file_pkey", File.FILE.ID);
        public static final UniqueKey<ReportRecord> REPORT_PKEY = createUniqueKey(Report.REPORT, "report_pkey", Report.REPORT.ID);
    }
}