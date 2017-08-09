/*
 * This file is generated by jOOQ.
*/
package com.rbkmoney.reporter.domain.tables.records;


import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.tables.Report;

import java.time.LocalDateTime;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ReportRecord extends UpdatableRecordImpl<ReportRecord> implements Record9<Long, LocalDateTime, LocalDateTime, LocalDateTime, String, String, ReportStatus, String, String> {

    private static final long serialVersionUID = 1349443250;

    /**
     * Setter for <code>rpt.report.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>rpt.report.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>rpt.report.from_time</code>.
     */
    public void setFromTime(LocalDateTime value) {
        set(1, value);
    }

    /**
     * Getter for <code>rpt.report.from_time</code>.
     */
    public LocalDateTime getFromTime() {
        return (LocalDateTime) get(1);
    }

    /**
     * Setter for <code>rpt.report.to_time</code>.
     */
    public void setToTime(LocalDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>rpt.report.to_time</code>.
     */
    public LocalDateTime getToTime() {
        return (LocalDateTime) get(2);
    }

    /**
     * Setter for <code>rpt.report.created_at</code>.
     */
    public void setCreatedAt(LocalDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>rpt.report.created_at</code>.
     */
    public LocalDateTime getCreatedAt() {
        return (LocalDateTime) get(3);
    }

    /**
     * Setter for <code>rpt.report.party_id</code>.
     */
    public void setPartyId(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>rpt.report.party_id</code>.
     */
    public String getPartyId() {
        return (String) get(4);
    }

    /**
     * Setter for <code>rpt.report.party_shop_id</code>.
     */
    public void setPartyShopId(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>rpt.report.party_shop_id</code>.
     */
    public String getPartyShopId() {
        return (String) get(5);
    }

    /**
     * Setter for <code>rpt.report.status</code>.
     */
    public void setStatus(ReportStatus value) {
        set(6, value);
    }

    /**
     * Getter for <code>rpt.report.status</code>.
     */
    public ReportStatus getStatus() {
        return (ReportStatus) get(6);
    }

    /**
     * Setter for <code>rpt.report.timezone</code>.
     */
    public void setTimezone(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>rpt.report.timezone</code>.
     */
    public String getTimezone() {
        return (String) get(7);
    }

    /**
     * Setter for <code>rpt.report.type</code>.
     */
    public void setType(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>rpt.report.type</code>.
     */
    public String getType() {
        return (String) get(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<Long, LocalDateTime, LocalDateTime, LocalDateTime, String, String, ReportStatus, String, String> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<Long, LocalDateTime, LocalDateTime, LocalDateTime, String, String, ReportStatus, String, String> valuesRow() {
        return (Row9) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Report.REPORT.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field2() {
        return Report.REPORT.FROM_TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field3() {
        return Report.REPORT.TO_TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field4() {
        return Report.REPORT.CREATED_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return Report.REPORT.PARTY_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return Report.REPORT.PARTY_SHOP_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<ReportStatus> field7() {
        return Report.REPORT.STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field8() {
        return Report.REPORT.TIMEZONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field9() {
        return Report.REPORT.TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value2() {
        return getFromTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value3() {
        return getToTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value4() {
        return getCreatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getPartyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getPartyShopId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportStatus value7() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value8() {
        return getTimezone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value9() {
        return getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportRecord value2(LocalDateTime value) {
        setFromTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportRecord value3(LocalDateTime value) {
        setToTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportRecord value4(LocalDateTime value) {
        setCreatedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportRecord value5(String value) {
        setPartyId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportRecord value6(String value) {
        setPartyShopId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportRecord value7(ReportStatus value) {
        setStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportRecord value8(String value) {
        setTimezone(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportRecord value9(String value) {
        setType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportRecord values(Long value1, LocalDateTime value2, LocalDateTime value3, LocalDateTime value4, String value5, String value6, ReportStatus value7, String value8, String value9) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ReportRecord
     */
    public ReportRecord() {
        super(Report.REPORT);
    }

    /**
     * Create a detached, initialised ReportRecord
     */
    public ReportRecord(Long id, LocalDateTime fromTime, LocalDateTime toTime, LocalDateTime createdAt, String partyId, String partyShopId, ReportStatus status, String timezone, String type) {
        super(Report.REPORT);

        set(0, id);
        set(1, fromTime);
        set(2, toTime);
        set(3, createdAt);
        set(4, partyId);
        set(5, partyShopId);
        set(6, status);
        set(7, timezone);
        set(8, type);
    }
}
