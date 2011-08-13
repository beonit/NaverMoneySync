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
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

	static final String SMS_RECV = "android.provider.Telephony.SMS_RECEIVED";
	static final String NOTI_CLEAR = "beonit.NOTI_CLEAR";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.w("beonit", "smsReceiver onReceive");
		if (intent.getAction().equals(SMS_RECV)) {
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
		    
		    // 여러개의 sms가 동시에 올 경우를 생각한다.
		    String items = new String("");
		    for( SmsMessage msg : messages ) {
		        if( !isCardSender( msg.getOriginatingAddress() ) )
		        	return;
		        Log.v("beonit", "sender : " + msg.getOriginatingAddress());
		        items = items + msg.getDisplayMessageBody() + ";";
		    }
		    
		    // load failed saved pref
			SharedPreferences prefs = context.getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
			items = items + prefs.getString("items", "");
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
		    	Log.e("beonit", "saved items" + items);
		    	ed.putString("items", items);
			    ed.commit();
		    	return;
			}else{
		    	// clear saved preference
				ed.putString("items", "");
				ed.commit();
			}
		    
		    // 전송
		    QuickWriter qw = new QuickWriter(id, passwd, context);
		    qw.quickWrite(items);
			return;
		}
	}

	private boolean isCardSender(String sender ) {
		ArrayList<String> nums = new ArrayList<String>();
		nums.add("01094784068"); // test
		nums.add("15888900");    // SAMSUNG
		nums.add("15888700");    // SAMSUNG
		nums.add("15886700");    // KEB
		nums.add("15884000");    // WOORI
		nums.add("0220085000");  // WOORI - 외국
		nums.add("15884000");    // BC
		nums.add("15888100");    // LOTTE
		nums.add("15887000");    // CITY
		nums.add("15887200");    // ?
		nums.add("15991155");    // HANA
		nums.add("15881688");    // KB
		for( String num : nums )
			if( sender.equals(num) )
				return true;
		return false;
	}
}
