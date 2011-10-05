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
			
			// ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ß±ï¿½
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
		    
		    // ï¿½ï¿½ï¿½ï¿½ SMS ï¿½ï¿½ ï¿½Î¸ï¿½ï¿½ï¿½.
			SharedPreferences prefs = context.getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
			StringBuilder item = new StringBuilder(prefs.getString("items", "")).append("; ");
			// ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ smsï¿½ï¿½ ï¿½ï¿½ï¿½Ã¿ï¿½ ï¿½ï¿½ ï¿½ï¿½ï¿½ì¸¦ ï¿½ï¿½ï¿½ï¿½ï¿½Ñ´ï¿½.
			boolean cardMsg = false;
		    for( SmsMessage msg : messages ) {
		        if( !isCardSender( msg.getOriginatingAddress().replace("\n", "").replace("-", "").replace(" ", ""), context ) )
		        	continue;
		        cardMsg = true;
		        Log.v("beonit", "sender : " + msg.getOriginatingAddress());
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

			// ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½.
			if( id == null || passwd == null || id.length() == 0 || passwd.length() == 0 ){
				Log.i("beonit", "id/pw ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½");
		    	Notification notification = new Notification(R.drawable.icon, "ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½Ï´ï¿½.", 0);
		    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
		    	Intent failIntent = new Intent(context, ViewMain.class);
		    	failIntent.putExtra("goto", 1);
		    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, failIntent, 0);
		    	notification.setLatestEventInfo(context, "ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½", "ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½Ï´ï¿½.", pendingIntent);
				NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		    	nm.notify(ViewMain.NOTI_ID, notification);
		    	return;
			}else if(!checkNetwork(context)){
				Log.i("beonit", "ï¿½ï¿½Æ®ï¿½ï¿½Å© ï¿½Èµï¿½");
		    	Notification notification = new Notification(R.drawable.icon, "ï¿½ï¿½ï¿½Í³ï¿½ ï¿½ï¿½ï¿½ï¿½ ï¿½Ò°ï¿½", 0);
		    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
		    	Intent failIntent = new Intent(context, ViewMain.class);
		    	failIntent.putExtra("goto", 1);
		    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, failIntent, 0);
		    	notification.setLatestEventInfo(context, "ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½", "ï¿½ï¿½ï¿½Í³ï¿½ ï¿½ï¿½ï¿½ï¿½ ï¿½Ò°ï¿½ ï¿½ï¿½ï¿½ï¿½", pendingIntent);
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
		nums.add("0220085000");  // WOORI - ï¿½Ü±ï¿½
		nums.add("15884000");    // BC
		nums.add("15888100");    // LOTTE
		nums.add("15887000");    // CITY
		nums.add("15881000");    // CITY
		nums.add("15887200");    // ?
		nums.add("15991155");    // HANA
		nums.add("15991111");    // HANA
		nums.add("15881788");    // KB
		nums.add("15889999");    // KB
		nums.add("16449999");    // KB
		nums.add("15882100");    // ï¿½ï¿½ï¿½ï¿½
		nums.add("15881600");	 // ï¿½ï¿½ï¿½ï¿½2
		nums.add("15776000");    // HYUNDAI
		nums.add("15776200");    // HYUNDAI
		nums.add("15778000");    // ï¿½ï¿½ï¿½ï¿½
		nums.add("15884560");    // ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½È­ï¿½ï¿½ Ä«ï¿½ï¿½
		nums.add("15880056");    // ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½
		nums.add("15773997");    // ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ W ï¿½ï¿½ï¿½ï¿½Ä«ï¿½ï¿½
		nums.add("15881155");    // ï¿½Ï³ï¿½ sk
		nums.add("15881599");    // ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½
		// ï¿½ï¿½ï¿½ï¿½ ï¿½ï¼º
		nums.add("15881515");    // ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½
		// ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½
		// ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½
		nums.add("15881515");   // ï¿½ï¿½ï¿½ï¿½
		// ï¿½ï¿½ï¿½ï¿½
		nums.add("15888801");   // ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½Ý°ï¿½
		nums.add("15881900");   // ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½Ý°ï¿½
		nums.add("15887000");   // ï¿½Ñ¹ï¿½ï¿½ï¿½ï¿½ï¿½
		nums.add("15884114");   // ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½		
		nums.add("01094858469"); // test
		
		for( String num : nums ){
			if( sender.contains(num) ){
				Log.i("beonit", "cell phone num matched");
		    	Notification notification = new Notification(R.drawable.icon, "Ä«µå ¹®ÀÚ ¼ö½Å", 0);
		    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
		    	Intent successIntent = new Intent();
		    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, successIntent, 0);
		    	notification.setLatestEventInfo(context, "ÀüÈ­¹øÈ£", num, pendingIntent);
				NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		    	nm.notify(ViewMain.NOTI_ID, notification);
				return true;
			}
		}
		Log.i("beonit", "cell phone num not matched");
    	Notification notification = new Notification(R.drawable.icon, "±×³É ¹®ÀÚ ¼ö½Å", 0);
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	Intent successIntent = new Intent();
    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, successIntent, 0);
    	notification.setLatestEventInfo(context, "Ä«µå»ç ÀüÈ­¹øÈ£ ¾Æ´Ô", "´©¸£½Ã¸é »ç¶óÁý´Ï´Ù.", pendingIntent);
		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    	nm.notify(ViewMain.NOTI_ID, notification);
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
