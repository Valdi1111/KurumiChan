package org.valdi.kurumi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.TimeZone;

public class Utils {

    public static String formatDate(long epoch) {
        Date date = new Date(epoch);
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        return format.format(date);
    }

    public static TemporalAccessor fixedTemporal(long epoch) {
        Instant instant = Instant.ofEpochMilli(epoch);
        ZoneId z = ZoneId.of("Europe/Rome");
        ZonedDateTime zdt = instant.atZone(z);
        return zdt.toInstant();
    }

    public static OffsetDateTime getOffsetDateTime(long epoch) {
        Instant instant = Instant.ofEpochMilli(epoch);
        ZoneId z = ZoneId.of("Europe/Rome");
        return OffsetDateTime.ofInstant(instant, z);
    }

}
