package s.tokyo.jp.batterymeter;

import android.app.Activity;
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

public class MongoLogPost extends AsyncTask<Uri.Builder, Void, String> {
    private final static String LOGDIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    private final static String SDFILE = LOGDIR + "/" + "btlog.csv";
    private static boolean enable = true;

    private Activity myActivity;

    private StringBuilder postData;

    public MongoLogPost(Activity activity) {
        this.myActivity = activity;
        postData = new StringBuilder();
    }


    @Override
    protected String doInBackground(Uri.Builder... builder) {

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
    }
}
