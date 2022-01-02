package com.stickynote.ttmiwn.myweatherinfo;

// 💮　無事稼働
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.Map;

// map立ち上げ時に検索する
final class pressButtonProcess {
    static String serchCity = "";
    static String serchSpot = "";

    @RequiresApi(api = Build.VERSION_CODES.N)
    void setSerchCity(Map<String, String> wmap){
        serchCity = wmap.getOrDefault("cityName", "北海道");
    }

    String getSerchCity(){
        return serchCity;
    }

    String getSerchSpot(){
        return serchSpot;
    }

    String serchSpotSwitch(View view) {

        switch(view.getId()){
            case R.id.iBtHotel:
                serchSpot = " ホテル";
                break;
            case R.id.iBtcamp:
                serchSpot = " キャンプ";
                break;
            case R.id.iBtSpa:
                serchSpot = " 日帰り入浴";
                break;
            case R.id.iBtRdSta:
                serchSpot = " 道の駅";
                break;
            case R.id.iBtSpot:
                serchSpot = " 観光地";
                break;
            case R.id.imageBtClear:
                serchSpot = "";
                break;
            default:
                break;
        }
        return serchSpot;
    }
}