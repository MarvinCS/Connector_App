package de.marvincs.clak.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import de.marvincs.clak.R;


/**
 * Datamanagement
 */
public class DataManager implements Parcelable {

    private static final String sharedPreferencesName = "Prefs";
    private String loginid, password, network;
    private List<String> times;


    public DataManager(Context c) {
        this.load(c);
    }

    public DataManager(Parcel in) {
        this.loginid = in.readString();
        this.password = in.readString();
        this.network = in.readString();
        times = new ArrayList<>();
        in.readStringList(this.times);
    }

    public static final Creator<DataManager> CREATOR = new Creator<DataManager>() {
        @Override
        public DataManager createFromParcel(Parcel in) {
            return new DataManager(in);
        }

        @Override
        public DataManager[] newArray(int size) {
            return new DataManager[size];
        }
    };

    public void load(Context c) {
        this.times = new ArrayList<>();
        c.getSharedPreferences(sharedPreferencesName, Context.MODE_MULTI_PROCESS);
        this.loadCredentials(c);
        this.loadTimes(c);
    }

    private void loadCredentials(Context c) {
        String defaultValue = c.getResources().getString(R.string.credentials_default);
        SharedPreferences sp = c.getSharedPreferences(sharedPreferencesName, Context.MODE_MULTI_PROCESS);

        String loginID = sp.getString(c.getString(R.string.credentials_loginID), defaultValue);
        if (!loginID.equals(defaultValue)) {
            setLoginID(loginID);
        }

        String password = sp.getString(c.getString(R.string.credentials_password), defaultValue);
        if (!password.equals(defaultValue)) {
            setPassword(password);
        }

        String network = sp.getString(c.getString(R.string.credentials_network), defaultValue);
        if (!network.equals(defaultValue)) {
            setNetwork(network);
        }

    }

    private void loadTimes(Context c) {
        String defaultValue = c.getResources().getString(R.string.credentials_default);
        SharedPreferences sp = c.getSharedPreferences(sharedPreferencesName, Context.MODE_MULTI_PROCESS);

        String serialized = sp.getString(c.getString(R.string.times), defaultValue);
        if (serialized != defaultValue) {
            List<String> list = Arrays.asList(TextUtils.split(serialized, ","));
            this.times = new ArrayList<>(list);
        }
    }


    public void save(Context c) {
        SharedPreferences sp = c.getSharedPreferences(sharedPreferencesName, Context.MODE_MULTI_PROCESS);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(c.getString(R.string.credentials_loginID), getLoginID());
        editor.putString(c.getString(R.string.credentials_password), getPassword());
        editor.putString(c.getString(R.string.credentials_network), getNetwork());

        editor.putString(c.getString(R.string.times), TextUtils.join(",", this.times));
        editor.apply();
    }

    public String getLoginID() {
        return this.loginid;
    }

    public String getPassword() {
        return this.password;
    }

    public String getNetwork() {
        return this.network;
    }

    public void setLoginID(String loginID) {
        this.loginid = loginID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public boolean addTime(String time) {
        if (!containsTime(time)) {
            this.times.add(time);
            return true;
        }
        return false;
    }

    public boolean containsTime(String time) {
        for (int i = 0; i < this.times.size(); i++) {
            if (this.times.get(i).equals(time)) {
                return true;
            }
        }
        return false;
    }

    public boolean removeTime(Calendar e) {
        for (int i = 0; i < this.times.size(); i++) {
            if (this.times.get(i).equals(calendarToTime(e))) {
                this.times.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removeTime(String time) {
        for (int i = 0; i < this.times.size(); i++) {
            if (this.times.get(i).equals(time)) {
                this.times.remove(i);
                return true;
            }
        }
        return false;
    }

    public static String calendarToTime(Calendar e) {
        int hour = e.get(Calendar.HOUR_OF_DAY);
        int min = e.get(Calendar.MINUTE);
        return (hour + ":" + (min >= 10 ? min : "0" + min));
    }

    public List<String> getTimes() {
        return this.times;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(loginid);
        dest.writeString(password);
        dest.writeString(network);
        dest.writeStringList(this.times);
    }

    public boolean credentialsStored() {
        return loginid != null && password != null && network != null;
    }
}
