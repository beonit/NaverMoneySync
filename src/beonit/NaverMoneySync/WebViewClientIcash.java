package beonit.NaverMoneySync;

import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewClientIcash extends WebViewClient {
	int writeState = QuickWriter.WRITE_READY;
	@Override
	public void onPageFinished(WebView view, String url) {
		view.setWillNotDraw(false);
		Log.i("beonit", "onPageFinished : " + url);
		if (url.equals("http://m.icashhouse.co.kr/")) {
			switch (writeState) {
			case QuickWriter.WRITE_READY:
				Log.v("beonit", "onPageFinished, WRITE_READY");
				view.loadUrl("javascript:window.HTMLOUT.checkLoginResult( document.getElementById('others').innerHTML );");
				view.loadUrl("javascript:Username.value='" + "beonit" + "'");
				view.loadUrl("javascript:Password.value='" + "akdma59" + "'");
				view.loadUrl("javascript:login.submit( check_login( document.getElementById('frm_login') ) )");
				writeState = QuickWriter.WRITE_LOGIN_ATTEMPT;
				break;
			case QuickWriter.WRITE_LOGIN_ATTEMPT:
				Log.v("beonit", "onPageFinished, WRITE_LOGIN_ATTEMPT");
				view.loadUrl("javascript:window.HTMLOUT.checkLoginResult( document.getElementById('others').innerHTML );");
				break;
			}
		} else if (url.equals("http://m.icashhouse.co.kr/tra_insert.php")) {
//			view.loadUrl("javascript:date_r_.value=" + "2011-08-18");
//			view.loadUrl("javascript:item.value=" + items.get(0));
//			view.loadUrl("javascript:money.value=" + 100);
//			view.loadUrl("javascript:window.HTMLOUT.checkInsert('', document.getElementsByName('insert')[0])");
//			writeState = QuickWriter.WRITE_WRITING;
		} else {
			Log.e("boenit", "fail : " + url);
			view.destroy();
//			sendFail("원인을 모름");
			writeState = QuickWriter.WRITE_FAIL;
		}
	}
	
	final class JSInterfaceICash {
		public void checkInsert(final String html){
			Log.i("beonit", "checkInsert : " + html);
		}
		
		public void checkLoginResult(final String html){
			Log.i("beonit", "checkLogin : " + html);
			if( html.contains("기타설정") ){
				writeState = QuickWriter.WRITE_LOGIN_SUCCESS;
//				mWebView.loadUrl("http://m.icashhouse.co.kr/tra_insert.php");
			}
			else{
				writeState = QuickWriter.WRITE_LOGIN_FAIL;
			}
			return;
		}
		
	    public void showHTML(final String html) {
	    	Log.i("beonit", "show html : " + html);
	        if( html.contains("오류") ){
	        	writeState = QuickWriter.WRITE_LOGIN_FAIL;
//	        	sendFail("로그인 실패");
	        }
	    }  
	}

}