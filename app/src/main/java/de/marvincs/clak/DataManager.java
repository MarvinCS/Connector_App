package de.marvincs.clak;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Datamanagement SINGLETON
 */
public class DataManager implements Serializable {

    private static DataManager instance;


    private SharedPreferences sharedPreferences;
    private Map<String, String> credentials;
    private List<Calendar> times;

    static DataManager getInstance(SharedPreferences sharedPref) {
        if (instance == null) {
            instance = new DataManager();
        }
        instance.setPreferences(sharedPref);
        return instance;
    }

    private DataManager() {
    }

    void load(Context c) {
        this.credentials = new HashMap<>();
        this.times = new ArrayList<>();
        this.loadCredentials(c);
        this.loadTimes(c);
    }

    private void loadCredentials(Context c) {
        String defaultValue = c.getResources().getString(R.string.credentials_default);

        String loginID = this.sharedPreferences.getString(c.getString(R.string.credentials_loginID), defaultValue);
        if (!loginID.equals(defaultValue)) {
            this.credentials.put("loginid", loginID);
        }

        String password = this.sharedPreferences.getString(c.getString(R.string.credentials_password), defaultValue);
        if (!password.equals(defaultValue)) {
            this.credentials.put("password", password);
        }

        String network = this.sharedPreferences.getString(c.getString(R.string.credentials_network), defaultValue);
        if (!network.equals(defaultValue)) {
            this.credentials.put("network", network);
        }

    }

    private void loadTimes(Context c) {
        String defaultValue = c.getResources().getString(R.string.credentials_default);

        String serialized = this.sharedPreferences.getString(c.getString(R.string.times), defaultValue);
        if (serialized != defaultValue) {
            List<String> list = Arrays.asList(TextUtils.split(serialized, ","));
            for (String time : list) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.split(":")[0]));
                cal.set(Calendar.MINUTE, Integer.valueOf(time.split(":")[1]));
                this.times.add(cal);
            }
        }
    }


    void save(Context c) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(c.getString(R.string.credentials_loginID), getLoginID());
        editor.putString(c.getString(R.string.credentials_password), getPassword());
        editor.putString(c.getString(R.string.credentials_network), getNetwork());

        List<String> t = new ArrayList<>();
        for (Calendar calendar : this.times) {
            t.add(calendarToTime(calendar));
        }

        editor.putString(c.getString(R.string.times), TextUtils.join(",", t));
        editor.apply();
    }


    void setPreferences(SharedPreferences sharedPref) {
        this.sharedPreferences = sharedPref;
    }

    String getLoginID() {
        return this.credentials.get("loginid");
    }

    String getPassword() {
        return this.credentials.get("password");
    }

    String getNetwork() {
        return this.credentials.get("network");
    }

    void setLoginID(String loginID) {
        this.credentials.put("loginid", loginID);
    }

    void setPassword(String password) {
        this.credentials.put("password", password);

    }

    void setNetwork(String network) {
        this.credentials.put("network", network);

    }

    boolean addTime(Calendar e) {
        if (!containsTime(e)) {
            this.times.add(e);
            return true;
        }
        return false;
    }

    boolean containsTime(Calendar e) {
        for (int i = 0; i < this.times.size(); i++) {
            if (calendarToTime(this.times.get(i)).equals(calendarToTime(e))) {
                return true;
            }
        }
        return false;
    }

    boolean removeTime(Calendar e) {
        for (int i = 0; i < this.times.size(); i++) {
            if (calendarToTime(this.times.get(i)).equals(calendarToTime(e))) {
                this.times.remove(i);
                return true;
            }
        }
        return false;
    }

    static String calendarToTime(Calendar e) {
        int hour = e.get(Calendar.HOUR_OF_DAY);
        int min = e.get(Calendar.MINUTE);
        return (hour + ":" + (min >= 10 ? min : "0" + min));
    }

    List<Calendar> getTimes() {
        return this.times;
    }


}
