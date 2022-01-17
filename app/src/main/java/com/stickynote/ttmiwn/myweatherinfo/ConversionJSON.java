package com.stickynote.ttmiwn.myweatherinfo;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// Web APIから取得したJSON文字列・天気情報から、必要な値を取りだしHashMapに登録

final class ConversionJSON {

    final String DEBUG_TAG = "WetherInfo";
    Map<String, String> tempDatas = new HashMap<>();

    public Map<String, String> getMap() {

        return tempDatas;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ConversionJSON(String _result) {

        try {
            // rootJSONオブジェクトを生成。                                └root
            // CityJSONオブジェクト　を rootから取得　改装を一段潜るイメージ  　└CityJSON　
            // 都市名・日の出・日の入りを"getString"で取得　　　　　　　　　　　　  ・sunrise　7:00
            JSONObject rootJSON = new JSONObject(_result);

            JSONObject citydJSON = rootJSON.getJSONObject("city");
            String cityName = citydJSON.getString("name");
            String sunriseEp = citydJSON.getString("sunrise");
            String sunsetEp = citydJSON.getString("sunset");

            //　root下　"list"という名の『全ての時間帯天気詳細』を配列"getJSONArray"で取得
            JSONArray listJSONArray = rootJSON.getJSONArray("list");

            // 上記配列の一つ目"list Array[0]"選択　3時間区切りでもっとも近い時間の天気予報
            JSONObject thisTimeJSON = listJSONArray.getJSONObject(0);
            String infoTime = thisTimeJSON.getString("dt_txt"); // 時刻習得
            // "main"オブジェクト　気温、湿度　を取得
            JSONObject mainJSON = thisTimeJSON.getJSONObject("main");
            String temper = mainJSON.getString("temp");
            // 謎のweather配列を取得、一個目をオブジェクトに
            JSONArray weatherJSONArray = thisTimeJSON.getJSONArray("weather");
            JSONObject weatherJSON = weatherJSONArray.getJSONObject(0);
            // 現在の天気情報文字列を取得
            String description = weatherJSON.getString("description");
            String icon = weatherJSON.getString("icon");

            // 仮データMAPに登録、"sunrise"等、時間は加工が必要
            tempDatas.put("cityName", cityName);
            tempDatas.put("description0", description);
            tempDatas.put("temper0", temper);
            tempDatas.put("icon0", icon);

            // timezone 加工して登録
            TimeZoneChange tzc = new TimeZoneChange();
            String sunrise = tzc.epochUnixTimeChange(sunriseEp);
            String sunset = tzc.epochUnixTimeChange(sunsetEp);

            tempDatas.put("sunrise", sunrise);
            tempDatas.put("sunset", sunset);
            // 時間の表示
            String nearTime = tzc.ZoneChangeCut(infoTime);

            //　↑　ここまで、都市の情報登録　　↓　ここから、その都市の3時間区切りの天気

            for (int i = 1; 6> i; i++) {
                thisTimeJSON = listJSONArray.getJSONObject(i);  //listarray[1]3jkikanngo
                mainJSON = thisTimeJSON.getJSONObject("main"); //main:[
                // Mapに　　　　 キー値 "temp + 1~5"　, 値　”JSON　-　main　(｛　温度　｝に”℃”付けて)登録　”
                tempDatas.put(("temper" + i), (mainJSON.getString("temp") +" ℃"));
                weatherJSONArray = thisTimeJSON.getJSONArray("weather");
                weatherJSON = weatherJSONArray.getJSONObject(0);    //一つしか無い謎配列なので0固定
                tempDatas.put(("description" + i), (weatherJSON.getString("description")));
                tempDatas.put(("icon" + i), (weatherJSON.getString("icon")));
                tempDatas.put(("dTime" + i), (thisTimeJSON.getString("dt_txt")));
            }
             if (cityName.equals("札幌市")) {
                tempDatas.put("nearTime", nearTime);
                tempDatas.put("hoursLater6", (tzc.ZoneChangeCut(tempDatas.get("dTime2"))));
                tempDatas.put("hoursLater3", (tzc.ZoneChangeTime(tempDatas.get("dTime1"))));
                tempDatas.put("hoursLater9", (tzc.ZoneChangeTime(tempDatas.get("dTime3"))));
                tempDatas.put("hoursLater12", (tzc.ZoneChangeTime(tempDatas.get("dTime4"))));
                tempDatas.put("hoursLater15", (tzc.ZoneChangeTime(tempDatas.get("dTime5"))));
            }

        } catch (JSONException ex) {
            Log.e(DEBUG_TAG, "JSON解析失敗", ex);
        } catch (NullPointerException ex){
            Log.w(DEBUG_TAG, "JSONの値にnull有り、キーname確認", ex);
        }
    }
}