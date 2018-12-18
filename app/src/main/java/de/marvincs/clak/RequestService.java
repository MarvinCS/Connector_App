package de.marvincs.clak;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RequestService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    protected static final String ACTION_IP = "FETCH_IP";
    protected static final String ACTION_CHECK = "CHECK_CONNECTION";
    protected static final String ACTION_CONNECT = "CONNECT";

    private static final String IP_URL = "https://login.rz.ruhr-uni-bochum.de/cgi-bin/start";
    private static final String CONNECT_URL = "https://login.rz.ruhr-uni-bochum.de/cgi-bin/laklogin";
    private static final String CHECK_URL = "http://google.com";

    protected static final Pattern IP_REGEX = Pattern.compile("(?<=name=\"ipaddr\" value=\")[\\d\\.]+");
    protected static final String LOGINID = "loginid";
    protected static final String PASSWORD = "password";
    protected static final String IPADRESS = "ipaddr";
    protected static final String HTTP_ACTION = "action";
    protected static final String NETWORKNAME = "networkname";
    protected static String ip;


    public RequestService() {
        super("RequestService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final String network = intent.getStringExtra(NETWORKNAME);
            if (check_rub_network(network)) {
                if (ip == null) {
                    try {
                        handleActionFetchIP();
                        sleep(10000);
                        Log.e("", ip);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (ACTION_IP.equals(action)) {
                    handleActionFetchIP();
                } else if (ACTION_CHECK.equals(action)) {
                    handleActionCheck();
                } else if (ACTION_CONNECT.equals(action)) {
                    final String loginid = intent.getStringExtra(LOGINID);
                    final String password = intent.getStringExtra(PASSWORD);
                    handleActionLogin(loginid, password);
                }
            }
        }
    }

    private void handleActionLogin(String loginid, String password) {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(CONNECT_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            String data = getRequestBody(loginid, password);
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            Log.e("", response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getRequestBody(String username, String password) {
        StringBuilder sb = new StringBuilder();
        sb.append(LOGINID);
        sb.append("=");
        sb.append(username);
        sb.append("&");
        sb.append(PASSWORD);
        sb.append("=");
        sb.append(password);
        sb.append("&");
        sb.append(IPADRESS);
        sb.append("=");
        sb.append(ip);
        sb.append("&");
        sb.append(HTTP_ACTION);
        sb.append("=");
        sb.append("Login");
        return sb.toString();
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchIP() {
        URL url = null;
        try {
            url = new URL(IP_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            urlConnection.setRequestProperty("Accept", "*/*");
            urlConnection.setRequestProperty("connection", "close");

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            String line;

            while ((line = r.readLine()) != null) {
                if (line.contains("name=\"ipaddr\"")) {
                    total.append(line);
                    break;
                }
            }

            Matcher m = IP_REGEX.matcher(total.toString());
            if (m.find()) {
                ip = m.group(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

    }


    /**
     * Handle action Check in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheck() {
        URL url = null;
        try {
            url = new URL(CHECK_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("HEAD");
            urlConnection.setConnectTimeout(10000); //set timeout to 5 seconds
            int statusCode = urlConnection.getResponseCode();
            Log.d("", "" + statusCode);
        } catch (java.net.SocketTimeoutException e) {
            Intent intent = new Intent(getApplicationContext(), RequestService.class);
            intent.setAction(RequestService.ACTION_CONNECT);
            startService(intent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public boolean check_rub_network(String network) {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            return false;
        }
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        if (wifiInfo != null) {
            NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
            if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                String ssid = wifiInfo.getSSID().trim();
                ssid = ssid.substring(1, ssid.length());
                ssid = ssid.substring(0, ssid.length() - 1);
                return ssid.toLowerCase().equals(network.trim().toLowerCase());
            }
        }
        return false;
    }
}
