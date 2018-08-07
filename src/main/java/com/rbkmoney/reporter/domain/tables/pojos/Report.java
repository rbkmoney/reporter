/*
 * This file is generated by jOOQ.
*/
package com.rbkmoney.reporter.domain.tables.pojos;


import com.rbkmoney.reporter.domain.enums.ReportStatus;
import com.rbkmoney.reporter.domain.enums.ReportType;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.annotation.Generated;


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
public class Report implements Serializable {

    private static final long serialVersionUID = 1277833059;

    private Long          id;
    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    private LocalDateTime createdAt;
    private String        partyId;
    private String        partyShopId;
    private ReportStatus  status;
    private String        timezone;
    private ReportType    type;

    public Report() {}

    public Report(Report value) {
        this.id = value.id;
        this.fromTime = value.fromTime;
        this.toTime = value.toTime;
        this.createdAt = value.createdAt;
        this.partyId = value.partyId;
        this.partyShopId = value.partyShopId;
        this.status = value.status;
        this.timezone = value.timezone;
        this.type = value.type;
    }

    public Report(
        Long          id,
        LocalDateTime fromTime,
        LocalDateTime toTime,
        LocalDateTime createdAt,
        String        partyId,
        String        partyShopId,
        ReportStatus  status,
        String        timezone,
        ReportType    type
    ) {
        this.id = id;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.createdAt = createdAt;
        this.partyId = partyId;
        this.partyShopId = partyShopId;
        this.status = status;
        this.timezone = timezone;
        this.type = type;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFromTime() {
        return this.fromTime;
    }

    public void setFromTime(LocalDateTime fromTime) {
        this.fromTime = fromTime;
    }

    public LocalDateTime getToTime() {
        return this.toTime;
    }

    public void setToTime(LocalDateTime toTime) {
        this.toTime = toTime;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPartyId() {
        return this.partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getPartyShopId() {
        return this.partyShopId;
    }

    public void setPartyShopId(String partyShopId) {
        this.partyShopId = partyShopId;
    }

    public ReportStatus getStatus() {
        return this.status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public ReportType getType() {
        return this.type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Report other = (Report) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (fromTime == null) {
            if (other.fromTime != null)
                return false;
        }
        else if (!fromTime.equals(other.fromTime))
            return false;
        if (toTime == null) {
            if (other.toTime != null)
                return false;
        }
        else if (!toTime.equals(other.toTime))
            return false;
        if (createdAt == null) {
            if (other.createdAt != null)
                return false;
        }
        else if (!createdAt.equals(other.createdAt))
            return false;
        if (partyId == null) {
            if (other.partyId != null)
                return false;
        }
        else if (!partyId.equals(other.partyId))
            return false;
        if (partyShopId == null) {
            if (other.partyShopId != null)
                return false;
        }
        else if (!partyShopId.equals(other.partyShopId))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        }
        else if (!status.equals(other.status))
            return false;
        if (timezone == null) {
            if (other.timezone != null)
                return false;
        }
        else if (!timezone.equals(other.timezone))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        }
        else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.fromTime == null) ? 0 : this.fromTime.hashCode());
        result = prime * result + ((this.toTime == null) ? 0 : this.toTime.hashCode());
        result = prime * result + ((this.createdAt == null) ? 0 : this.createdAt.hashCode());
        result = prime * result + ((this.partyId == null) ? 0 : this.partyId.hashCode());
        result = prime * result + ((this.partyShopId == null) ? 0 : this.partyShopId.hashCode());
        result = prime * result + ((this.status == null) ? 0 : this.status.hashCode());
        result = prime * result + ((this.timezone == null) ? 0 : this.timezone.hashCode());
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Report (");

        sb.append(id);
        sb.append(", ").append(fromTime);
        sb.append(", ").append(toTime);
        sb.append(", ").append(createdAt);
        sb.append(", ").append(partyId);
        sb.append(", ").append(partyShopId);
        sb.append(", ").append(status);
        sb.append(", ").append(timezone);
        sb.append(", ").append(type);

        sb.append(")");
        return sb.toString();
    }
}
