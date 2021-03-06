/*
* Copyright (C) 2015 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.omnirom.omnijawsclient;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class OmniJawsClient {
    private static final String TAG = "WeatherService:OmniJawsClient";
    private static final String BROADCAST_INTENT = "org.omnirom.omnijaws.BROADCAST_INTENT";
    public static final String SERVICE_PACKAGE = "org.omnirom.omnijaws";
    public static final Uri WEATHER_URI
            = Uri.parse("content://org.omnirom.omnijaws.provider/weather");
    private static final String ICON_PACKAGE_DEFAULT = "org.omnirom.omnijaws";

    public static final String[] WEATHER_PROJECTION = new String[]{
            "city",
            "wind",
            "condition_code",
            "temperature",
            "humidity",
            "condition",
            "forecast_low",
            "forecast_high",
            "forecast_condition",
            "forecast_condition_code",
            "time_stamp",
            "forecast_date"
    };

    public static class WeatherInfo {
        public String city;
        public String wind;
        public int conditionCode;
        public String temp;
        public String humidity;
        public String condition;
        public String timeStamp;
        public List<DayForecast> forecasts;
        
        public String toString() {
            return city + ":" + timeStamp + ": " + wind + ":" +conditionCode + ":" + temp + ":" + humidity + ":" + condition + ":" + forecasts;
        }
    }

    public static class DayForecast {
        public String low;
        public String high;
        public int conditionCode;
        public String condition;
        public String date;
        
        public String toString() {
            return "[" + low + ":" + high + ":" +conditionCode + ":" + condition + ":" + date + "]";
        }
    }

    private Context mContext;
    private Handler mHandler = new Handler();
    private WeatherContentObserver mContentObserver = new WeatherContentObserver(mHandler);
    private WeatherInfo mCachedInfo;
    private boolean mEnabled;
    private Resources mRes;
    private String mPackageName;
    private String mSettingHeaderPackage;

    private final class WeatherContentObserver extends ContentObserver {
        WeatherContentObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "onChange " + WEATHER_URI);
            queryWeather();
            ((MainActivity)mContext).update();
        }
    }

    public OmniJawsClient(Context context) {
        mContext = context;
        mEnabled = isOmniJawsServiceInstalled();
        if (mEnabled) {
            loadDefaultIconsPackage();
            mContext.getContentResolver().registerContentObserver(WEATHER_URI, false, mContentObserver);
            queryWeather();
        }
    }

    public void forceUpdate() {
        if (mEnabled) {
            Intent updateIntent = new Intent(Intent.ACTION_MAIN)
                    .setClassName("org.omnirom.omnijaws", "org.omnirom.omnijaws.WeatherService");
            updateIntent.setAction("org.omnirom.omnijaws.ACTION_UPDATE");
            updateIntent.putExtra("force", true);
            mContext.startService(updateIntent);
        }
    }

    public void startSettings() {
        if (mEnabled) {
            Intent settings = new Intent(Intent.ACTION_MAIN)
                    .setClassName("org.omnirom.omnijaws", "org.omnirom.omnijaws.SettingsActivity");
            mContext.startActivity(settings);
        }
    }

    public WeatherInfo getWeatherInfo() {
        return mCachedInfo;
    }

    private void queryWeather() {
        Cursor c = mContext.getContentResolver().query(WEATHER_URI, WEATHER_PROJECTION,
                null, null, null);
        if (c != null) {
            Log.d(TAG, "queryWeather " + WEATHER_URI);
            try {
                mCachedInfo = new WeatherInfo();
                int count = c.getCount();
                List<DayForecast> forecastList = new ArrayList<DayForecast>();
                int i = 0;
                for (i = 0; i < count; i++) {
                    c.moveToPosition(i);
                    if (i == 0) {
                        mCachedInfo.city = c.getString(0);
                        mCachedInfo.wind = c.getString(1);
                        mCachedInfo.conditionCode = c.getInt(2);
                        mCachedInfo.temp = c.getString(3);
                        mCachedInfo.humidity = c.getString(4);
                        mCachedInfo.condition = c.getString(5);
                        mCachedInfo.timeStamp = c.getString(10);
                    } else {
                        DayForecast day = new DayForecast();
                        day.low = c.getString(6);
                        day.high = c.getString(7);
                        day.condition = c.getString(8);
                        day.conditionCode = c.getInt(9);
                        day.date = c.getString(11);
                        forecastList.add(day);
                    }
                }
                mCachedInfo.forecasts = forecastList;
                Log.d(TAG, "queryWeather " + mCachedInfo);
            } finally {
                c.close();
            }
        }
    }

    private void loadDefaultIconsPackage() {
        Log.i(TAG, "Load default header pack");
        mPackageName = ICON_PACKAGE_DEFAULT;
        mSettingHeaderPackage = mPackageName;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            mRes = packageManager.getResourcesForApplication(mPackageName);
        } catch (Exception e) {
            mRes = null;
        }
        if (mRes == null) {
            Log.w(TAG, "No default package found");
        }
    }

    private int getWeatherImageResource(int conditionCode) {
        if (mRes != null) {
            int resId = mRes.getIdentifier("weather_" + conditionCode, "drawable", mPackageName);
            if (resId != 0) {
                return resId;
            }
            resId = mRes.getIdentifier("weather_na", "drawable", mPackageName);
            if (resId != 0) {
                return resId;
            }
        }
        // last resort
        return R.drawable.weather_na;
    }

    public Drawable getWeatherConditionImage(int conditionCode) {
        int resId = getWeatherImageResource(conditionCode);
        return mRes.getDrawable(resId);
    }

    private boolean isOmniJawsServiceInstalled() {
        final PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(SERVICE_PACKAGE, PackageManager.GET_ACTIVITIES);
            int enabled = pm.getApplicationEnabledSetting(SERVICE_PACKAGE);
            return enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
                enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
