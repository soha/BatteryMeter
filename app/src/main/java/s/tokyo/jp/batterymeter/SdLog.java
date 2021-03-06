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

        Log.i("SdLog", LOGDIR);

        if (!enable) return;

        try {

            FileOutputStream file = new FileOutputStream(SDFILE, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(file, "UTF-8"));
            String line = text + "\r\n";
            bw.append(line);
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
