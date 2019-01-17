package de.marvincs.clak;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;


import static de.marvincs.clak.Network.*;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RequestService extends JobIntentService {

    public static final String DATAMANAGER = "DataManager";
    protected static final String ACTION_CHECK = "CHECK_CONNECTION";
    protected static final String ACTION_CONNECT = "CONNECT";
    protected static final String ACTION_SAVE_CREDENTIALS = "SAVE";

    private static final int UNIQUE_JOB_ID = 1337;

    /*static void enqueueWork(Context ctxt, Intent intent) {
        enqueueWork(ctxt, RequestService.class, UNIQUE_JOB_ID, new Intent(ctxt, RequestService.class));
    }*/

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i("MCSAPP - RequestService", "Called onHandleWork");
        DataManager dataManager = DataManager.getInstance(PreferenceManager.getDefaultSharedPreferences(this));
        final String action = intent.getAction();
        final String network = intent.getStringExtra(NETWORKNAME);
        if (check_rub_network(getApplicationContext(), network)) {
            if (ACTION_CHECK.equals(action)) {
                handleActionCheck();
            } else if (ACTION_CONNECT.equals(action)) {
                handleActionFetchIP();
                handleActionLogin(intent.getStringExtra(LOGINID), intent.getStringExtra(PASSWORD), intent.getStringExtra(IPADRESS));
            } else if (ACTION_SAVE_CREDENTIALS.equals(action)) {
                handleActionSaveCredentials((DataManager) intent.getSerializableExtra(DATAMANAGER));
            }
        }
    }


    /**
     * Handle Login
     *
     * @param loginid
     * @param password
     * @param ip
     */
    private void handleActionLogin(String loginid, String password, String ip) {
    }


    /**
     * Handle action Fetch IP in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchIP() {
    }


    /**
     * Handle action Check in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheck() {
    }


    private void handleActionSaveCredentials(DataManager dataManager) {
        dataManager.setPreferences(PreferenceManager.getDefaultSharedPreferences(this));
        dataManager.save(this);
        Log.i("MCSAPP - RequestService", dataManager.getLoginID());

    }

}
