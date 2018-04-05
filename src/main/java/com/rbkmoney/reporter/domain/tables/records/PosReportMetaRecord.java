/*
 * This file is generated by jOOQ.
*/
package com.rbkmoney.reporter.domain.tables.records;


import com.rbkmoney.reporter.domain.tables.PosReportMeta;

import java.time.LocalDateTime;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


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
public class PosReportMetaRecord extends UpdatableRecordImpl<PosReportMetaRecord> implements Record5<String, String, Long, Long, LocalDateTime> {

    private static final long serialVersionUID = -1276821869;

    /**
     * Setter for <code>rpt.pos_report_meta.party_id</code>.
     */
    public void setPartyId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>rpt.pos_report_meta.party_id</code>.
     */
    public String getPartyId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>rpt.pos_report_meta.contract_id</code>.
     */
    public void setContractId(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>rpt.pos_report_meta.contract_id</code>.
     */
    public String getContractId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>rpt.pos_report_meta.last_opening_balance</code>.
     */
    public void setLastOpeningBalance(Long value) {
        set(2, value);
    }

    /**
     * Getter for <code>rpt.pos_report_meta.last_opening_balance</code>.
     */
    public Long getLastOpeningBalance() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>rpt.pos_report_meta.last_closing_balance</code>.
     */
    public void setLastClosingBalance(Long value) {
        set(3, value);
    }

    /**
     * Getter for <code>rpt.pos_report_meta.last_closing_balance</code>.
     */
    public Long getLastClosingBalance() {
        return (Long) get(3);
    }

    /**
     * Setter for <code>rpt.pos_report_meta.last_report_created_at</code>.
     */
    public void setLastReportCreatedAt(LocalDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>rpt.pos_report_meta.last_report_created_at</code>.
     */
    public LocalDateTime getLastReportCreatedAt() {
        return (LocalDateTime) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record2<String, String> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<String, String, Long, Long, LocalDateTime> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<String, String, Long, Long, LocalDateTime> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return PosReportMeta.POS_REPORT_META.PARTY_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return PosReportMeta.POS_REPORT_META.CONTRACT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field3() {
        return PosReportMeta.POS_REPORT_META.LAST_OPENING_BALANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field4() {
        return PosReportMeta.POS_REPORT_META.LAST_CLOSING_BALANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field5() {
        return PosReportMeta.POS_REPORT_META.LAST_REPORT_CREATED_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getPartyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getContractId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value3() {
        return getLastOpeningBalance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value4() {
        return getLastClosingBalance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value5() {
        return getLastReportCreatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PosReportMetaRecord value1(String value) {
        setPartyId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PosReportMetaRecord value2(String value) {
        setContractId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PosReportMetaRecord value3(Long value) {
        setLastOpeningBalance(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PosReportMetaRecord value4(Long value) {
        setLastClosingBalance(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PosReportMetaRecord value5(LocalDateTime value) {
        setLastReportCreatedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PosReportMetaRecord values(String value1, String value2, Long value3, Long value4, LocalDateTime value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PosReportMetaRecord
     */
    public PosReportMetaRecord() {
        super(PosReportMeta.POS_REPORT_META);
    }

    /**
     * Create a detached, initialised PosReportMetaRecord
     */
    public PosReportMetaRecord(String partyId, String contractId, Long lastOpeningBalance, Long lastClosingBalance, LocalDateTime lastReportCreatedAt) {
        super(PosReportMeta.POS_REPORT_META);

        set(0, partyId);
        set(1, contractId);
        set(2, lastOpeningBalance);
        set(3, lastClosingBalance);
        set(4, lastReportCreatedAt);
    }
}