package s.tokyo.jp.batterymeter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MyActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Button btn = (Button) findViewById(R.id.StartButton);
        btn.setOnClickListener(btnListener);//リスナの登録

        btn  = (Button) findViewById(R.id.StopButton);
        btn.setOnClickListener(btnListener);//リスナの登録

        btn = (Button) findViewById(R.id.BindButton);
        btn.setOnClickListener(btnListener);//リスナの登録

        btn = (Button) findViewById(R.id.UnbindButton);
        btn.setOnClickListener(btnListener);//リスナの登録
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        public void onClick(View v) {

            switch(v.getId()){

                case R.id.StartButton://startServiceでサービスを起動
                    startService(new Intent(MyActivity.this, MyService.class));
                    break;
                case R.id.StopButton://stopServiceでサービスの終了
                    stopService(new Intent(MyActivity.this, MyService.class));
                    break;

                case R.id.BindButton://doBindService
                    doBindService();
                    break;

                case R.id.UnbindButton://doUnbindService
                    doUnbindService();
                    break;

                default:
                    break;
            }
        }
    };



    //取得したServiceの保存
    private MyService mBoundService;
    private boolean mIsBound;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            // サービスとの接続確立時に呼び出される
            Toast.makeText(MyActivity.this, "Activity:onServiceConnected",
                    Toast.LENGTH_SHORT).show();

            // サービスにはIBinder経由で#getService()してダイレクトにアクセス可能
            mBoundService = ((MyService.MyServiceLocalBinder)service).getService();

            //必要であればmBoundServiceを使ってバインドしたサービスへの制御を行う
        }

        public void onServiceDisconnected(ComponentName className) {
            // サービスとの切断(異常系処理)
            // プロセスのクラッシュなど意図しないサービスの切断が発生した場合に呼ばれる。
            mBoundService = null;
            Toast.makeText(MyActivity.this, "Activity:onServiceDisconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {

        Toast.makeText(MyActivity.this, "Activity:doBindService",
                Toast.LENGTH_SHORT).show();

        //サービスとの接続を確立する。明示的にServiceを指定
        //(特定のサービスを指定する必要がある。他のアプリケーションから知ることができない = ローカルサービス)
        bindService(new Intent(MyActivity.this,
                MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {

            // サービスとの接続確立時に呼び出される
            Toast.makeText(MyActivity.this, "Activity:doUnbindService",
                    Toast.LENGTH_SHORT).show();

            // コネクションの解除
            unbindService(mConnection);
            mIsBound = false;
        }
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
