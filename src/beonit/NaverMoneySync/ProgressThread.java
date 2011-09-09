package beonit.NaverMoneySync;

import java.util.List;

import android.os.Handler;
import android.util.Log;

public class ProgressThread extends Thread {
	Handler mHandler;
	QuickWriter writer;
	List<String> items;
    ProgressThread(Handler h, QuickWriter writer, List<String> items) {
    	this.writer = writer;
    	this.items = items;
        mHandler = h;
    }
    
    public void run() {
    	Log.i("beonit", "run start");
    	writer.quickWrite(items);
    	int state = QuickWriterNaver.WRITE_READY;
    	int newState = QuickWriterNaver.WRITE_READY;
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
    		}else
    			continue;
    		if( state == QuickWriterNaver.WRITE_SUCCESS || state == QuickWriterNaver.WRITE_FAIL || state == QuickWriterNaver.WRITE_LOGIN_FAIL || state == QuickWriterNaver.WRITE_FAIL_REGISTER ){
    			return;
    		}
    	}
    	if( mHandler != null ){
    		mHandler.sendEmptyMessage(QuickWriterNaver.TIME_OUT);
    	}
   }
}
