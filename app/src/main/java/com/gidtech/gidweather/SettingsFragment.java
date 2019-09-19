package com.gidtech.gidweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.gidtech.gidweather.data.WeatherPreferences;
import com.gidtech.gidweather.sync.SyncUtils;

/**
 * Created by gid on 4/23/2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener{

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if(prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries() [prefIndex]);
            }
        }else {
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_list);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();
        for (int i=0; i < count; i++) {
            Preference p = preferenceScreen.getPreference(i);
            if (! (p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(),"");
                setPreferenceSummary(p,value);
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();

        if (key.equals(getString(R.string.pref_location_key))) {
            WeatherPreferences.resetLocationCoordinates(activity);
            //ProgressDialog progressDialog = ProgressDialog.show(getActivity(),"PLEASE WAIT.","FETCHING DATA.",true);
            //ProgressDialog progressDialog = new ProgressDialog(SettingsActivity.this);
            SyncUtils.startImmediateSync(activity);
            //progressDialog.dismiss();
        }else if (key.equals(getString(R.string.pref_units_key))) {
            activity.getContentResolver().notifyChange(WContract.WeatherEntry.CONTENT_URI,null);
        }
        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary(preference,sharedPreferences.getString(key,""));
            }
        }
    }
}
