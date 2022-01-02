package com.stickynote.ttmiwn.myweatherinfo;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

//  (1)時間の型を設定
//      (2) LDTime型に変換	(3) タイムゾーンをUTCに設定	(4) ZoneをJSTに変換+9時間
//      (5) 日本時間に変換されたDATEを(1)で指定した形にStringで代入 "T[Asia/Tokyo]"等を省く
//        日付データ 「2021-12-09 06:00:00」を「12/09 06時」に加工


// JSONから抜き出した時間を意味する数列を、加工・変換するクラス
// Java8.0以降から追加されたため @annotation が付属される

final class TimeZoneChange {

    final String DEBUG_TAG = "WetherInfo";

    @RequiresApi(api = Build.VERSION_CODES.O)

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

//    受け取った数列を、日本時間に変換し"MM/dd HH時"に加工する
    @RequiresApi(api = Build.VERSION_CODES.O)
    String ZoneChangeCut(String str) {
        if(str != null) {
            try {
                LocalDateTime localDate = LocalDateTime.parse(str, dtf);
                ZonedDateTime utcDate = ZonedDateTime.of(localDate, ZoneId.of("UTC"));
                ZonedDateTime jstDate = utcDate.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));

                DateTimeFormatter cutT = DateTimeFormatter.ofPattern("MM/dd\nHH時");
                String cutTime = jstDate.format(cutT);

                return cutTime;
            } catch (DateTimeParseException e) {
                Log.w(DEBUG_TAG, "時間の値が不正です", e);
                return "時間\n不明";
            }
        } else {
            return "時間\n取得失敗";
        }
    }

//    "HH時"だけに加工
    @RequiresApi(api = Build.VERSION_CODES.O)
    String ZoneChangeTime(String str) {

        LocalDateTime localDate = LocalDateTime.parse(str, dtf);
        ZonedDateTime utcDate = ZonedDateTime.of(localDate, ZoneId.of("UTC"));
        ZonedDateTime jstDate = utcDate.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));

        //日付データ 「2021-12-09 06:00:00」を「12/09 06時」に加工
        DateTimeFormatter timeOnly = DateTimeFormatter.ofPattern("HH時");
        String cutTimeOnly = jstDate.format(timeOnly);

        return cutTimeOnly;
    }

//    EPOC Unixタイムスタンプ を日時(LocalDateTime) に変換し、"HH:mm:ss"に加工
    @RequiresApi(api = Build.VERSION_CODES.O)
    String epochUnixTimeChange(String time) {

        try {

            Long log = Long.parseLong(time);
            Instant instant = Instant.ofEpochSecond(log);

            LocalDateTime localDateEp = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            ZonedDateTime utcDateEp = ZonedDateTime.of(localDateEp, ZoneId.of("UTC"));
            ZonedDateTime jstDateEp = utcDateEp.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));

            DateTimeFormatter sunt = DateTimeFormatter.ofPattern("HH:mm:ss");
            String cutTimeEp = jstDateEp.format(sunt);

            return cutTimeEp;

        }catch(NumberFormatException e){
            Log.w(DEBUG_TAG,"時間の数値がnullか不正です", e);
            return "時間、取得失敗";
        }
    }
}
