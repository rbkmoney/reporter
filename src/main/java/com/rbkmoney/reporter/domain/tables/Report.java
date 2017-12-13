/*
 * This file is generated by jOOQ.
*/
package com.rbkmoney.reporter.domain.tables;


import com.rbkmoney.reporter.domain.Keys;
import com.rbkmoney.reporter.domain.Rpt;
import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.tables.records.ReportRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.6"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Report extends TableImpl<ReportRecord> {

    private static final long serialVersionUID = 765586862;

    /**
     * The reference instance of <code>rpt.report</code>
     */
    public static final Report REPORT = new Report();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ReportRecord> getRecordType() {
        return ReportRecord.class;
    }

    /**
     * The column <code>rpt.report.id</code>.
     */
    public final TableField<ReportRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('rpt.report_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>rpt.report.from_time</code>.
     */
    public final TableField<ReportRecord, LocalDateTime> FROM_TIME = createField("from_time", org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false), this, "");

    /**
     * The column <code>rpt.report.to_time</code>.
     */
    public final TableField<ReportRecord, LocalDateTime> TO_TIME = createField("to_time", org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false), this, "");

    /**
     * The column <code>rpt.report.created_at</code>.
     */
    public final TableField<ReportRecord, LocalDateTime> CREATED_AT = createField("created_at", org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false), this, "");

    /**
     * The column <code>rpt.report.party_id</code>.
     */
    public final TableField<ReportRecord, String> PARTY_ID = createField("party_id", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>rpt.report.party_shop_id</code>.
     */
    public final TableField<ReportRecord, String> PARTY_SHOP_ID = createField("party_shop_id", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>rpt.report.status</code>.
     */
    public final TableField<ReportRecord, ReportStatus> STATUS = createField("status", org.jooq.util.postgres.PostgresDataType.VARCHAR.asEnumDataType(com.rbkmoney.reporter.domain.enums.ReportStatus.class), this, "");

    /**
     * The column <code>rpt.report.timezone</code>.
     */
    public final TableField<ReportRecord, String> TIMEZONE = createField("timezone", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>rpt.report.type</code>.
     */
    public final TableField<ReportRecord, String> TYPE = createField("type", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>rpt.report.need_sign</code>.
     */
    public final TableField<ReportRecord, Boolean> NEED_SIGN = createField("need_sign", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

    /**
     * Create a <code>rpt.report</code> table reference
     */
    public Report() {
        this("report", null);
    }

    /**
     * Create an aliased <code>rpt.report</code> table reference
     */
    public Report(String alias) {
        this(alias, REPORT);
    }

    private Report(String alias, Table<ReportRecord> aliased) {
        this(alias, aliased, null);
    }

    private Report(String alias, Table<ReportRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Rpt.RPT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<ReportRecord, Long> getIdentity() {
        return Keys.IDENTITY_REPORT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ReportRecord> getPrimaryKey() {
        return Keys.REPORT_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ReportRecord>> getKeys() {
        return Arrays.<UniqueKey<ReportRecord>>asList(Keys.REPORT_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report as(String alias) {
        return new Report(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Report rename(String name) {
        return new Report(name, null);
    }
}
