package de.marvincs.clak.Activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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

import java.util.Calendar;
import java.util.List;

import de.marvincs.clak.R;
import de.marvincs.clak.Services.RequestService;
import de.marvincs.clak.Utils.DataManager;
import de.marvincs.clak.Utils.MyAlarmManager;

import static de.marvincs.clak.Utils.Network.check_rub_network;


public class MainActivity extends AppCompatActivity {

    // Edittexts and Buttons in View
    private EditText loginID, password;
    private Button network, addTime, connectNow;
    private BroadcastReceiver wifiReceiver;

    // For WifiPicker
    private WifiManager mWifiManager;


    // Timemanagement
    private ListView timeList;
    private ArrayAdapter<String> listAdapter;

    // DataManager
    private DataManager dataManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        //ActionBar bar = getActionBar();
        //bar.setBackgroundDrawable(new ColorDrawable(Color.BLUE));
        this.dataManager = new DataManager(this);
        this.bindViewElements();
        this.load();
        this.updateTimeList();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.registerReceiver(wifiReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //this.checkBatteryOptimized();
    }

    @Override
    protected void onStop() {
        super.onStop();
        save();
    }

    private void bindViewElements() {
        this.loginID = findViewById(R.id.et_loginID);
        this.password = findViewById(R.id.et_password);
        this.network = findViewById(R.id.btn_wifipicker);
        this.addTime = findViewById(R.id.btn_addTime);
        this.timeList = findViewById(R.id.timeList);
        this.connectNow = findViewById(R.id.btn_connectNow);

        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mWifiManager != null) {
                    List<ScanResult> results = mWifiManager.getScanResults();
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
                        String time = hourOfDay + ":" + (minute >= 10 ? minute : ("0" + minute));
                        if (!dataManager.containsTime(time)) {
                            dataManager.addTime(time);
                            updateTimeList();
                            save();
                            MyAlarmManager.setSavedAlarms(MainActivity.this);
                        } else {
                            Toast.makeText(MainActivity.this, "This time is already in your list.", Toast.LENGTH_LONG).show();
                        }
                    }
                }, hour, minute, true).show();
            }

        });


        this.timeList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String time = (String) timeList.getItemAtPosition(position);
                dataManager.removeTime(time);
                updateTimeList();
                save();
                MyAlarmManager.setSavedAlarms(MainActivity.this);
                return true;
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

        this.connectNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                if (dataManager.credentialsStored()) {
                    if (check_rub_network(getApplicationContext(), dataManager.getNetwork())) {
                        Intent myIntent = new Intent(MainActivity.this, RequestService.class);
                        myIntent.setAction(RequestService.ACTION_CONNECT);
                        startService(myIntent);
                        Toast.makeText(MainActivity.this, "You will be connected in a few seconds", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Sorry, but you are not in your selected network", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enter your access data and choose your network", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void listNetworks() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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

    void updateTimeList() {
        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<>(this, R.layout.simplerow, this.dataManager.getTimes());
        this.timeList.setAdapter(listAdapter);
    }

    private void load() {
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