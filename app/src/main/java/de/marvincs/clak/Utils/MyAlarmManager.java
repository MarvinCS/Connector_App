package de.marvincs.clak.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import de.marvincs.clak.Services.RequestService;

public class MyAlarmManager {

    public static void setSavedAlarms(Context ctx) {
        Log.d("MCSAPP - MyAlarammanage", "Reset alarms");
        Intent myIntent = new Intent(ctx, RequestService.class);
        myIntent.setAction(RequestService.ACTION_RESET_ALARMS);
        ctx.startService(myIntent);

    }

    public static void addAlarms(Context ctx, List<String> times) {
        for (String time : times) {
            String hour = time.split(":")[0];
            String minute = time.split(":")[1];
            addAlarm(ctx, Integer.parseInt(hour), Integer.parseInt(minute));
        }
    }

    public static void addAlarm(Context ctx, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 1);
        Intent myIntent = new Intent(ctx, RequestService.class);
        myIntent.setAction(RequestService.ACTION_CONNECT);
        myIntent.putExtra("REPEATING", true);
        PendingIntent pi = PendingIntent.getService(ctx, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long time = TimeManager.validateTime(calendar.getTimeInMillis());
        android.app.AlarmManager am = (android.app.AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, time, pi);
        Log.d("MCSAPP - MyAlarammanage", "Added Alarm: " + hourOfDay + ":" + (minute < 10 ? "0" + minute : minute) + ":" + 1);
        Log.d("MCSAPP - MyAlarammanage", "Now: " + TimeManager.getCurrentTimeInMilliseconds());
        Log.d("MCSAPP - MyAlarammanage", "Set: " + time);
        Log.d("MCSAPP - MyAlarammanage", "Dif: " + (TimeManager.getCurrentTimeInMilliseconds() - time));
        //am.setRepeating(android.app.AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), android.app.AlarmManager.INTERVAL_DAY, pi);
    }

    public static void cancelAlarms(Context ctx) {
        Intent myIntent = new Intent(ctx, RequestService.class);
        myIntent.setAction(RequestService.ACTION_CONNECT);
        myIntent.putExtra("REPEATING", true);
        android.app.AlarmManager am = (android.app.AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(PendingIntent.getService(ctx, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        Log.d("MCSAPP - MyAlarammanage", "Canceled alarms");
    }
}
