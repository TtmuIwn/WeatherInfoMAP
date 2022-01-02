package com.stickynote.ttmiwn.myweatherinfo;

//　忘備録
// リダイレクト拒否設定　con.setInstanceFollowRedirects(false);
//　findbyew のエラーは消せるが、 setする動作は厳しい

// 内容変更時　天気予報、時間取得数増やすなら　int[]回数も

// い：　天気情報を取得する
// ろ：非同期処理を使わないと、外部接続してJSON持ってこれない
//    private class BackgroundTask implements Runnable｛の　run｛　で持ってくる
//      ここだけ、非同期処理すればOK！！！
//      api.openweathermap.org/data/2.5/forecast?q={city name}&appid={API key}
// は　　api.openweathermap.org/data/2.5/forecast?id={city ID}&appid={API key}


// 非同期処理　weastherget　メソッド
//　1）looper 処理終わったら確実にmainに帰ってくるためのキーアイテム　ここが君の帰る場所
//　2）handler　スレッド間のデータ通信をしてくれるオブジェ　指揮官
// 引数で戻るためのキーアイテムlooperを所持
// 合体して　→ Handler handler = new Handler(Looper.getMainLooper());　もok！！

// 1）runnable　class　を作成　実際の処理理内容を「自分で」記入
// 2）ExecuterService　javaで用意されたクラス　シャットダウンできる　new
// 3）submit（送信）method　を実行　引数として（1）を渡す


