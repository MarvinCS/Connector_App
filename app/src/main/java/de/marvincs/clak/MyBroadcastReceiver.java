package de.marvincs.clak;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import static de.marvincs.clak.Network.IPADRESS;
import static de.marvincs.clak.Network.LOGINID;
import static de.marvincs.clak.Network.NETWORKNAME;
import static de.marvincs.clak.Network.PASSWORD;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static int period = 600000; // 10 minutes
    private static final int INITIAL_DELAY = 5000; // 5 seconds

    public static void deleteAlarms() {
    }

    @Override
    public void onReceive(Context ctxt, Intent i) {
        if (i.getAction() == null) {
            RequestService.enqueueWork(ctxt, i);
        } else {
            scheduleAlarms(ctxt, i.getStringExtra(LOGINID), i.getStringExtra(PASSWORD), i.getStringExtra(IPADRESS), i.getStringExtra(NETWORKNAME));
        }
    }

    static void scheduleAlarms(Context ctxt, String loginid, String password, String ip, String network) {
        AlarmManager mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i = getLoginIntent(ctxt, loginid, password, ip, network);
        PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);

        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + INITIAL_DELAY, period, pi);
    }

    public static int getPeriod() {
        return period;
    }

    public static void setPeriod(int period) {
        if (period > 0) {
            MyBroadcastReceiver.period = period;
        }
    }


    private static Intent getLoginIntent(Context ctxt, String loginid, String password, String ip, String network) {
        Intent myIntent = new Intent(ctxt, MyBroadcastReceiver.class);
        myIntent.putExtra(NETWORKNAME, network);
        myIntent.putExtra(LOGINID, loginid);
        myIntent.putExtra(PASSWORD, password);
        myIntent.putExtra(IPADRESS, ip);
        //myIntent.setAction(ACTION_CONNECT);
        return myIntent;
        //startService(myIntent);
    }
}
