package de.marvincs.clak;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {


    private EditText loginID, password, interval;
    private Button fetch_ip, network, start, stop;
    private BroadcastReceiver wifiReceiver;
    private WifiManager mWifiManager;
    private RelativeLayout loading;
    private TextView ip;

    private static final String IP_URL = "https://login.rz.ruhr-uni-bochum.de/cgi-bin/start";
    private static final Pattern IP_REGEX = Pattern.compile("(?<=name=\"ipaddr\" value=\")[\\d\\.]+");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        this.bindViewElements();
        this.readCredentials();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.registerReceiver(wifiReceiver, intentFilter);

    }

    private void bindViewElements() {
        this.loginID = findViewById(R.id.et_loginID);
        this.password = findViewById(R.id.et_password);
        this.ip = findViewById(R.id.tv_ip);
        this.interval = findViewById(R.id.et_update_interval);
        this.fetch_ip = findViewById(R.id.btn_fetch_ip);
        this.network = findViewById(R.id.btn_wifipicker);
        this.start = findViewById(R.id.btn_start);
        this.stop = findViewById(R.id.btn_stop);


        this.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCredentials();
                Intent myIntent = new Intent(getApplicationContext(), RequestService.class);
                myIntent.putExtra(RequestService.NETWORKNAME, network.getText().toString());
                myIntent.putExtra(RequestService.LOGINID, loginID.getText().toString().trim());
                myIntent.putExtra(RequestService.PASSWORD, password.getText().toString().trim());
                myIntent.setAction(RequestService.ACTION_CONNECT);
                startService(myIntent);
            }
        });

        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (mWifiManager != null) {
                    List<ScanResult> results = mWifiManager.getScanResults();
                    for (ScanResult result : results) {
                        Log.d("name", result.toString());
                    }
                    showWifiListDialog(results);
                }
            }
        };

        this.network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    Toast.makeText(getApplicationContext(), getString(R.string.enable_gps), Toast.LENGTH_LONG).show();
                    startActivity(myIntent);
                }
                listNetworks();
            }
        });

    }

    private void listNetworks() {
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiManager.startScan();
        Toast.makeText(this, "Please wait a few seconds", Toast.LENGTH_LONG).show();
    }

    private void showWifiListDialog(List<ScanResult> results) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);

        for (ScanResult r : results) {
            if (r == null || r.SSID == null) continue;
            if ("".equalsIgnoreCase(r.SSID.trim())) continue;
            String name = r.SSID.replace("\"", "");
            arrayAdapter.add(name);
        }


        builderSingle.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        wifiReceiver.abortBroadcast();
                    }
                });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                Toast.makeText(getApplicationContext(), "Selected " + strName, Toast.LENGTH_SHORT).show();
                network.setText(strName);
            }
        });

        AlertDialog dialog = builderSingle.create();
        dialog.show();
    }

    private String fetch_ip() {
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
                return m.group(0);
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "An error has occurred. Please check your internet connection.", Toast.LENGTH_LONG).show();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    private void readCredentials() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.credentials_default);
        String loginID = sharedPref.getString(getString(R.string.credentials_loginID), defaultValue);
        String password = sharedPref.getString(getString(R.string.credentials_password), defaultValue);
        String ip = sharedPref.getString(getString(R.string.credentials_ip), defaultValue);
        String network = sharedPref.getString(getString(R.string.credentials_network), defaultValue);
        int interval = sharedPref.getInt(getString(R.string.credentials_interval), -1);

        if (!loginID.equals(defaultValue)) {
            this.loginID.setText(loginID);
        }
        if (!password.equals(defaultValue)) {
            this.password.setText(password);
        }
        if (!ip.equals(defaultValue)) {
            this.ip.setText(ip);
        } else if (RequestService.ip != null) {
            this.ip.setText(RequestService.ip.trim());
        }
        if (!network.equals(defaultValue)) {
            this.network.setText(network);
        }
        if (interval > 0) {
            this.interval.setText(String.valueOf(interval));
        }
    }

    private void saveCredentials() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (!this.loginID.getText().toString().trim().isEmpty()) {
            editor.putString(getString(R.string.credentials_loginID), this.loginID.getText().toString().trim());
        }
        if (!this.password.getText().toString().trim().isEmpty()) {
            editor.putString(getString(R.string.credentials_password), this.password.getText().toString().trim());
        }
        if (!this.ip.getText().toString().trim().isEmpty()) {
            editor.putString(getString(R.string.credentials_ip), this.ip.getText().toString().trim());
        } else if (RequestService.ip != null) {
            editor.putString(getString(R.string.credentials_ip), RequestService.ip.trim());
        }

        if (!this.interval.getText().toString().trim().isEmpty()) {
            int interval = Integer.parseInt(this.interval.getText().toString().trim());
            if (interval > 0) {
                editor.putInt(getString(R.string.credentials_interval), interval);
            } else {
                Toast.makeText(this, getString(R.string.error_interval), Toast.LENGTH_LONG).show();
            }
        }
        if (!this.network.getText().toString().equals(getString(R.string.wifipicker))) {
            editor.putString(getString(R.string.credentials_network), this.network.getText().toString().trim());
        }
        editor.apply();

    }

}
