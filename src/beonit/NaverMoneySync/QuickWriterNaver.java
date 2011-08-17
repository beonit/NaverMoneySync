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

public class QuickWriterNaver extends QuickWriter {
	
	public QuickWriterNaver(String id, String passwd, Context context){
		super(id, passwd, context);
		mWebView.setWebViewClient(new NaverViewClient());
	}
	
	public boolean quickWrite(ArrayList<String> items){
		super.quickWrite(items, "https://nid.naver.com/nidlogin.login?svctype=262144&url=http://beta.moneybook.naver.com/m/write.nhn?method=quick");
        mWebView.addJavascriptInterface(new JSInterfaceNaver(), "HTMLOUT");
        return true;
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
    			writeState = WRITE_LOGIN_ATTEMPT;
    			view.loadUrl("javascript:window.HTMLOUT.showHTML('' + document.body.getElementsByTagName('span')[3].innerHTML);");
    		}
    		else if( url.contains("http://static.nid.naver.com/login/sso/finalize.nhn") ){
    			Log.v("beonit", "login... success");
    			writeState = WRITE_LOGIN_SUCCESS;
    		}
    		else if( url.equals("http://beta.moneybook.naver.com/m/write.nhn?method=quick") ){
    			String writeString = "";
    			for( String item : items ){
    				Log.v("beonit", "write item : " + item);
    				writeString = writeString + item + ";";
    			}
    			Log.v("beonit", "write item : " + writeString);
    			if( writeString.length() == 0 )
    				return;
    			view.loadUrl("javascript:window.HTMLOUT.showHTML(items.value)");
    			view.loadUrl("javascript:items.value='"+ writeString +"'");
    			view.loadUrl("javascript:window.HTMLOUT.showHTML(items.value)");
	    		view.loadUrl("javascript:writeForm.submit()");
	    		writeState = WRITE_WRITING;
    		}
    		else if( url.equals("http://beta.moneybook.naver.com/m/smry.nhn")){
    			Log.v("beonit", "write finish");
    			sendSuccess();
    			view.destroy();
    			writeState = WRITE_SUCCESS;
    		}
    		else if( url.equals("http://beta.moneybook.naver.com/m/mbookUser.nhn")){
    			view.destroy();
    			sendFail("����� ������� �ȵ�");
    			writeState = WRITE_FAIL_REGISTER;
    		}
    		else{
    			Log.e("boenit", "fail : " + url);
    			view.destroy();
    			sendFail("������ ��");
    			writeState = WRITE_FAIL;
    		}
    	}
    }
	
	final class JSInterfaceNaver {
	    public void showHTML(String html) {
	    	Log.v("beonit", "show html : " + html);
	        if( html.contains("����") ){
	        	writeState = WRITE_LOGIN_FAIL;
	        	sendFail("�α��� ����");
	        }
	    }  
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	// �̰����� ���� ���п� ���� notify, ��������� �Ѵ�.
	// notify �� ��� �� ����� ����Ʈ���� ���� ������ �پ��� �� �ֱ� ������ �� ����Ʈ Ư���� �Ļ���Ų Ŭ�������� �� �־�� �Ѵ�.
    ///////////////////////////////////////////////////////////////////////////////////
	
	private boolean isResultNoti = true;
	public void setResultNoti( boolean noti ){
		this.isResultNoti = noti;
	}

	public void sendFail(String cause) {
		super.sendFail(); 
		// ����� notify �Ѵ�.
		if( isResultNoti ){
			// result notify  
			Context context = mWebView.getContext();
			Notification notification = new Notification(R.drawable.icon, "����� �Է� ����", 0);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent failIntent = new Intent(context, ViewMain.class);
			failIntent.putExtra("goto", 2);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, failIntent, 0);
			notification.setLatestEventInfo(context, "���̹��� ���� ����", cause, pendingIntent);
			NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(ViewMain.NOTI_ID, notification);
		}
	}

	private void sendSuccess() {
		// ����� notify �Ѵ�.
		if( isResultNoti ){
			Context context = mWebView.getContext();
	    	Notification notification = new Notification(R.drawable.icon, "���̹� ����ο� �Է� �Ϸ�", 0);
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
	    	notification.setLatestEventInfo(context, "��� �Ϸ�", writeString, pendingIntent);
			NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	    	nm.notify(ViewMain.NOTI_ID, notification);
		}
	}
}