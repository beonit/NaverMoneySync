package beonit.NaverMoneySync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.WebView;

public abstract class QuickWriter {

	public QuickWriter(String id, String passwd, Context context) {
		this.id = id;
		this.passwd = passwd;
		mWebView = new WebView(context);
        mWebView.setWillNotDraw(true);
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

	public boolean quickWrite(String items) {
		Log.e("beonit", "this is interface method for override");
		return false;
	}
	
	protected void quickWrite(String items, String url) {
		this.items = items;
		mWebView.setWillNotDraw(true);
		mWebView.loadUrl(url);
        mWebView.getSettings().setJavaScriptEnabled(true);
	}

	public int getSendState() {
		return writeState;
	}

	private boolean isFailSave = true;
	public void setFailSave(boolean failSave) {
		this.isFailSave = failSave;
	} 

	public void sendFail() {
		if( isFailSave ){
			Context context = mWebView.getContext();
			SharedPreferences prefs = context.getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
	        SharedPreferences.Editor editor = prefs.edit();
	        editor.putString("items", items + prefs.getString("items", "") );
			editor.commit();
		}
	}

	
}