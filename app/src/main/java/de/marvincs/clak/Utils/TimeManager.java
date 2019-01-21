package de.marvincs.clak.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimeManager {

    public static String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("hh:mm:ss");
        String datetime = dateformat.format(cal.getTime());
        return datetime;
    }

    public static int getCurrentHour() {
        return Integer.parseInt(getCurrentTime().split(":")[0]);
    }

    public static int getCurrentMinunte() {
        return Integer.parseInt(getCurrentTime().split(":")[1]);

    }

    public static int getCurrentSecond() {
        return Integer.parseInt(getCurrentTime().split(":")[2]);

    }

    public static long getCurrentTimeInMilliseconds() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }

    public static long getTimeInMilliseconds(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
        try {
            return dateFormat.parse(time).getTime();
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    public static long validateTime(long time) {
        while (!inFuture(time)) {
            time = addADay(time);
        }
        return time;
    }

    public static boolean inFuture(long time) {
        return (TimeManager.getCurrentTimeInMilliseconds() - time) < 0;
    }

    public static long addADay(long time) {
        return time + 86400000;
    }


    /**
     * Sorts a list with times
     *
     * @param times
     */
    public static void sort(List<String> times) {
        Collections.sort(times, new Comparator<String>() {
            @Override
            public int compare(String time1, String time2) {
                long t1 = getTimeInMilliseconds(time1);
                long t2 = getTimeInMilliseconds(time2);
                return Long.compare(t1, t2);
            }
        });
    }
}
