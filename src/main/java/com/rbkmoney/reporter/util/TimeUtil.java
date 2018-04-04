package com.rbkmoney.reporter.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static Instant toZoneSameLocal(Instant instant, ZoneId zoneId) {
        return ZonedDateTime.ofInstant(instant, zoneId).withZoneSameLocal(ZoneOffset.UTC).toInstant();
    }

    public static String toLocalizedDateTime(String dateTimeUtc, ZoneId zoneId) {
        return ZonedDateTime.parse(dateTimeUtc, DateTimeFormatter.ISO_DATE_TIME).withZoneSameLocal(ZoneOffset.UTC).withZoneSameInstant(zoneId).format(dateTimeFormatter);
    }

    public static String toLocalizedDate(Instant instant, ZoneId zoneId) {
        return dateFormatter.withZone(zoneId).format(instant);
    }
}
