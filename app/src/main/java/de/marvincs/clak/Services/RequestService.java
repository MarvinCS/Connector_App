package de.marvincs.clak.Services;

import android.app.IntentService;
import android.content.Intent;


import de.marvincs.clak.Utils.MyAlarmManager;
import de.marvincs.clak.Utils.MyNotificationManager;
import de.marvincs.clak.Utils.Network;
import de.marvincs.clak.Utils.DataManager;

import static de.marvincs.clak.Utils.Network.*;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RequestService extends IntentService {

    public static final String ACTION_CONNECT = "CONNECT";
    public static final String ACTION_RESET_ALARMS = "ACTION_RESET_ALARMS";

    private DataManager dataManager;
    private MyNotificationManager mnm;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RequestService() {
        super("CLAK - Request Service");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        mnm = new MyNotificationManager(this);
        final String action = intent.getAction();
        this.dataManager = new DataManager(this);
        if (dataManager.credentialsStored()) {
            if (ACTION_RESET_ALARMS.equals(action)) {
                this.handleResetAlarms();
            } else if (check_rub_network(this, dataManager.getNetwork()) && ACTION_CONNECT.equals(action)) {
                String ip = handleActionFetchIP();
                if (ip != null) {
                    handleActionLogin(ip);
                } else {
                    mnm.notRUBNetwork(this);
                }
                if (intent.hasExtra("REPEATING")) {
                    this.handleResetAlarms();
                }
            } else {
                mnm.notSelectedWIFI(this);
            }
        }
    }


    /**
     * Handle Login
     *
     * @param ip
     */
    private boolean handleActionLogin(String ip) {
        String answere = Network.login(dataManager.getLoginID(), dataManager.getPassword(), ip);
        if (answere.contains("Authentisierung fehlgeschlagen")) {
            mnm.wrongCredentials(this);
            return false;
        } else if (answere.contains("Authentisierung gelungen")) {
            mnm.connected(this);
            return true;
        }
        return false;
    }


    /**
     * Handle action Fetch IP in the provided background thread with the provided
     * parameters.
     */
    private String handleActionFetchIP() {
        return Network.fetch_ip();
    }

    private void handleResetAlarms() {
        MyAlarmManager.cancelAlarms(this);
        MyAlarmManager.addAlarms(this, dataManager.getTimes());
    }
}
