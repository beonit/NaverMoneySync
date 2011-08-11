package beonit.NaverMoneySync;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class QuickWriter {
	private String id;
	private String passwd;
	private String items;
	private WebView mWebView;
	
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
		return true;
	}
	
	class NaverViewClient extends WebViewClient{
    	@Override
    	public void onPageFinished(WebView view, String url){
    		Log.v("beonit", "url : " + url);
    		if( url.contains("https://nid.naver.com/nidlogin.login") ){
    			Log.v("beonit", "login");
	    		view.loadUrl("javascript:id.value='"+ id +"'");
	    		view.loadUrl("javascript:pw.value='"+ passwd +"'");
	    		view.loadUrl("javascript:loginform.submit()");
    		}else if( url.contains("http://beta.moneybook.naver.com/m/write.nhn?method=quick") ){
    			Log.v("beonit", "items : " + items);
    			view.loadUrl("javascript:items.value='"+ items +"'");
	    		view.loadUrl("javascript:writeForm.submit()");
    		}else if( url.contains("http://beta.moneybook.naver.com/m/smry.nhn")){
    			Log.v("beonit", "write finish");
    		}else{
    			Log.v("beonit", "finish");
    		}
    	}
    }
}
