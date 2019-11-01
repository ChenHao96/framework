package com.github.chenhao96.utils;

import java.util.Calendar;
import java.util.Date;

public final class DateUtil {
    private DateUtil() {
    }

    public static long onlyDateTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long onlyDateTime() {
        return onlyDateTime(new Date());
    }
}
