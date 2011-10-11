package beonit.NaverMoneySync;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

public class SmsReceiverActivity extends Activity {

	QuickWriter writer = null;
	ProgressThread progressThread = null;
	
	private Context getContext(){ return this; }
	
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
		// 전송
		Log.i("beonit", "recv to remote service");
	    writer = new QuickWriterNaver(id, passwd, this);
		writer.setFailSave(true);
		writer.setResultNoti(true);
		Log.i("beonit", "ProgressThread" + items);
	    progressThread = new ProgressThread(mHandler, writer, items);
		progressThread.start();
		DialogInterface.OnCancelListener listenerCancel = new DialogInterface.OnCancelListener (){
			@Override
			public void onCancel(DialogInterface dialog){
				// notify
				Log.i("beonit", "user cancel writing to naver");
		    	Notification notification = new Notification(R.drawable.icon, "사용자 입력 취소", 0);
		    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
		    	Intent cancelIntent = new Intent();
		    	PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, cancelIntent, 0);
		    	notification.setLatestEventInfo(getContext(), "사용자 입력 취소", "당신이 네이버 입력을 취소했습니다", pendingIntent);
				NotificationManager nm = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		    	nm.notify(ViewMain.NOTI_ID, notification);
				
				// close dialog
				mProgressDialog.dismiss();
				activity.finish();
			}
		};
		mProgressDialog = ProgressDialog.show(this, "가계부 쓰기", "3G는 더 기다려 주세요\n창을 없애려면 뒤로가기 버튼\n취소해도 입력은 계속 진행됩니다.", false, true , listenerCancel);
	}
	
	@Override
	protected void onDestroy (){
		super.onDestroy();
		if( writer != null )
			writer.stop();
	}
	
	// send
	Activity activity = this;
	public ProgressDialog mProgressDialog;
	private Handler mHandler = new SyncHandler(); 
	public class SyncHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case QuickWriterNaver.WRITE_READY:
				mProgressDialog.setMessage("3G는 더 기다려 주세요\n접속 중...");
				break;
			case QuickWriterNaver.WRITE_LOGIN_ATTEMPT:
				mProgressDialog.setMessage("3G는 더 기다려 주세요\n로그인 페이지 로드");
				break;
			case QuickWriterNaver.WRITE_LOGIN_SUCCESS:
				mProgressDialog.setMessage("3G는 더 기다려 주세요\n입력 페이지 로드");
				break;
			case QuickWriterNaver.WRITE_WRITING:
				mProgressDialog.setMessage("3G는 더 기다려 주세요\n가계부 내용 입력 ");
				break;
			case QuickWriterNaver.WRITE_SUCCESS:
				mProgressDialog.dismiss(); // ProgressDialog 종료
				activity.finish();
				break;
			case QuickWriterNaver.WRITE_LOGIN_FAIL:
				mProgressDialog.dismiss(); // ProgressDialog 종료
				activity.finish();
				break;
			case QuickWriterNaver.WRITE_FAIL:
				mProgressDialog.dismiss(); // ProgressDialog 종료
				activity.finish();
				break;
			case QuickWriterNaver.WRITE_FAIL_REGISTER:
				mProgressDialog.dismiss(); // ProgressDialog 종료
				activity.finish();
				break;
			case QuickWriterNaver.TIME_OUT:
				mProgressDialog.dismiss(); // ProgressDialog 종료
				activity.finish();
				break;
			default:
				break;
			}
		}
	};
    
}
