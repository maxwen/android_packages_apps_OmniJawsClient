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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private FrameLayout mCurrentImages;
    private FrameLayout mForcastImages;
    private Animator mFlipAnimation;
    private Animator mFlipAnimationReverse;
    private TextView mCurrentCity;
    private TextView mCurrentWind;
    private TextView mCurrentHumidity;
    private ImageView mCurrentBg;
    private ImageView mForcastBg;

    private boolean mShowCurrent = true;
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
        mCurrentCity = (TextView) findViewById(R.id.current_weather_city);
        mCurrentWind = (TextView) findViewById(R.id.current_weather_wind);
        mCurrentHumidity = (TextView) findViewById(R.id.current_weather_humidity);
        mCurrentBg = (ImageView) findViewById(R.id.current_weather_bg);
        mForcastBg = (ImageView) findViewById(R.id.forcast_weather_bg);
        
        mCurrentImages = (FrameLayout) findViewById(R.id.current_images);
        mCurrentImages.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                flipWeather();
                mShowCurrent = false;
            }});
        mForcastImages = (FrameLayout) findViewById(R.id.forcast_images);
        mForcastImages.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                flipWeather();
                mShowCurrent = true;
            }});
        mForcastImages.setVisibility(View.GONE);
        mCurrentImages.setVisibility(View.VISIBLE);
    }

    private void flipWeather() {
        if (mFlipAnimation != null) {
            mFlipAnimation.cancel();
        }

        int cx = mCurrentImages.getMeasuredWidth() / 2;
        int cy = mCurrentImages.getMeasuredHeight() / 2;
        
        int radius = mCurrentImages.getWidth() / 2;

        // create the animator for this view (the start radius is zero)
        if (mShowCurrent) {
            mFlipAnimation = ViewAnimationUtils.createCircularReveal(mForcastImages,
                    cx, cy, 0, radius);
        } else {
            mFlipAnimation = ViewAnimationUtils.createCircularReveal(mForcastImages,
                    cx, cy, radius, 0);
        }
        mFlipAnimation.setDuration(500);
        mFlipAnimation.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                if (mShowCurrent) {
                    mForcastImages.setVisibility(View.VISIBLE);
                } else {
                    mCurrentImages.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mShowCurrent) {
                    mForcastImages.setVisibility(View.GONE);
                } else {
                    mCurrentImages.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }});

        mFlipAnimation.start();
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
            mCurrent.setImageDrawable(getWeatherImage(data.conditionCode, null, data.temp));
            mCurrentCity.setText(data.city);
            mCurrentWind.setText(data.wind);
            mCurrentHumidity.setText(data.humidity);
            mCurrentBg.setImageDrawable(getResources().getDrawable(getBgImageForCondition(data.conditionCode)));
            mForcastBg.setImageDrawable(getResources().getDrawable(getBgImageForCondition(data.conditionCode)));

            mForcast1.setImageDrawable(getWeatherImage(data.forecasts.get(1).conditionCode,
                    data.forecasts.get(1).low, data.forecasts.get(1).high));
            mForcast2.setImageDrawable(getWeatherImage(data.forecasts.get(2).conditionCode,
                    data.forecasts.get(2).low, data.forecasts.get(2).high));
            mForcast3.setImageDrawable(getWeatherImage(data.forecasts.get(3).conditionCode,
                    data.forecasts.get(3).low, data.forecasts.get(3).high));
            mForcast4.setImageDrawable(getWeatherImage(data.forecasts.get(4).conditionCode,
                    data.forecasts.get(4).low, data.forecasts.get(4).high));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    private Drawable getWeatherImage(int conditionCode, String min, String max) {
        Drawable weatherImage = mClient.getWeatherConditionImage(conditionCode);
        Drawable d = overlay(getResources(), weatherImage, min, max);
        return d;
    }

    private Drawable overlay(Resources resources, Drawable image, String min, String max) {
        final Canvas canvas = new Canvas();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        final float density = resources.getDisplayMetrics().density;
        final int footerHeight = Math.round(resources.getDimensionPixelSize(R.dimen.weather_icon_footer) * density);
        final int imageWidth = image.getIntrinsicWidth();
        final int imageHeight = image.getIntrinsicHeight();
        final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        Typeface font = Typeface.create("sans", Typeface.NORMAL);
        textPaint.setTypeface(font);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        final int textSize = Math.round(14 * density);
        textPaint.setTextSize(textSize);
        textPaint.setShadowLayer(5.0f, 0.0f, 0.0f, Color.BLACK);
        final int height = imageHeight + footerHeight;
        final int width = imageWidth;

        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bmp);
        canvas.drawBitmap(((BitmapDrawable)image).getBitmap(), 0, 0, null);

        String str = null;
        if (min != null) {
            str = min +"-"+max;
        } else {
            str = max;
        }
        Rect bounds = new Rect();
        textPaint.getTextBounds(str, 0, str.length(), bounds);
        canvas.drawText(str, width / 2 - bounds.width() / 2, height - textSize / 2, textPaint);

        return new BitmapDrawable(resources, bmp);
    }

    private int getBgImageForCondition(int conditionCode) {
        switch(conditionCode) {
        case 31:
        case 32:
        case 33:
        case 34:
        case 36:
            return R.drawable.bg_clear;
        case 5:
        case 6:
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
        case 35:
        case 40:
            return R.drawable.bg_rain;
        case 7:
        case 13:
        case 14:
        case 15:
        case 16:
        case 17:
        case 18:
        case 41:
        case 42:
        case 43:
        case 46:
            return R.drawable.bg_snow;
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 37:
        case 38:
        case 39:
        case 45:
        case 47:
            return R.drawable.bg_storm;
        case 19:
        case 20:
        case 21:
        case 22:
            return R.drawable.bg_fog;
        }
        return R.drawable.bg_clouds;
    }
}
