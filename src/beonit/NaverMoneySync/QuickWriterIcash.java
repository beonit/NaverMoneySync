package beonit.NaverMoneySync;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class QuickWriterIcash extends QuickWriter {
	public QuickWriterIcash(String id, String passwd, Context context){
		super(id, passwd, context);
		mWebView.setWebViewClient(new ICashViewClient());
	}
	
	public boolean quickWrite(ArrayList<String> items){
		super.quickWrite(items, "http://m.icashhouse.co.kr");
        mWebView.addJavascriptInterface(new JSInterfaceICash(), "HTMLOUT");
        return true;
	}
	
	class ICashViewClient extends WebViewClient{
    	@Override
    	public void onPageFinished(WebView view, String url){
    		Log.i("beonit", url);
    		if( url.equals("http://m.icashhouse.co.kr")){
    			switch( writeState ){
    			case WRITE_READY:
        			Log.v("beonit", "page load, login attempt");
    	    		view.loadUrl("javascript:Username.value='"+ id +"'");
    	    		view.loadUrl("javascript:Password.value='"+ passwd +"'");
    	    		view.loadUrl("javascript:window.HTMLOUT.loginResult( check_login(document.getElementById('frm_login'))");
    	    		writeState = WRITE_LOGIN_ATTEMPT;
    				break;
    			case WRITE_LOGIN_ATTEMPT:
    				view.loadUrl("javascript:window.HTMLOUT.checkLogin( document.getElementById('private').innerHTML );");
    				break;
    			case WRITE_LOGIN_SUCCESS:
    				view.loadUrl("http://m.icashhouse.co.kr/tra_insert.php");
    				break;
    			}
    		}
    		else if( url.equals("http://m.icashhouse.co.kr/tra_insert.php") ){
    			view.loadUrl("javascript:date_r_.value=" + "2011-08-18" );
    			view.loadUrl("javascript:item.value=" + items.get(0) );
    			view.loadUrl("javascript:money.value=" + 100 );
    			view.loadUrl("javascript:window.HTMLOUT.checkInsert('', document.getElementsByName('insert')[0])");
    			writeState = WRITE_WRITING;
    		}
    		else{
    			Log.e("boenit", "fail : " + url);
    			view.destroy();
    			sendFail("원인을 모름");
    			writeState = WRITE_FAIL;
    		}
    	}
    }
	
	final class JSInterfaceICash {
		public void loginResult(String result){
			Log.i("beonit", "loginResult " + result );
			if( result.equals("false") ){
				
			}
		}
		
		public void checkInsert(String html){
			Log.i("beonit", "checkInsert : " + html);
		}
		
		public void checkLogin(String html){
			Log.i("beonit", "checkLogin : " + html);
			if( html.contains(id) )
				writeState = WRITE_LOGIN_SUCCESS;
			else
				writeState = WRITE_LOGIN_FAIL;
			return;
		}
		
	    public void showHTML(String html) {
	    	Log.i("beonit", "show html : " + html);
	        if( html.contains("오류") ){
	        	writeState = WRITE_LOGIN_FAIL;
	        	sendFail("로그인 실패");
	        }
	    }  
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	// 이곳에서 성고 실패에 관한 notify, 저장관리를 한다.
	// notify 의 경우 각 가계부 사이트마다 실패 사유가 다양할 수 있기 때문에 각 사이트 특성을 파생시킨 클래스에서 해 주어야 한다.
    ///////////////////////////////////////////////////////////////////////////////////
	
	private boolean isResultNoti = true;
	public void setResultNoti( boolean noti ){
		this.isResultNoti = noti;
	}

	public void sendFail(String cause) {
		super.sendFail(); 
		// 결과를 notify 한다.
		if( isResultNoti ){
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
		if( isResultNoti ){
			Context context = mWebView.getContext();
	    	Notification notification = new Notification(R.drawable.icon, "네이버 가계부에 입력 완료", 0);
	    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    	Intent successIntent = new Intent();
	    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, successIntent, 0);
			String writeString = "";
			for( String item : items ){
				Log.v("beonit", "write item : " + item);
				writeString = writeString + item + ";";
			}
			Log.v("beonit", "write item : " + writeString);
			if( writeString.length() == 0 )
				return;
	    	notification.setLatestEventInfo(context, "기록 완료", writeString, pendingIntent);
			NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	    	nm.notify(ViewMain.NOTI_ID, notification);
		}
	}
}
