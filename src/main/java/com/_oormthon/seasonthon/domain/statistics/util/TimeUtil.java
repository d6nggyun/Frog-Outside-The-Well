package com._oormthon.seasonthon.domain.statistics.util;

public final class TimeUtil {

    public static String formatDuration(Long seconds) {
        if (seconds == null) return null;
        if (seconds == 0) return "0시간";
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0 && minutes > 0) return hours + "시간 " + minutes + "분";
        if (hours > 0) return hours + "시간";
        return minutes + "분";
    }
}
