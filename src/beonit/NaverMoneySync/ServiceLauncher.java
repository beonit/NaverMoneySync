package beonit.NaverMoneySync;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
	    	Log.i("beonit", "on service connected");
	        mIRemoteService = ICommunicator.Stub.asInterface(service);
	        try {
				mIRemoteService.onRecvSMS();
			} catch (RemoteException e) {
				Log.i("beonit", "service call error");
				e.printStackTrace();
			}
			Log.i("beonit", "service unbind");
			unbindService(mConnection);
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
		bindService(new Intent(ICommunicator.class.getName()), mConnection, BIND_AUTO_CREATE);
		finish();
	}
	

    
}
