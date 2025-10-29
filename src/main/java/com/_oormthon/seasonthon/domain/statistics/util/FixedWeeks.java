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

        for (int i = 0; )
    }
}
