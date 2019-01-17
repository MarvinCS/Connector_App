package de.marvincs.clak;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.marvincs.clak.DataManager.calendarToTime;


public class MainActivity extends AppCompatActivity {

    // Edittexts and Buttons in View
    private EditText loginID, password;
    private Button network, addTime;
    private BroadcastReceiver wifiReceiver;

    // For WifiPicker
    private WifiManager mWifiManager;


    // Timemanagement
    private ListView timeList;
    private ArrayAdapter<String> listAdapter;

    // DataManager
    private DataManager dataManager;


    private static final String IP_URL = "https://login.rz.ruhr-uni-bochum.de/cgi-bin/start";
    private static final Pattern IP_REGEX = Pattern.compile("(?<=name=\"ipaddr\" value=\")[\\d\\.]+");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        this.dataManager = DataManager.getInstance(PreferenceManager.getDefaultSharedPreferences(this));
        this.bindViewElements();
        this.readCredentials();
        this.getTimes();
        this.updateTimeList();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.registerReceiver(wifiReceiver, intentFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        save();
        saveInService();
    }

    private void saveInService() {
        Intent myIntent = new Intent(getApplicationContext(), RequestService.class);
        myIntent.putExtra(RequestService.DATAMANAGER, dataManager);
        myIntent.setAction(RequestService.ACTION_SAVE_CREDENTIALS);
        startService(myIntent);
    }


    private void bindViewElements() {
        this.loginID = findViewById(R.id.et_loginID);
        this.password = findViewById(R.id.et_password);
        this.network = findViewById(R.id.btn_wifipicker);
        this.addTime = findViewById(R.id.btn_addTime);
        this.timeList = findViewById(R.id.timeList);
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

        this.addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar myCalendar = Calendar.getInstance();
                final int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        if (!dataManager.containsTime(c)) {
                            dataManager.addTime(c);
                            updateTimeList();
                        } else {
                            Toast.makeText(MainActivity.this, "This time is already in your list.", Toast.LENGTH_LONG).show();
                        }
                    }
                }, hour, minute, true).show();
            }

        });


        this.timeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(),
                        "Click ListItem Number " + position, Toast.LENGTH_LONG)
                        .show();
            }
        });


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
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item);

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
                        mWifiManager = null;
                    }
                });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                Toast.makeText(getApplicationContext(), "Selected " + strName, Toast.LENGTH_SHORT).show();
                network.setText(strName);
                mWifiManager = null;
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


    private void getTimes() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);
        dataManager.getTimes();
    }

    void updateTimeList() {
        List<String> timeList = new ArrayList<>();
        for (Calendar time : this.dataManager.getTimes()) {
            timeList.add(calendarToTime(time));
        }
        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<>(this, R.layout.simplerow, timeList);
        this.timeList.setAdapter(listAdapter);
    }

    private void readCredentials() {
        dataManager.load(this);
        if (dataManager.getLoginID() != null) {
            this.loginID.setText(dataManager.getLoginID());
        }
        if (dataManager.getPassword() != null) {
            this.password.setText(dataManager.getPassword());
        }
        if (dataManager.getNetwork() != null) {
            this.network.setText(dataManager.getNetwork());
        }
    }

    /**
     * Save Credentials to shared preferences
     */
    private void save() {
        if (!this.loginID.getText().toString().trim().isEmpty()) {
            this.dataManager.setLoginID(this.loginID.getText().toString().trim());
        }
        if (!this.password.getText().toString().trim().isEmpty()) {
            this.dataManager.setPassword(this.password.getText().toString().trim());
        }
        if (!this.network.getText().toString().equals(getString(R.string.wifipicker))) {
            this.dataManager.setNetwork(this.network.getText().toString().trim());
        }
        dataManager.save(this);
    }

}
