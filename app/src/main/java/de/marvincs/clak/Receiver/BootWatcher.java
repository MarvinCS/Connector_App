package de.marvincs.clak.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.marvincs.clak.Services.RequestService;

public class BootWatcher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, RequestService.class);
        myIntent.setAction(RequestService.ACTION_RESET_ALARMS);
        context.startService(myIntent);
    }

}
