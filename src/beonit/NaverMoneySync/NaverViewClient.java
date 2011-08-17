package beonit.NaverMoneySync;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NaverViewClient extends WebViewClient {
	/**
	 * 
	 */
	private String id;
	private String passwd;
	private ProgressDialog mProgressLoginDialog = null;
	private ProgressDialog mProgressLoadingDialog = null;
	
	public NaverViewClient(String id, String passwd) {
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
	
	public void onPageFinished(WebView view, String url){
		Log.i("beonit", url);
		if( mProgressLoadingDialog != null ){
			mProgressLoadingDialog.dismiss();
			mProgressLoadingDialog = null;
		}
		if( id == null || passwd == null || id.length() == 0 || passwd.length() == 0 )
			return;
		if( url.equals("https://nid.naver.com/nidlogin.login?svctype=262144&url=http://beta.moneybook.naver.com/m/view.nhn?method=monthly")){
			view.loadUrl("javascript:id.value='"+ id +"'");
			view.loadUrl("javascript:pw.value='"+ passwd +"'");
			view.loadUrl("javascript:loginform.submit()");
			MyJavaScriptInterface iJS = new MyJavaScriptInterface();
			view.addJavascriptInterface(iJS, "HTMLOUT");
			if( mProgressLoginDialog != null )
				mProgressLoginDialog.setMessage("3G는 더 기다려 주세요\n로그인 시도");
		}
		else if( url.equals("https://nid.naver.com/nidlogin.login?svctype=262144") ){
			view.loadUrl("javascript:window.HTMLOUT.showHTML('' + document.body.getElementsByTagName('span')[3].innerHTML);");
			if( mProgressLoginDialog != null )
				mProgressLoginDialog.setMessage("3G는 더 기다려 주세요\n로그인 처리");
			view.setWillNotDraw(false);
		}else if( url.contains("http://static.nid.naver.com/login/sso/finalize.nhn") ){
			if( mProgressLoginDialog != null )
				mProgressLoginDialog.setMessage("3G는 더 기다려 주세요\n가계부 로딩 중");
		}else if( url.equals("http://beta.moneybook.naver.com/m/view.nhn?method=monthly") ){
			// 정상 로딩 완료
			closeDialog();
		}else if( url.equals("http://beta.moneybook.naver.com/m/mbookUser.nhn")){
			closeDialog();
    		errorNotify(view, "가계부 가입 안됨", "현재 앱을 닫고 모바일 웹/PC 로 먼저 약관동의를 처리하고 접속해 주세요." );
		}else{
			closeDialog();
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
	
	public void closeDialog(){
		if( mProgressLoginDialog != null ){
			mProgressLoginDialog.dismiss();
			mProgressLoginDialog = null;
		}
	}
	
	final class MyJavaScriptInterface {
	    public void showHTML(String html) {
	        if( html.contains("오류") ){
	        	mProgressLoginDialog.dismiss();
	        	mProgressLoginDialog = null;
	        }
	    }  
	}  

	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
		closeDialog();
		errorNotify(view, "로딩 에러", "네이버 로딩에 실패했습니다" );
	}
}