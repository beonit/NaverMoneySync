package beonit.NaverMoneySync;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewClientIcash extends WebViewClient {
	
	private String id;
	private String passwd;
	private ProgressDialog mProgressLoginDialog = null;
	private ProgressDialog mProgressLoadingDialog = null;
	
	public WebViewClientIcash(String id, String passwd) {
		this.id = id;
		this.passwd = passwd;
	}
	
	public void onPageStarted(WebView view, String url, Bitmap favicon){
		if( view.willNotDraw() && mProgressLoginDialog == null ){
			try{
				mProgressLoginDialog = ProgressDialog.show(view.getContext(), "가계부 로딩", "3G는 더 기다려 주세요\n로그인 페이지 로딩", false);
				mProgressLoginDialog.setCancelable(true);
			}catch(Exception e){
				Log.e("beonit", "dialog error");
				e.printStackTrace();
			}
		}else if( !view.willNotDraw() && mProgressLoadingDialog == null && mProgressLoginDialog == null ){
			mProgressLoadingDialog = ProgressDialog.show(view.getContext(), "가계부 웹페이지 로딩", " 3G는 더 기다려 주세요\n뒤로가기 버튼을 누르면 사라집니다.", false);
			mProgressLoadingDialog.setCancelable(true);
		}
	}
	
	int writeState = QuickWriter.WRITE_READY;
	@Override
	public void onPageFinished(WebView view, String url) {
		Log.i("beonit", url);
		if( mProgressLoadingDialog != null ){
			mProgressLoadingDialog.dismiss();
			mProgressLoadingDialog = null;
		}
		if( id == null || passwd == null || id.length() == 0 || passwd.length() == 0 )
			return;
		if (url.equals("http://m.icashhouse.co.kr/")) {
			switch (writeState) {
			case QuickWriter.WRITE_READY:
				Log.v("beonit", "onPageFinished, WRITE_READY -> WRITE_LOGIN_ATTEMPT");
				view.loadUrl("javascript:Username.value='" + id + "'");
				view.loadUrl("javascript:Password.value='" + passwd + "'");
				view.loadUrl("javascript:login.submit( check_login( document.getElementById('frm_login') ) )");
				JSInterfaceICash iJS = new JSInterfaceICash();
				view.addJavascriptInterface(iJS, "HTMLOUT");
				writeState = QuickWriter.WRITE_LOGIN_ATTEMPT;
				if( mProgressLoginDialog != null )
					mProgressLoginDialog.setMessage("3G는 더 기다려 주세요\n로그인 시도");
				view.reload();
				break;
			case QuickWriter.WRITE_LOGIN_ATTEMPT:
				Log.v("beonit", "onPageFinished, WRITE_LOGIN_ATTEMPT -> WRITE_LOGIN_FAIL || WRITE_LOGIN_SUCCESS");
				view.loadUrl("http://m.icashhouse.co.kr/tra_transaction.php");
				break;
			}
		} else if( url.equals("http://m.icashhouse.co.kr/tra_transaction.php") ) {
			view.setWillNotDraw(false);			
			closeDialog();
		} else {
			closeDialog();
		}
	}
	
	final class JSInterfaceICash {
		public void checkLoginResult(final String html){
			Log.i("beonit", "checkLogin : " + html);
			if( html.contains("기타설정") ){
				writeState = QuickWriter.WRITE_LOGIN_SUCCESS;
			}
			else{
				writeState = QuickWriter.WRITE_LOGIN_FAIL;
			}
			return;
		}
	}
	
	public void closeDialog(){
		if( mProgressLoginDialog != null ){
			mProgressLoginDialog.dismiss();
			mProgressLoginDialog = null;
		}
	}
	
	private void errorNotify(WebView view, String title, String message) {
		AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
		alert.setTitle( title );
		alert.setMessage( message );
		alert.setPositiveButton(
				 "닫기", new DialogInterface.OnClickListener() {
				    public void onClick( DialogInterface dialog, int which) {
				        dialog.dismiss();   //닫기
				    }
				});
		alert.show();
	}
	
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
		closeDialog();
		errorNotify(view, "로딩 에러", "네이버 로딩에 실패했습니다" );
	}

}