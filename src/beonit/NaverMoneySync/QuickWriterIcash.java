package beonit.NaverMoneySync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;

public class QuickWriterIcash extends QuickWriter implements IQuickWriter {
	
	public QuickWriterIcash(String id, String passwd, Context context){
		super(id, passwd);
	}
	
	public boolean quickWrite(String itemsStr){
		// https://www.icashhouse.co.kr:50103/api_android/insert.php
		// GET or POST
		// mb_id : 사용자 아이디
		// mb_password : 사용자 비밀번호
		// date : 날짜. ex) 2011-12-01
		// item : 품목 혹은 거래처. ex) 김밥(배고파서먹음)
		// money : 금액. ex) 21000
		// l_acc_type : 차변의 계정. ex) e
		// l_acc_id : 차변의 항목 고유번호. ex) 917773
		// r_acc_type : 대변의 계정. ex) a
		// r_acc_id : 대변의 항목 고유번호. ex) 827711
		StringBuilder uri = new StringBuilder();
    	String itemStr = null;
		try {
			itemStr = URLEncoder.encode("한글이 잘 써지나요?" , "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e("beonit", "item str encode fail");
			e.printStackTrace();
			return false;
		}
		uri = uri.append("https://www.icashhouse.co.kr:50103/api_android/insert.php").append("?mb_id=beonit").append("&mb_password=akdma59")
					.append("&date=2011-9-30").append("&item=").append( itemStr )
					.append("&money=1,000")
					.append("&l_acc_type=e").append("&l_acc_id=917773")
					.append("&r_acc_type=e").append("&r_acc_id=827711");
		InputStream in = null;
		try {
			in = executeHttpGet( uri.toString() );
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("beonit", "executeHttpGet fail");
			return false;
		}
		if( in == null ){
			Log.e("beonit", "input stream is null");
			return false;
		}
		InputStreamReader isr = new InputStreamReader(in);
	    BufferedReader br = new BufferedReader(isr);
	    String s;
	    try {
			while ((s = br.readLine()) != null) {
				Log.i("beonit", "result : " + s);
			}
			isr.close();
		} catch (IOException e) {
			Log.e("beonit", "input stream is null");
			e.printStackTrace();
		}

        return true;
	}
	
	public InputStream executeHttpGet(String url) throws Exception {
		Log.i("beonit", "request url : " + url );
		InputStream content = null;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(url));
			content = response.getEntity().getContent();
		} catch (Exception e) {
			Log.i("beonit", "http request fail");
			e.printStackTrace();
		}
		return content;
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	// 이곳에서 성고 실패에 관한 notify, 저장관리를 한다.
	// notify 의 경우 각 가계부 사이트마다 실패 사유가 다양할 수 있기 때문에 각 사이트 특성을 파생시킨 클래스에서 해 주어야 한다.
    ///////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void stop() {
		
	}
}
