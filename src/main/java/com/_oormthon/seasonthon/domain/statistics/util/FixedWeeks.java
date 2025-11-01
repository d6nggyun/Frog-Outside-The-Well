package com._oormthon.seasonthon.domain.statistics.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public final class FixedWeeks {

    private FixedWeeks() {
        throw new UnsupportedOperationException("Utility class");
    }

    public record WeekBucket(int index, LocalDate startDate, LocalDate endDate) {}

    public static List<WeekBucket> getWeekBuckets(YearMonth yearMonth) {
        LocalDate end = yearMonth.atEndOfMonth();
        List<WeekBucket> weekBuckets = new ArrayList<>(5);

        for (int i = 0, start = 1; i < 4; i++, start += 7) {
            LocalDate startDate = yearMonth.atDay(start);
            LocalDate endDate = startDate.plusDays(6);

            if (startDate.isAfter(end)) break;
            if (endDate.isAfter(end)) endDate = end;

            weekBuckets.add(new WeekBucket(i+1, startDate, endDate));
        }

        if (end.getDayOfMonth() >= 29) {
            weekBuckets.add(new WeekBucket(5, yearMonth.atDay(29), end));
        }

        return weekBuckets;
    }

    public static int getWeekIndex(LocalDate date) {
        int index = ((date.getDayOfMonth() - 1) / 7) + 1;
        return Math.min(index, 5);
    }
}