import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //
    private static final String WEATHERINFO_URL = "https://api.openweathermap.org/data/2.5/forecast?lang=ja&units=metric&cnt=6";
    private static final String APP_ID = BuildConfig.API_KEY;

    Map<String, String> wakkaDatas = new HashMap<>();
    Map<String, String> asahiDatas = new HashMap<>();
    Map<String, String> abashiDatas = new HashMap<>();
    Map<String, String> obihiDatas = new HashMap<>();
    Map<String, String> kushiDatas = new HashMap<>();
    Map<String, String> urakaDatas = new HashMap<>();
    Map<String, String> hakodaDatas = new HashMap<>();
    Map<String, String> sappuDatas = new HashMap<>();

    // アイコンID管理用配列
    int[] wakIcons, asaIcons, abaIcons, obiIcons, kusIcons, uraIcons, hakIcons, sapIcons ;

    //　エラーログ
    private static final String DEBUG_TAG = "WetherInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 各都市ボタンリスナー設定
        findViewById(R.id.wakkaBt).setOnClickListener(MainActivity.this);
        findViewById(R.id.asahiBt).setOnClickListener(MainActivity.this);
        findViewById(R.id.abaBt).setOnClickListener(MainActivity.this);
        findViewById(R.id.obiBt).setOnClickListener(MainActivity.this);
        findViewById(R.id.kusBt).setOnClickListener(MainActivity.this);
        findViewById(R.id.uraBt).setOnClickListener(MainActivity.this);
        findViewById(R.id.hakoBt).setOnClickListener(MainActivity.this);
        findViewById(R.id.sapBt).setOnClickListener(MainActivity.this);

        weatherGet();
    }

    //非同期処理　天気確認ボタンonclick
    public void weatherUpdate(View view){
        weatherGet();
    }

    public void weatherGet() {
        Looper looper = Looper.getMainLooper();
        Handler handler = HandlerCompat.createAsync(looper);

        // OpenWeatherリクエスト用urlの設定 ID
        String[] ids = {"2127515","2130629","2130741", "2128815", "2129376", "2127586", "2130188", "2128295" } ;
        String[] urlFulls = new String[8] ;

        for (int i = 0; ids.length > i; i++) {
            urlFulls[i] =  WEATHERINFO_URL + "&id=" + ids[i] + "&appid=" + APP_ID;
        };

        // "String[] ids" に登録した都市の回数分、"BackgroundTask"を実行
        ExecutorService executor  = Executors.newCachedThreadPool();  // (2)新規スレッド作成
        for(int i=0; urlFulls.length > i; i++){
            BackgroundTask backgroundTask = new BackgroundTask(handler, urlFulls[i]);  // (3)
            executor.submit(backgroundTask);
        }
        executor.shutdown();
    }


    final class BackgroundTask implements Runnable {

        private final Handler handler;
        private final String urlFull;

        public BackgroundTask(Handler hand, String url) {
            handler = hand;
            urlFull = url;
        }


        // HTTP接続を行うHttpURLConnectionオブジェクトを宣言。finallyで解放するためにtry外で宣言。
        // HTTP接続のレスポンスデータとして取得するInputStreamオブジェクトを宣言。同じくtry外で宣言。
        // 天気情報サービスから取得したJSON文字列。天気情報が格納されている。
        // URLオブジェクトからHttpURLConnectionオブジェクトを取得。
        // 接続に使ってもよい時間を設定。 データ取得に使ってもよい時間。 HTTP接続メソッドをGETに設定。 接続。
        // HttpURLConnectionオブジェクトからレスポンスデータを取得。
        // レスポンスデータであるInputStreamオブジェクトを文字列に変換。

        // is　処理は決り文句　難しい

        //　非同期処理　@workerThread　ここから　

        @WorkerThread
        @Override
        public void run() {

            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";
            try {
                URL url = new URL(urlFull);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(1000);
                con.setReadTimeout(1000);
                con.setRequestMethod("GET");
                con.setInstanceFollowRedirects(false);
                con.connect();
                is = con.getInputStream();
                result = is2String(is);
            }
            catch(MalformedURLException ex) {
                Log.e(DEBUG_TAG, "URL変換失敗", ex);
            }
            catch(SocketTimeoutException ex) {
                Log.w(DEBUG_TAG, "通信タイムアウト", ex);
            }
            catch(IOException ex) {
                Log.e(DEBUG_TAG, "通信失敗", ex);
            }
            finally {
                if(is != null) {
                    try {
                        is.close();
                    }
                    catch(IOException ex) {
                        Log.e(DEBUG_TAG, "InputStream解放失敗", ex);
                    }
                }
                if(con != null) {
                    con.disconnect();
                }
            }

            PostExecutor postExecutor = new PostExecutor(result);
            handler.post(postExecutor);
        }

        final String is2String(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while(0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
            return sb.toString();
        }
    }

//　戻ってきたJSONを処理する
//  Web APIから取得したお天気情報JSON文字列。

    final class PostExecutor implements Runnable {

        private final String result;

        public PostExecutor(String resu) {
            result = resu;
        }

//      andro8.0 Oreo以降のビルド指定　Timezone習得java8.0の影響　
        @RequiresApi(api = Build.VERSION_CODES.O)
        @UiThread
        @Override
        public void run() {

            ConversionJSON conversionJSON = new ConversionJSON(result);
            Map<String, String> tempDatas = conversionJSON.getMap();
            IconSet icS = new IconSet();
            int[] iconIDs = icS.iconSet(tempDatas);

            if((tempDatas.get("cityName")).equals("稚内市")) {
                Button wakka = findViewById(R.id.wakkaBt);
                TextView wakka1 = findViewById(R.id.wakkaTx1);
                TextView wakka2 = findViewById(R.id.wakkaTx2);
                ImageView wakkaIv1 = findViewById(R.id.wakkaImage1);
                ImageView wakkaIv2 = findViewById(R.id.wakkaImage2);
                tempDatas.replace("cityName", "稚内");
                wakka.setText(tempDatas.get("cityName"));
                wakka1.setText(tempDatas.get("description0"));
                wakka2.setText(tempDatas.get("description2"));
                wakkaIv1.setImageResource(iconIDs[0]);
                wakkaIv2.setImageResource(iconIDs[2]);
                wakkaDatas.putAll(tempDatas);
                wakIcons = iconIDs;

            } else if ((tempDatas.get("cityName")).equals("旭川")){
                ((Button) findViewById(R.id.asahiBt)).setText(tempDatas.get(("cityName")));
                ((TextView) findViewById(R.id.asahiTx1)).setText(tempDatas.get("description0"));
                ((TextView) findViewById(R.id.asahiTx2)).setText(tempDatas.get("description2"));
                ((ImageView) findViewById(R.id.asaImage1)).setImageResource(iconIDs[0]);
                ((ImageView) findViewById(R.id.asaImage2)).setImageResource(iconIDs[2]);
                asahiDatas.putAll(tempDatas);
                asaIcons = iconIDs;

            } else if((tempDatas.get("cityName")).equals("網走")) {
                ((Button) findViewById(R.id.abaBt)).setText(tempDatas.get(("cityName")));
                ((TextView) findViewById(R.id.abaTx1)).setText(tempDatas.get("description0"));
                ((TextView) findViewById(R.id.abaTx2)).setText(tempDatas.get("description2"));
                ((ImageView) findViewById(R.id.abaImage1)).setImageResource(iconIDs[0]);
                ((ImageView) findViewById(R.id.abaImage2)).setImageResource(iconIDs[2]);
                abashiDatas.putAll(tempDatas);
                abaIcons = iconIDs;

            } else if((tempDatas.get("cityName")).equals("帯広")) {
                ((Button) findViewById(R.id.obiBt)).setText(tempDatas.get(("cityName")));
                ((TextView) findViewById(R.id.obiTx1)).setText(tempDatas.get("description0"));
                ((TextView) findViewById(R.id.obiTx2)).setText(tempDatas.get("description2"));
                ((ImageView) findViewById(R.id.obiImage1)).setImageResource(iconIDs[0]);
                ((ImageView) findViewById(R.id.obiImage2)).setImageResource(iconIDs[2]);
                obihiDatas.putAll(tempDatas);
                obiIcons = iconIDs;

            } else if((tempDatas.get("cityName")).equals("釧路市")) {
                tempDatas.replace("cityName", "釧路");
                ((Button) findViewById(R.id.kusBt)).setText(tempDatas.get(("cityName")));
                ((TextView) findViewById(R.id.kusTx1)).setText(tempDatas.get("description0"));
                ((TextView) findViewById(R.id.kusTx2)).setText(tempDatas.get("description2"));
                ((ImageView) findViewById(R.id.kusImage1)).setImageResource(iconIDs[0]);
                ((ImageView) findViewById(R.id.kusImage2)).setImageResource(iconIDs[2]);
                kushiDatas.putAll(tempDatas);
                kusIcons = iconIDs;

            } else if((tempDatas.get("cityName")).equals("Urakawa")) {
                tempDatas.replace("cityName", "浦河");
                ((Button) findViewById(R.id.uraBt)).setText(tempDatas.get(("cityName")));
                ((TextView) findViewById(R.id.uraTx1)).setText(tempDatas.get("description0"));
                ((TextView) findViewById(R.id.uraTx2)).setText(tempDatas.get("description2"));
                ((ImageView) findViewById(R.id.uraImage1)).setImageResource(iconIDs[0]);
                ((ImageView) findViewById(R.id.uraImage2)).setImageResource(iconIDs[2]);
                urakaDatas.putAll(tempDatas);
                uraIcons = iconIDs;

            } else if((tempDatas.get("cityName")).equals("函館市")) {
                tempDatas.replace("cityName", "函館");
                ((Button) findViewById(R.id.hakoBt)).setText(tempDatas.get(("cityName")));
                ((TextView) findViewById(R.id.hakoTx1)).setText(tempDatas.get("description0"));
                ((TextView) findViewById(R.id.hakoTx2)).setText(tempDatas.get("description2"));
                ((ImageView) findViewById(R.id.hakoImage1)).setImageResource(iconIDs[0]);
                ((ImageView) findViewById(R.id.hakoImage2)).setImageResource(iconIDs[2]);
                hakodaDatas.putAll(tempDatas);
                hakIcons = iconIDs;

            } else {
                //　Cityname札幌・その他の場合
                tempDatas.replace("cityName", "札幌");
                ((Button) findViewById(R.id.sapBt)).setText(tempDatas.get(("cityName")));
                ((TextView) findViewById(R.id.sapTx1)).setText(tempDatas.get("description0"));
                ((TextView) findViewById(R.id.sapTx2)).setText(tempDatas.get("description2"));
                ((ImageView) findViewById(R.id.sapImage1)).setImageResource(iconIDs[0]);
                ((ImageView) findViewById(R.id.sapImage2)).setImageResource(iconIDs[2]);
                sappuDatas.putAll(tempDatas);
                sapIcons = iconIDs;

                ((TextView) findViewById(R.id.foreCast)).setText(tempDatas.get("nearTime"));
                ((TextView) findViewById(R.id.foreCast6)).setText(tempDatas.get("hoursLater6"));
                ((TextView) findViewById(R.id.time1Tx)).setText(tempDatas.get("hoursLater3"));
                ((TextView) findViewById(R.id.time2Tx)).setText(tempDatas.get("hoursLater9"));
                ((TextView) findViewById(R.id.time3Tx)).setText(tempDatas.get("hoursLater12"));
                ((TextView) findViewById(R.id.time4Tx)).setText(tempDatas.get("hoursLater15"));
            }
        }
    }

//  各都市のボタンクリック時の処理
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {
        Map<String, String> whichDatas = new HashMap<>();
        int[] whichIcons = new int[6];

        int whichBtId = view.getId();
        switch (whichBtId){
            case R.id.wakkaBt:
                whichDatas.putAll(wakkaDatas);
                whichIcons = wakIcons;
                break;
            case R.id.asahiBt:
                whichDatas = asahiDatas;
                whichIcons = asaIcons;
                break;
            case R.id.sapBt:
                whichDatas = sappuDatas;
                whichIcons = sapIcons;
                break;
            case R.id.abaBt:
                whichDatas = abashiDatas;
                whichIcons = abaIcons;
                break;
            case R.id.obiBt:
                whichDatas = obihiDatas;
                whichIcons = obiIcons;
                break;
            case R.id.kusBt:
                whichDatas = kushiDatas;
                whichIcons = kusIcons;
                break;
            case R.id.uraBt:
                whichDatas = urakaDatas;
                whichIcons = uraIcons;
                break;
            case R.id.hakoBt:
                whichDatas = hakodaDatas;
                whichIcons = hakIcons;
                break;
            default:
                break;
        }

        pressButtonProcess prs = new pressButtonProcess();
        prs.setSerchCity(whichDatas);

        if(whichIcons == null){
            whichIcons = new int[6];
            Arrays.fill(whichIcons, R.drawable.weather_frog);
        }

        // 下段画面に、クリックされた都市の天気を出力
        ((TextView) findViewById(R.id.locationTx)).setText(whichDatas.get("cityName"));
        ((TextView) findViewById(R.id.sunriseTx)).setText(whichDatas.get("sunrise"));
        ((TextView) findViewById(R.id.sunsetTx)).setText(whichDatas.get("sunset"));

        ((TextView) findViewById(R.id.focsTx1)).setText(whichDatas.get("description1"));
        ((TextView) findViewById(R.id.focsTx2)).setText(whichDatas.get("description3"));
        ((TextView) findViewById(R.id.focsTx3)).setText(whichDatas.get("description4"));
        ((TextView) findViewById(R.id.focsTx4)).setText(whichDatas.get("description5"));

        ((ImageView) findViewById(R.id.focsImage1)).setImageResource(whichIcons[1]);
        ((ImageView) findViewById(R.id.focsImage2)).setImageResource(whichIcons[3]);
        ((ImageView) findViewById(R.id.focsImage3)).setImageResource(whichIcons[4]);
        ((ImageView) findViewById(R.id.focsImage4)).setImageResource(whichIcons[5]);

        ((TextView) findViewById(R.id.temperTx1)).setText(whichDatas.get("temper1"));
        ((TextView) findViewById(R.id.temperTx2)).setText(whichDatas.get("temper3"));
        ((TextView) findViewById(R.id.temperTx3)).setText(whichDatas.get("temper4"));
        ((TextView) findViewById(R.id.temperTx4)).setText(whichDatas.get("temper5"));

    }

    // 検索ワード設定ボタン処理
    public void serchButton(View view) {

        pressButtonProcess prs = new pressButtonProcess();
        String serchSpot = prs.serchSpotSwitch(view);
        TextView serchTx = findViewById(R.id.serchTx);
        serchTx.setText(serchSpot);
    }

    // GoogleMap立ち上げ処理
    public void mapSend(View view) {

        String mapSearchName = "北海道";
        pressButtonProcess prs = new pressButtonProcess();

        mapSearchName = prs.getSerchCity();
        String spot = prs.getSerchSpot();

        try {
            // 入力されたキーワードをURLエンコード。 マップアプリと連携するURI文字列を生成。
            // URI文字列からURIオブジェクトを生成。　Intentオブジェクトを生成。 アクティビティを起動。
            mapSearchName = URLEncoder.encode(mapSearchName, "UTF-8");
            String uriStr = "geo:0,0?q=" + mapSearchName + spot;
            Uri uri = Uri.parse(uriStr);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        catch(UnsupportedEncodingException ex) {
            Log.e("MainActivity", "検索キーワード変換失敗", ex);
        }
    }
}