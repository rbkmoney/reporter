package com.rbkmoney.reporter.task;

import java.util.TimeZone;

/**
 * Created by tolkonepiu on 17/07/2017.
 */
public class Task {

    private String id;

    private TimeZone timezone;

    private String cron;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (id != null ? !id.equals(task.id) : task.id != null) return false;
        if (timezone != null ? !timezone.equals(task.timezone) : task.timezone != null) return false;
        return cron != null ? cron.equals(task.cron) : task.cron == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (timezone != null ? timezone.hashCode() : 0);
        result = 31 * result + (cron != null ? cron.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", timezone=" + timezone +
                ", cron='" + cron + '\'' +
                '}';
    }
}
