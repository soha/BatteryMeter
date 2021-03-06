package s.tokyo.jp.batterymeter;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MongoLogPost extends AsyncTask<Uri.Builder, Void, String> {
    private final static String LOGDIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    private final static String SDFILE = LOGDIR + "/" + "btlog.csv";
    private static boolean enable = true;

    private NotificationManager nm;

    private Activity myActivity;

    private StringBuilder postData;

    public MongoLogPost(Activity activity) {
        this.myActivity = activity;
        postData = new StringBuilder();
    }


    @Override
    protected String doInBackground(Uri.Builder... builder) {

        nm = (NotificationManager)myActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        String ticker="アップロード通知";
        String title = "BatteryMeter";
        String message = "ログファイルアップロード中";
        Notification notification = new Notification(android.R.drawable.ic_menu_info_details,ticker,System.currentTimeMillis());
        PendingIntent intent = PendingIntent.getActivity(myActivity, 0, new Intent(myActivity, s.tokyo.jp.batterymeter.MyActivity.class), 0);
        notification.setLatestEventInfo(myActivity.getApplicationContext(), title, message, intent);
        // 同じIDで表示中の通知を消す
        nm.cancel(0);

        // 動作フラグを書き換える(常駐)
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        // Notificationの表示
        nm.notify(0,notification);

        postData.setLength(0);

        try {
            File file = new File(SDFILE);
            BufferedReader br = new BufferedReader(new FileReader(file));

            String str = "";
            while ((str = br.readLine()) != null) {
                Log.i("MongoLog", str);

                String[] lines = str.split(",");
                if(lines.length != 5) continue; //カラム数が異なる行は無視する。
                postData.append(str + "\r\n");


//            MongoCredential credential = MongoCredential.createMongoCRCredential("taru", "futureprediction", "me".toCharArray());
//            MongoClient mongoClient = new MongoClient(new ServerAddress(server), Arrays.asList(credential));

                MongoClientOptions options = new MongoClientOptions.Builder()
                        .writeConcern(WriteConcern.ACKNOWLEDGED)
                        .readPreference(ReadPreference.primaryPreferred())
                        .connectTimeout(3000)
                        .socketTimeout(60000)
                        .connectionsPerHost(50)
                        .threadsAllowedToBlockForConnectionMultiplier(10)
                        .build();

                //mongodb://taru:me@kahana.mongohq.com:10078/futureprediction
                MongoClient mongoClient = new MongoClient(new ServerAddress("kahana.mongohq.com:10078"), options);

                DB db = mongoClient.getDB("futureprediction");
                boolean auth = db.authenticate("mongo", "me".toCharArray());

                DBCollection coll = db.getCollection("androidlog");

                BasicDBObject doc = new BasicDBObject("timestamp", lines[0])
                        .append("yyyy/MM/dd", lines[1])
                        .append("E", lines[2])
                        .append("HH:mm:ss", lines[3])
                        .append("ButteryLevel", lines[4]);

                //upload
                coll.insert(doc);
            }

            br.close();

            Calendar cal = Calendar.getInstance();
            //フォーマットパターンを指定して表示する
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            File renamedFile = new File(LOGDIR + "/" + sdf.format(cal.getTime()));
            file.renameTo(renamedFile);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            //Toast.makeText(this.myActivity, SDFILE + " file format is incrrect", Toast.LENGTH_SHORT).show();
        }

        return null;
    }



    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(this.myActivity, "ログファイルをアップロードしました。", Toast.LENGTH_SHORT).show();
        Toast.makeText(this.myActivity, postData.toString(), Toast.LENGTH_LONG).show();

        // 同じIDで表示中の通知を消す
        nm.cancel(0);
    }
}
