package beonit.NaverMoneySync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.webkit.WebView;

public abstract class QuickWriter implements IQuickWriter {

	public QuickWriter(String id, String passwd) {
		this.id = id;
		this.passwd = passwd;
	}
	
	protected String id;
	protected String passwd;
	protected String items;
	protected WebView mWebView;
	protected int writeState = WRITE_READY;
	
	public static final int WRITE_READY = 0;
	public static final int WRITE_LOGIN_ATTEMPT = 1;
	public static final int WRITE_LOGIN_FAIL = 2;
	public static final int WRITE_LOGIN_SUCCESS = 3;
	public static final int WRITE_WRITING = 4;
	public static final int WRITE_SUCCESS = 5;
	public static final int WRITE_FAIL = 6;
	public static final int WRITE_FAIL_REGISTER = 7;
	public static final int TIME_OUT = 8;

	final public int getSendState() {
		return writeState;
	}
	
	private boolean isFailSave = true;
	final public void setFailSave(boolean failSave) {
		this.isFailSave = failSave;
	}
	
	final public void sendFail(String cause) {
		if( isFailSave ){
			Context context = mWebView.getContext();
			SharedPreferences prefs = context.getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
	        SharedPreferences.Editor editor = prefs.edit();
	        editor.putString("items", items + prefs.getString("items", "") );
			editor.commit();
		}
		if( isResultNoti ){
			// result notify  
			Context context = mWebView.getContext();
			Notification notification = new Notification(R.drawable.icon, "가계부 입력 실패", 0);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent failIntent = new Intent(context, ViewMain.class);
			failIntent.putExtra("goto", 2);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, failIntent, 0);
			notification.setLatestEventInfo(context, "쓰기 실패", cause, pendingIntent);
			NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(ViewMain.NOTI_ID, notification);
		}
	}

	protected boolean isResultNoti = true;
	final public void setResultNoti(boolean noti) {
		this.isResultNoti = noti;
	}	
	
	final protected void sendSuccess() {
		SharedPreferences prefs = mWebView.getContext().getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
    	ed.putString("items", "");
    	ed.commit();
		// 결과를 notify 한다.
		if( isResultNoti ){
			Context context = mWebView.getContext();
	    	Notification notification = new Notification(R.drawable.icon, "기록 완료", 0);
	    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    	Intent successIntent = new Intent();
	    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, successIntent, 0);
	    	notification.setLatestEventInfo(context, "기록 완료", items, pendingIntent);
			NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	    	nm.notify(ViewMain.NOTI_ID, notification);
		}
	}

	protected void sendProgressNotify( String progressString ){
		// 결과를 notify 한다.
		if( isResultNoti ){
			Context context = mWebView.getContext();
	    	Notification notification = new Notification(R.drawable.icon, progressString, 0);
	    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    	Intent successIntent = new Intent();
	    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, successIntent, 0);
	    	notification.setLatestEventInfo(context, progressString, items, pendingIntent);
			NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	    	nm.notify(ViewMain.NOTI_ID, notification);
		}
	}
}