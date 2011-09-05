package beonit.NaverMoneySync;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

public class Communicator extends Service {
	private Context context = this;
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// Return the interface
		return mBinder;
	}
	
	private final ICommunicator.Stub mBinder = new ICommunicator.Stub() {

		@Override
		public void onRecvSMS(List<String> items, String id, String passwd)
				throws RemoteException {
			// Àü¼Û
			Log.i("beonit", "recv to remote service");
		    QuickWriterNaver writer = new QuickWriterNaver(id, passwd, context);
			writer.setFailSave(true);
			writer.setResultNoti(true);
			Log.i("beonit", "ProgressThread " + items);
		    ProgressThread progressThread = new ProgressThread(mHandler, writer, items);
			progressThread.start();
		}

		@Override
		public void test() throws RemoteException {
			Log.i("beonit", "test service");
		}
		
	};

	// send
    private Handler mHandler = new SyncHandler(); 
    public class SyncHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case QuickWriterNaver.WRITE_READY:
				break;
			case QuickWriterNaver.WRITE_LOGIN_ATTEMPT:
				break;
			case QuickWriterNaver.WRITE_LOGIN_SUCCESS:
				break;
			case QuickWriterNaver.WRITE_WRITING:
				break;
			case QuickWriterNaver.WRITE_SUCCESS:
				break;
			case QuickWriterNaver.WRITE_LOGIN_FAIL:
				break;
			case QuickWriterNaver.WRITE_FAIL:
				break;
			case QuickWriterNaver.WRITE_FAIL_REGISTER:
				break;
			default:
				break;
			}
		}
	};
}