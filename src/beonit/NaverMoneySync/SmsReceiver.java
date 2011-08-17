package beonit.NaverMoneySync;

import java.util.ArrayList;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

	static final String SMS_RECV = "android.provider.Telephony.SMS_RECEIVED";
	static final String NOTI_CLEAR = "beonit.NOTI_CLEAR";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.w("beonit", "smsReceiver onReceive");
		if (intent.getAction().equals(SMS_RECV)) {
			// 정보 갖추기
			Log.w("beonit", "SMS_RECV");
			Bundle bundle = intent.getExtras();
			if (bundle == null) {
				Log.v("beonit", "bundle == null");
				return ;
			}
			Log.i("beonit", "get bundle pass");
			
			Object[] pdusObj = (Object[])bundle.get("pdus");
			if (pdusObj == null) {
				Log.v("beonit", "pdusObj == null");
				return ;
			}
			Log.i("beonit", "pdusObj pass");
			
			SmsMessage[] messages = new SmsMessage[pdusObj.length];
		    for(int i = 0; i < pdusObj.length; i++) {
		        messages[i] = SmsMessage.createFromPdu((byte[])pdusObj[i]);
		    }
		    
		    if( messages.length < 0 ){
		    	Log.v("beonit", "msg len : " + messages.length ); 
		    	return;
		    }
		    Log.i("beonit", "msg len pass");
		    
		    // 여러개의 sms가 동시에 올 경우를 생각한다.
		    ArrayList<String> items = new ArrayList<String>();
		    for( SmsMessage msg : messages ) {
		        if( !isCardSender( msg.getOriginatingAddress() ) )
		        	continue;
		        Log.v("beonit", "sender : " + msg.getOriginatingAddress());
		        Log.v("beonit", "msg : " + msg.getDisplayMessageBody());
		        items.add( msg.getDisplayMessageBody().replace("\n", " ").replace("\r", " ") );
		    }
		    if( items.size() == 0 )
		    	return;
		    
		    // 이미 실패한 문자를 로드해서 한번의 통신에 한번에 쓴다.
			SharedPreferences prefs = context.getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
			String failsStr = prefs.getString("items", null);
			if( failsStr != null ){
				for( String fail : failsStr.split(";") )
					items.add(fail);
			}
			
			String id = prefs.getString("naverID", null);
			String passwd = null;
			try {
				passwd = SimpleCrypto.decrypt("SECGAL", prefs.getString("naverPasswd", null));
			} catch (Exception e) {
				Log.e("beonit", "simple crypto decrypt fail");
				e.printStackTrace();
			}
			Editor ed = prefs.edit();

			// 계정 정보가 없으면 끝.
			if( id == null || passwd == null || id.length() == 0 || passwd.length() == 0 ){
				Log.i("beonit", "id/pw 정보 없음");
		    	Notification notification = new Notification(R.drawable.icon, "계정 정보가 없습니다.", 0);
		    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
		    	Intent failIntent = new Intent(context, ViewMain.class);
		    	failIntent.putExtra("goto", 1);
		    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, failIntent, 0);
		    	notification.setLatestEventInfo(context, "가계부 쓰기 실패", "계정 정보가 없습니다.", pendingIntent);
				NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		    	nm.notify(ViewMain.NOTI_ID, notification);
		    	// update saved preference
		    	failsStr = "";
		    	for( String item : items )
		    		failsStr = failsStr + item + ";";
		    	Log.e("beonit", "saved items" + failsStr);
		    	ed.putString("items", failsStr);
			    ed.commit();
		    	return;
			}else if(!checkNetwork(context)){
				Log.i("beonit", "네트워크 안됨");
		    	Notification notification = new Notification(R.drawable.icon, "인터넷 사용 불가", 0);
		    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
		    	Intent failIntent = new Intent(context, ViewMain.class);
		    	failIntent.putExtra("goto", 1);
		    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, failIntent, 0);
		    	notification.setLatestEventInfo(context, "가계부 쓰기 실패", "인터넷 사용 불가 상태", pendingIntent);
				NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		    	nm.notify(ViewMain.NOTI_ID, notification);
		    	// update saved preference
		    	failsStr = "";
		    	for( String item : items )
		    		failsStr = failsStr + item + ";";
		    	Log.e("beonit", "saved items" + failsStr);
		    	ed.putString("items", failsStr);
			    ed.commit();
			    return;
			}else{
		    	// clear saved preference
				ed.putString("items", "");
				ed.commit();
			}
		    
		    // 전송
		    QuickWriterNaver writer = new QuickWriterNaver(id, passwd, context);
			writer.setFailSave(true);
			writer.setResultNoti(true);
			Log.i("beonit", "ProgressThread " + items);
		    ProgressThread progressThread = new ProgressThread(mHandler, writer, items);
			progressThread.start();
			return;
		}
	}

	private boolean isCardSender(String sender ) {
		ArrayList<String> nums = new ArrayList<String>();
		nums.add("01094858469"); // test
		nums.add("01094784068"); // test
		nums.add("15888900");    // SAMSUNG
		nums.add("15888700");    // SAMSUNG
		nums.add("15886700");    // KEB
		nums.add("15883000");    // KEB    
		nums.add("15884000");    // WOORI
		nums.add("15885000");    // WOORI
		nums.add("0220085000");  // WOORI - 외국
		nums.add("15884000");    // BC
		nums.add("15888100");    // LOTTE
		nums.add("15887000");    // CITY
		nums.add("15881000");    // CITY
		nums.add("15887200");    // ?
		nums.add("15991155");    // HANA
		nums.add("15991111");    // HANA
		nums.add("15881688");    // KB
		nums.add("15889999");    // KB
		nums.add("15882100");    // 농협
		nums.add("15776000");    // HYUNDAI
		nums.add("15776200");    // HYUNDAI
		nums.add("15778000");    // 신한
		nums.add("15884560");    // 현대 백화점 카드
		nums.add("15880056");    // 동양종금
		nums.add("15773997");    // 동양종금 W 제휴카드
		nums.add("15881155");    // 하나 sk
		nums.add("15881599");    // 제일은행
		// 동양 삼성
		nums.add("15881515");    // 기업은행
		// 제주은행
		// 광주은행
		nums.add("15881515");   // 수협
		// 축협
		nums.add("15888801");   // 새마을 금고
		nums.add("15881900");   // 새마을 금고
		nums.add("15887000");   // 한미은행
		nums.add("15884114");   // 조흥은행		
		
		for( String num : nums )
			if( sender.equals(num) )
				return true;
		return false;
	}
	
	// check network        
    public boolean checkNetwork(Context context) 
    {
        boolean result = true;
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        // boolean isWifiAvail = ni.isAvailable();
        boolean isWifiConn = ni.isConnected();
        ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // boolean isMobileAvail = ni.isAvailable();
        boolean isMobileConn = ni.isConnected();
        if (isWifiConn == false && isMobileConn == false)
            result = false;
        return result;
    }
    
    // send
    private Handler mHandler = new SyncHandler(); 
    public class SyncHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case QuickWriterNaver.WRITE_READY:
				break;
			case QuickWriterNaver.WRITE_LOGIN:
				break;
			case QuickWriterNaver.WRITE_LOGIN_SUCCESS:
				break;
			case QuickWriterNaver.WRITE_WRITING:
				break;
			case QuickWriterNaver.WRITE_SUCCESS:
				break;
			case QuickWriterNaver.WRITE_LOGIN_FAIL:
				break;
			case QuickWriterNaver.WRITE_FAIL:
				break;
			case QuickWriterNaver.WRITE_FAIL_REGISTER:
				break;
			default:
				break;
			}
		}
	};
}
