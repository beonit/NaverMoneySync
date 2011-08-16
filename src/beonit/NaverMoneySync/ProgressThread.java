package beonit.NaverMoneySync;

import android.os.Handler;

public class ProgressThread extends Thread {
	Handler mHandler;
	QuickWriter writer;
	String items;
    ProgressThread(Handler h, QuickWriter writer, String items) {
    	this.writer = writer;
    	this.items = items;
        mHandler = h;
    }
    
    public void run() {
    	writer.quickWrite(items);
    	int state = QuickWriter.WRITE_READY;
    	int newState = QuickWriter.WRITE_READY;
    	for( int i=0; i<100; i++ ){
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			newState = writer.getSendState();
    		if( state != newState ){
    			state = newState;
    			if( mHandler != null )
    				mHandler.sendEmptyMessage(state);
    			i = 0; // 한 스텝마다 10초씩 기다릴 수 있다.
    		}
    		if( state == QuickWriter.WRITE_SUCCESS || state == QuickWriter.WRITE_FAIL || state == QuickWriter.WRITE_LOGIN_FAIL || state == QuickWriter.WRITE_FAIL_REGISTER ){
    			return;
    		}
    	}
    	if( mHandler != null )
    		mHandler.sendEmptyMessage(QuickWriter.WRITE_FAIL);
   }
}
