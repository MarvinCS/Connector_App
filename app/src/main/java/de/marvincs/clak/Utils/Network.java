package de.marvincs.clak.Utils;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Network {
    // URLs
    private static final String IP_URL = "https://login.rz.ruhr-uni-bochum.de/cgi-bin/start";
    private static final String CONNECT_URL = "https://login.rz.ruhr-uni-bochum.de/cgi-bin/laklogin";

    // Parameter
    static final String LOGINID = "loginid";
    static final String PASSWORD = "password";
    static final String IPADRESS = "ipaddr";
    static final String ACTION = "action";
    static final String HTTP_ACTION = "action";
    static final String NETWORKNAME = "networkname";

    // Regex to get IP
    static final Pattern IP_REGEX = Pattern.compile("(?<=name=\"ipaddr\" value=\")[\\d\\.]+");
    static String ip;


    private static String getRequestBody(String username, String password, String ip) {
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

    public static boolean check_rub_network(Context context, String network) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            return false;
        }
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        if (wifiInfo != null) {
            NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
            if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                String ssid = wifiInfo.getSSID().trim();
                ssid = ssid.substring(1);
                ssid = ssid.substring(0, ssid.length() - 1);
                return ssid.toLowerCase().equals(network.trim().toLowerCase());
            }
        }
        return false;
    }


    public static String login(String loginid, String password, String ip) {
        Log.i("MCSAPP - Network", "Logging in");
        String answere = "";
        URL url;
        HttpURLConnection connection = null;
        int responseCode = 0;
        try {
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
            String credentials = getRequestBody(loginid, password, ip);
            byte[] credentialsInBytes = credentials.getBytes("UTF-8");
            OutputStream os = connection.getOutputStream();
            os.write(credentialsInBytes);
            os.close();


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
            responseCode = connection.getResponseCode();
            answere = response.toString();
            Log.i("MCSAPP", response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return answere;
    }


    public static String fetch_ip() {
        Log.i("MCSAPP", "fetchingIP");
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
                Log.e("", ip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return ip;
    }
}
