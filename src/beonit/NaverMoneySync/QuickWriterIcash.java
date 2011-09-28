package beonit.NaverMoneySync;

import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

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
		uri = uri.append("?mb_id=beonit").append("&mb_password=akdma59")
					.append("&date=2011-12-01").append("&item=").append("itemsStr")
					.append("&money=1000").append("&l_acc_type=")
					.append("&l_acc_id=").append("&r_acc_type=")
					.append("&r_acc_id=");
		try {
			executeHttpGet(uri.toString());
		} catch (Exception e) {
			e.printStackTrace();
			writeState = WRITE_FAIL;
			return false;
		}
        return true;
	}
	
	public InputStream executeHttpGet(String url) throws Exception {
		InputStream content = null;
		try {
			// TODO. encode uri
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(url));
			content = response.getEntity().getContent();
		} catch (Exception e) {
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
