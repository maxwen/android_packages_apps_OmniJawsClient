/*
 *  Copyright (C) 2015 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omnijawsclient;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity {
    private TextView mTextView;
    private OmniJawsClient mClient;
    private ImageView mCurrent;
    private ImageView mForcast1;
    private ImageView mForcast2;
    private ImageView mForcast3;
    private ImageView mForcast4;
    private ImageView mForcast5;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = new OmniJawsClient(this);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textView);
        mCurrent = (ImageView) findViewById(R.id.current_weather);
        mForcast1 = (ImageView) findViewById(R.id.forcast_day1);
        mForcast2 = (ImageView) findViewById(R.id.forcast_day2);
        mForcast3 = (ImageView) findViewById(R.id.forcast_day3);
        mForcast4 = (ImageView) findViewById(R.id.forcast_day4);
        mForcast5 = (ImageView) findViewById(R.id.forcast_day5);
    }
    
    public void onUpdatePressed(View v) {
        mClient.forceUpdate();
    }
    
    public void onSettingsPressed(View v) {
        mClient.startSettings();
    }
    
    public void update() {
        OmniJawsClient.WeatherInfo data = mClient.getWeatherInfo();
        if (data != null) {
            mTextView.setText(data.toString());
            mCurrent.setImageResource(getWeatherIconResource(data.conditionCode));
            mForcast1.setImageResource(getWeatherIconResource(data.forecasts.get(0).conditionCode));
            mForcast2.setImageResource(getWeatherIconResource(data.forecasts.get(1).conditionCode));
            mForcast3.setImageResource(getWeatherIconResource(data.forecasts.get(2).conditionCode));
            mForcast4.setImageResource(getWeatherIconResource(data.forecasts.get(3).conditionCode));
            mForcast5.setImageResource(getWeatherIconResource(data.forecasts.get(4).conditionCode));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    private int getWeatherIconResource(int conditionCode) {
        final Resources res = getResources();
        final int resId = res.getIdentifier("weather_color_" + conditionCode,
                "drawable", getPackageName());

        if (resId != 0) {
            return resId;
        }

        return R.drawable.weather_na;
    }
}
