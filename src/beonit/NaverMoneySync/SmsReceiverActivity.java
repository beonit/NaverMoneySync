package beonit.NaverMoneySync;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

public class SmsReceiverActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		SharedPreferences prefs = this.getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
		String items = prefs.getString("items", "");
		String id = prefs.getString("naverID", null);
		String passwd = null;
		try {
			passwd = SimpleCrypto.decrypt("SECGAL", prefs.getString("naverPasswd", null));
		} catch (Exception e) {
			Log.e("beonit", "simple crypto decrypt fail");
			e.printStackTrace();
		}
		// Àü¼Û
		Log.i("beonit", "recv to remote service");
	    QuickWriterNaver writer = new QuickWriterNaver(id, passwd, this);
		writer.setFailSave(true);
		writer.setResultNoti(true);
		Log.i("beonit", "ProgressThread" + items);
	    ProgressThread progressThread = new ProgressThread(mHandler, writer, items);
		progressThread.start();
	}
	
	// send
    private Handler mHandler = new SyncHandler(); 
    public class SyncHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case QuickWriterNaver.WRITE_READY:
				Log.i("beonit", "WRITE_READY");
				break;
			case QuickWriterNaver.WRITE_LOGIN_ATTEMPT:
				Log.i("beonit", "WRITE_LOGIN_ATTEMPT");
				break;
			case QuickWriterNaver.WRITE_LOGIN_SUCCESS:
				Log.i("beonit", "WRITE_LOGIN_SUCCESS");
				break;
			case QuickWriterNaver.WRITE_WRITING:
				Log.i("beonit", "WRITE_WRITING");
				break;
			case QuickWriterNaver.WRITE_SUCCESS:
				Log.i("beonit", "WRITE_SUCCESS");
				break;
			case QuickWriterNaver.WRITE_LOGIN_FAIL:
				Log.i("beonit", "WRITE_LOGIN_FAIL");
				break;
			case QuickWriterNaver.WRITE_FAIL:
				Log.i("beonit", "WRITE_FAIL");
				break;
			case QuickWriterNaver.WRITE_FAIL_REGISTER:
				Log.i("beonit", "WRITE_FAIL_REGISTER");
				break;
			default:
				break;
			}
		}
    }
    
}
