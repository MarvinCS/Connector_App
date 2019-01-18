package de.marvincs.clak;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


import static de.marvincs.clak.Network.*;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RequestService extends IntentService {

    static final String ACTION_CONNECT = "CONNECT";

    private DataManager dataManager;
    private MyNotificationManager mnm;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RequestService() {
        super("CLAK - Request Service");
        Log.i("MCSAPP - RequestService", "Create Service");
        mnm = new MyNotificationManager(this);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("MCSAPP - RequestService", "Called onHandleWork");
        final String action = intent.getAction();
        this.dataManager = new DataManager(this);
        if (dataManager.credentialsStored()) {
            if (check_rub_network(this, dataManager.getNetwork()) && ACTION_CONNECT.equals(action)) {
                String ip = handleActionFetchIP();
                Log.i("MCSAPP - RequestService", "ip: " + ip);
                if (ip != null) {
                    handleActionLogin(ip);
                } else {
                    mnm.notRUBNetwork(this);
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
        Log.i("MCSAPP - RequestService", "Called Login");
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
        Log.i("MCSAPP - RequestService", "Called fetchIP");
        return Network.fetch_ip();
    }
}
