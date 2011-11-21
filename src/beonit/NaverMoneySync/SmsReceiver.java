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
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
	
	static final String SMS_RECV = "android.provider.Telephony.SMS_RECEIVED";
	static final String NOTI_CLEAR = "beonit.NOTI_CLEAR";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// send information to remote service
		Log.w("beonit", "smsReceiver onReceive");
		if (intent.getAction().equals(SMS_RECV)) {
			
			// 정보 갖추기
			Log.w("beonit", "SMS_RECV");
			Bundle bundle = intent.getExtras();
			if (bundle == null) {
				Log.v("beonit", "bundle == null");
				return ;
			}
			
			Object[] pdusObj = (Object[])bundle.get("pdus");
			if (pdusObj == null) {
				Log.v("beonit", "pdusObj == null");
				return ;
			}
			
			SmsMessage[] messages = new SmsMessage[pdusObj.length];
		    for(int i = 0; i < pdusObj.length; i++) {
		        messages[i] = SmsMessage.createFromPdu((byte[])pdusObj[i]);
		    }
		    
		    if( messages.length < 0 ){
		    	Log.v("beonit", "msg len : " + messages.length ); 
		    	return;
		    }
		    
		    // 기존 SMS 를 부른다.
			SharedPreferences prefs = context.getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
			StringBuilder item = new StringBuilder(prefs.getString("items", "")).append("; ");
			// 여러개의 sms가 동시에 올 경우를 생각한다.
			boolean cardMsg = false;
			StringBuffer numStr;
		    for( SmsMessage msg : messages ) {
		    	// 전화번호에서 숫자만 뽑아온다.
		    	numStr = new StringBuffer();
		    	for( char c : msg.getDisplayOriginatingAddress().toCharArray() )
					if( Character.isDigit(c) )
						numStr.append(c);
				// 카드문자인지 확인
		        if( !isCardSender( numStr.toString(), context ) )
		        	continue;
		        cardMsg = true;
		        Log.v("beonit", "sender : " + numStr.toString());
		        Log.v("beonit", "msg : " + msg.getDisplayMessageBody());
		        item.append( msg.getDisplayMessageBody().replace("\n", " ").replace("\r", " ") + "; " );
		    }
		    
		    if( !cardMsg )
		    	return;
		    
		    // save sms items
	    	Log.e("beonit", "saved items" + item);
	    	Editor ed = prefs.edit();
	    	ed.putString("items", item.toString());
		    ed.commit();
		    
		    Log.i("beonit", "get id/passwd");
			String id = prefs.getString("naverID", null);
			String passwd = null;
			try {
				passwd = SimpleCrypto.decrypt("SECGAL", prefs.getString("naverPasswd", null));
			} catch (Exception e) {
				Log.e("beonit", "simple crypto decrypt fail");
				e.printStackTrace();
			}

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
			    return;
			}
			
			Log.i("beonit", "start activity call");
			Intent launcherIntent = new Intent( context, SmsReceiverActivity.class );
			launcherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(launcherIntent);
			return;
		}
	}

	private boolean isCardSender(String sender, Context context ) {
		ArrayList<String> nums = new ArrayList<String>();
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
		nums.add("15661000");	 // CITY
		nums.add("15887200");    // ?
		nums.add("15991155");    // HANA
		nums.add("15991111");    // HANA
		nums.add("15881788");    // KB
		nums.add("15889999");    // KB
		nums.add("16449999");    // KB
		nums.add("15882100");    // 농협
		nums.add("15881600");	 // 농협2
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
		// 신한카드
		nums.add("15888801");   // 새마을 금고
		nums.add("15881900");   // 새마을 금고
		nums.add("15887000");   // 한미은행
		nums.add("15884114");   // 조흥은행		
		nums.add("01094858469"); // test
		nums.add("15447000");	// 신한카드
		nums.add("15447200");	// 신한카드
		nums.add("15882588");   // 기업은행
		nums.add("15662566");   // 기업은행
		
		for( String num : nums ){
			if( sender.contains(num) ){
//				Log.i("beonit", "cell phone num matched");
//		    	Notification notification = new Notification(R.drawable.icon, "카드 문자 수신", 0);
//		    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
//		    	Intent successIntent = new Intent();
//		    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, successIntent, 0);
//		    	notification.setLatestEventInfo(context, "전화번호", num, pendingIntent);
//				NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//		    	nm.notify(ViewMain.NOTI_ID, notification);
				return true;
			}
		}
//		Log.i("beonit", "cell phone num not matched");
//    	Notification notification = new Notification(R.drawable.icon, "그냥 문자 수신", 0);
//    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
//    	Intent successIntent = new Intent();
//    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, successIntent, 0);
//    	notification.setLatestEventInfo(context, "카드사 번호 아님 : " + sender, "누르면 사라집니다.", pendingIntent);
//		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//    	nm.notify(ViewMain.NOTI_ID, notification);
		return false;
	}
	
	// check network        
    public boolean checkNetwork(Context context) 
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info.isConnected();
    }
    
}
