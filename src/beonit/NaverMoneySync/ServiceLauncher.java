package beonit.NaverMoneySync;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ServiceLauncher extends Activity {

	ICommunicator mIRemoteService = null;
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // Following the example above for an AIDL interface,
	        // this gets an instance of the IRemoteInterface, which we can use to call on the service
	        mIRemoteService = ICommunicator.Stub.asInterface(service);
	    }

	    // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.e("beonit", "Service has unexpectedly disconnected");
	        mIRemoteService = null;
	    }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.i("beonit", "service launcher activity start");
		bindService(new Intent(ICommunicator.class.getName()), mConnection, BIND_AUTO_CREATE);
		boolean serviceCallStatus = false;
		try {
			for( int i=0; i<5; i++){
				if( mIRemoteService == null ){
					Thread.sleep(5000);
					continue;
				}
				Log.i("beonit", "test call");
				mIRemoteService.onRecvSMS();
				serviceCallStatus = true;
			}
			// mIRemoteService.onRecvSMS(items, id, password)
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if( serviceCallStatus == false )
			Log.e("beonit", "service service call fail");
	}
	

    
}
