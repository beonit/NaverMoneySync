package beonit.NaverMoneySync;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

	static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.w("beonit", "receive sms");
		if (intent.getAction().equals(ACTION)) {
			
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
			
			// loadIdPasswd();
			SharedPreferences prefs = context.getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
			String id = prefs.getString("naverID", null);
			String passwd = prefs.getString("naverPasswd", null);
			if( id == null || passwd == null ){
				Log.v("beonit", "id or passwd == null");
				return;
			}
			
			SmsMessage[] messages = new SmsMessage[pdusObj.length];
		    for(int i = 0; i < pdusObj.length; i++) {
		        messages[i] = SmsMessage.createFromPdu((byte[])pdusObj[i]);
		    }
		    
		    if( messages.length < 0 ){
		    	Log.v("beonit", "msg len : " + messages.length ); 
		    	return;
		    }
		    
		    String items = new String("");
		    QuickWriter qw = new QuickWriter(id, passwd, context);
		    for( SmsMessage msg : messages ) {
//		        msg.getOriginatingAddress(); // 발신번호
		        if( !isCardSender(msg.getOriginatingAddress()) )
		        	return;
		        Log.v("beonit", "sender : " + msg.getOriginatingAddress());
		        items = items + msg.getDisplayMessageBody() + ";";
		    }
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
		for( String num : nums )
			if( sender.equals(num) )
				return true;
		return false;
	}
}
