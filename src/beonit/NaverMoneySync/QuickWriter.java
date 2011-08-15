package beonit.NaverMoneySync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class QuickWriter {
	public static final int WRITE_READY = 0;
	public static final int WRITE_LOGIN = 1;
	public static final int WRITE_LOGIN_FAIL = 2;
	public static final int WRITE_LOGIN_SUCCESS = 3;
	public static final int WRITE_WRITING = 4;
	public static final int WRITE_SUCCESS = 5;
	public static final int WRITE_FAIL = 6;
	public static final int WRITE_FAIL_REGISTER = 7;
	
	private String id;
	private String passwd;
	private String items;
	private WebView mWebView;
	private int writeState = WRITE_READY;
	private boolean resultNoti = true;
	private boolean failSave = true;
	
	public void setResultNoti( boolean noti ){
		this.resultNoti = noti;
	}
	
	public QuickWriter(String id, String passwd, Context context){
		this.id = id;
		this.passwd = passwd;
		mWebView = new WebView(context);
        mWebView.setWillNotDraw(true);
        mWebView.setWebViewClient(new NaverViewClient());
	}
	
	public boolean quickWrite(String items){
		this.items = items.replace("\n", " ");
		mWebView.loadUrl("https://nid.naver.com/nidlogin.login?svctype=262144&url=http://beta.moneybook.naver.com/m/write.nhn?method=quick");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
		return true;
	}
	
	public int getSendState() {
		return writeState;
	}

	class NaverViewClient extends WebViewClient{
    	@Override
    	public void onPageFinished(WebView view, String url){
    		Log.i("beonit", url);
    		if( url.equals("https://nid.naver.com/nidlogin.login?svctype=262144&url=http://beta.moneybook.naver.com/m/write.nhn?method=quick")){
    			Log.v("beonit", "page load, login attempt");
    			writeState = WRITE_READY;
    			Log.v("beonit", "login... attempt with id/passwd");
	    		view.loadUrl("javascript:id.value='"+ id +"'");
	    		view.loadUrl("javascript:pw.value='"+ passwd +"'");
	    		view.loadUrl("javascript:loginform.submit()");
    		}
    		else if( url.equals("https://nid.naver.com/nidlogin.login?svctype=262144") ){
    			writeState = WRITE_LOGIN;
    			view.loadUrl("javascript:window.HTMLOUT.showHTML('' + document.body.getElementsByTagName('span')[3].innerHTML);");
    		}else if( url.contains("http://static.nid.naver.com/login/sso/finalize.nhn") ){
    			Log.v("beonit", "login... success");
    			writeState = WRITE_LOGIN_SUCCESS;
    		}else if( url.equals("http://beta.moneybook.naver.com/m/write.nhn?method=quick") ){
    			Log.v("beonit", "write items : " + items);
    			view.loadUrl("javascript:items.value='"+ items +"'");
	    		view.loadUrl("javascript:writeForm.submit()");
	    		writeState = WRITE_WRITING;
    		}else if( url.equals("http://beta.moneybook.naver.com/m/smry.nhn")){
    			Log.v("beonit", "write finish");
    			sendSuccess();
    			view.destroy();
    			writeState = WRITE_SUCCESS;
    		}if( url.equals("http://beta.moneybook.naver.com/m/mbookUser.nhn")){
    			view.destroy();
    			sendFail("가계부 약관동의 안됨");
    			writeState = WRITE_FAIL_REGISTER;
    		}else{
    			Log.e("boenit", "fail : " + url);
    			view.destroy();
    			sendFail("원인을 모름");
    			writeState = WRITE_FAIL;
    		}
    	}
    }

	public void sendFail(String cause) {
		if( items == null && mWebView != null ){
			Log.e("beonit", "items must have some sms text");
			return;
		}
		// 메세지 실패함에 다시 넣는다.
		if( failSave ){
			Context context = mWebView.getContext();
			SharedPreferences prefs = context.getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
	        String newItems = this.items + prefs.getString("items", ""); 
	        SharedPreferences.Editor editor = prefs.edit();
	        editor.putString("items", newItems);
			editor.commit();
		}
		// 결과를 notify 한다.
		if( resultNoti ){
			// result notify  
			Context context = mWebView.getContext();
			Notification notification = new Notification(R.drawable.icon, "가계부 입력 실패", 0);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent failIntent = new Intent(context, ViewMain.class);
			failIntent.putExtra("goto", 2);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, failIntent, 0);
			notification.setLatestEventInfo(context, "네이버에 쓰기 실패", cause, pendingIntent);
			NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(ViewMain.NOTI_ID, notification);
		}
	}

	private void sendSuccess() {
		// 결과를 notify 한다.
		if( resultNoti ){
			Context context = mWebView.getContext();
	    	Notification notification = new Notification(R.drawable.icon, "네이버 가계부에 입력 완료", 0);
	    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    	Intent successIntent = new Intent();
	    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, successIntent, 0);
	    	notification.setLatestEventInfo(context, "기록 완료", items, pendingIntent);
			NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	    	nm.notify(ViewMain.NOTI_ID, notification);
		}
	}
	
	final class MyJavaScriptInterface {
	    public void showHTML(String html) {
	        if( html.contains("오류") ){
	        	writeState = WRITE_LOGIN_FAIL;
	        	sendFail("로그인 실패");
	        }
	    }  
	}

	public void setFailSave(boolean failSave) {
		this.failSave = failSave;
	} 
}
