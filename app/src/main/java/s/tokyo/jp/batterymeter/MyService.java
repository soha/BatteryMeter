package s.tokyo.jp.batterymeter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.*;

public class MyService extends Service {
    public MyService() {
    }

    static final String TAG="ButteryMeterService";

    /**
     * 指定回数ログが溜まったらファイルに書き出す個数
     */
    static final int FILE_WRITE_LOG_COUNT = 10;

    static List<String> msgList = new ArrayList<String>();
    static int bLevel = 0;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        makeText(this, "MyService#onCreate", LENGTH_SHORT).show();

        msgList.add("log collect start\r\n");

        // インテントフィルタ
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        // ブロードキャストレシーバ登録
        registerReceiver(broadcastReceiver_, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);
        Toast.makeText(this, "MyService#onStartCommand", Toast.LENGTH_SHORT).show();
        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        Toast.makeText(this, "MyService#onDestroy", Toast.LENGTH_SHORT).show();
        unregisterReceiver(broadcastReceiver_);

        writeLogFile();
    }

    private void writeLogFile() {
        Toast.makeText(this, msgList.size() + "個のログ", Toast.LENGTH_SHORT).show();
        StringBuilder sb = new StringBuilder();
        for(String msg : msgList) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            sb.append(msg);
            sb.append(("\r\n"));
        }
        SdLog.put(sb.toString());
        //古いのは捨て新しいインスタンス作る
        msgList = new ArrayList<String>();
    }


    //サービスに接続するためのBinder
    public class MyServiceLocalBinder extends Binder {
        //サービスの取得
        MyService getService() {
            return MyService.this;
        }
    }
    //Binderの生成
    private final IBinder mBinder = new MyServiceLocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "MyService#onBind"+ ": " + intent, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onBind" + ": " + intent);
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent){
        Toast.makeText(this, "MyService#onRebind"+ ": " + intent, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onRebind" + ": " + intent);
    }

    @Override
    public boolean onUnbind(Intent intent){
        Toast.makeText(this, "MyService#onUnbind"+ ": " + intent, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onUnbind" + ": " + intent);

        //onUnbindをreturn trueでoverrideすると次回バインド時にonRebildが呼ばれる
        return true;
    }


    private BroadcastReceiver broadcastReceiver_ = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {

                // バッテリーの状態
                String batteryHealth = "";
                switch (intent.getIntExtra("health", 0)) {
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        batteryHealth = "COLD";
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        batteryHealth = "DEAD";
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        batteryHealth = "GOOD";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        batteryHealth = "OVERHEAT";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        batteryHealth = "OVER VOLTAGE";
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        batteryHealth = "UNNOWN";
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        batteryHealth = "UNSPECIFIED FAILURE";
                        break;
                }

                // AC or USB
                String batteryPlugged = "";
                switch (intent.getIntExtra("plugged", 0)) {
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        batteryPlugged = "AC";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        batteryPlugged = "USB";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                        batteryPlugged = "WIRELESS";
                        break;
                }

                // 充電状態
                String batteryStatus = "";
                switch (intent.getIntExtra("status", 0)) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        Log.d("BatteryChange", "Status : CHARGING");
                        batteryStatus = "CHARGING";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        Log.d("BatteryChange", "Status : DISCHARGING");
                        batteryStatus = "DISCHARGING";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        Log.d("BatteryChange", "Status : FULL");
                        batteryStatus = "FULL";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        Log.d("BatteryChange", "Status : NOT CHARGING");
                        batteryStatus = "NOT CHARGING";
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        Log.d("BatteryChange", "Status : UNKNOWN");
                        batteryStatus = "UNKNOWN";
                        break;
                }


                // バッテリー残量
                int newLevel = intent.getIntExtra("level", 0);
                String batteryLevel = String.valueOf(newLevel);

                String writeLogMsg = "";
                if (bLevel != newLevel) {
                    bLevel = newLevel;

                    //ログファイルに出力
                    //SdLog.put(batteryLevel);
                    long currentTimeMillis = System.currentTimeMillis();
                    msgList.add(currentTimeMillis + "," + batteryLevel);
                    writeLogMsg = "write log\n";


                    if(msgList.size() > FILE_WRITE_LOG_COUNT) {
                        writeLogFile();
                    }
                }


                String str = "";
                //str += "バッテリの健康状態：" + intent.getIntExtra("health", 0) + "\n";
                str += "バッテリの健康状態：" + batteryHealth + "\n";
                //str += "アイコンのリソースID：" + intent.getIntExtra("icon-small", 0) + "\n";
                //str += "バッテリの残量：" + intent.getIntExtra("level", 0) + "\n";
                str += "バッテリの残量：" + batteryLevel + "\n";
                //str += "ケーブルの接続状態：" + intent.getIntExtra("plugged", 0) + "\n";
                str += "ケーブルの接続状態：" + batteryPlugged + "\n";
                str += "バッテリの有無：" + intent.getBooleanExtra("present", false) + "\n";
                str += "バッテリ残量の最大値：" + intent.getIntExtra("scale", 0) + "\n";
                //str += "充電状態：" + intent.getIntExtra("status", 0) + "\n";
                str += "充電状態：" + batteryStatus + "\n";
                str += "バッテリの種類：" + intent.getStringExtra("technology") + "\n";
                str += "バッテリの温度；" + (float) (intent.getIntExtra("temperature", 0) / 10) + " [℃]\n";
                str += "バッテリの電圧：" + intent.getIntExtra("voltage", 0) + " [mV]\n";
                str += "\n";
                long currentTimeMillis = System.currentTimeMillis();
                str += "取得したタイムスタンプ：" + currentTimeMillis + "\n";
                str += writeLogMsg;

                Log.i("BatteryChange", currentTimeMillis + str);
                //msgList.add(currentTimeMillis + "," + str);
            }
        }
    };
}