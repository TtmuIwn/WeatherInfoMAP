package com.stickynote.ttmiwn.myweatherinfo;

//　💮　無事稼働中 下記方法で変数一つ削れるが、難読
// if (  (tmap.get(("icon")+i)).equals("01d") || (tmap.get(("icon")+i)).equals("01n")  ) {
//        weatherIDs[i] = R.drawable.weather_01d;
//        }


import android.util.Log;

import java.util.HashMap;
import java.util.Map;

final class IconSet {

    public int[] iconSet(Map<String,String> tmap){

        String icon;

        int[] weatherIDs = new int[6];
        for(int i = 0 ; 6 > i; i++) {
            icon = tmap.get(("icon")+i);

            if(icon != null) {

                if (icon.equals("01d") || icon.equals("01n")) {
                    weatherIDs[i] = R.drawable.weather_01d;
                } else if (icon.equals("02d") || icon.equals("02n")) {
                    weatherIDs[i] = R.drawable.weather_02d;
                } else if (icon.equals("03d") || icon.equals("03n")) {
                    weatherIDs[i] = R.drawable.weather_03d;
                } else if (icon.equals("04d") || icon.equals("04n")) {
                    weatherIDs[i] = R.drawable.weather_04d;
                } else if (icon.equals("09d") || icon.equals("09n")) {
                    weatherIDs[i] = R.drawable.weather_09d;
                } else if (icon.equals("10d") || icon.equals("10n")) {
                    weatherIDs[i] = R.drawable.weather_10d;
                } else if (icon.equals("11d") || icon.equals("11n")) {
                    weatherIDs[i] = R.drawable.weather_11d;
                } else if (icon.equals("13d") || icon.equals("13n")) {
                    weatherIDs[i] = R.drawable.weather_13d;
                } else if (icon.equals("50d") || icon.equals("50n")) {
                    weatherIDs[i] = R.drawable.weather_50d;
                } else {
                    Log.w("WetherInfo", "Iconの値が範囲外、もしくは不正です");
                    weatherIDs[i] = 0;
                }

            } else {
                weatherIDs[i] = R.drawable.weather_frog;
                Log.w("WetherInfo", "Iconの値がnullです");
            }
        }
        return weatherIDs;
    }
}