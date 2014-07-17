package s.tokyo.jp.batterymeter;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class SdLog {
    private final static String LOGDIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    private final static String SDFILE = LOGDIR + "/" + "btlog.csv";
    private static boolean enable = true;

    static public void put(String text) {

        Log.e("SdLog", LOGDIR);

        if (!enable) return;
        BufferedWriter bw = null;

        try {

            FileOutputStream file = new FileOutputStream(SDFILE, true);
            bw = new BufferedWriter(new OutputStreamWriter(file, "UTF-8"));
            bw.append(text + "\n");
            bw.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
