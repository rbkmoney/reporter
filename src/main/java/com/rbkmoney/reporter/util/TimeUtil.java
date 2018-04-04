package com.rbkmoney.reporter.util;

import java.time.*;

public class TimeUtil {

    public static Instant toZoneSameLocal(Instant instant, ZoneId zoneId) {
        return ZonedDateTime.ofInstant(instant, zoneId).withZoneSameLocal(ZoneOffset.UTC).toInstant();
    }

    public static LocalDateTime toZoneSameLocal(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.atZone(zoneId).withZoneSameLocal(ZoneOffset.UTC).toLocalDateTime();
    }

}
