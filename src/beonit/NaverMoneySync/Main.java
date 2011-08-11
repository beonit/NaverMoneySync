package beonit.NaverMoneySync;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

public class Main extends TabActivity {
    /** Called when the activity is first created. */
	
//	static final String logTag = "SmsReceiver";
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.e("beonit", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TabHost mTabHost = getTabHost();
        
        mTabHost.addTab(mTabHost.newTabSpec("tab_test1")
        		.setIndicator("현금 사용")
        		.setContent(R.id.viewRecord)
        		);
        mTabHost.addTab(mTabHost.newTabSpec("tab_test2")
        		.setIndicator("네이버 계정")
        		.setContent(R.id.viewAccount)
        		);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        
        SharedPreferences prefs = getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
        EditText editTextNaverId = (EditText)findViewById(R.id.EditTextAccount);
        EditText editTextNaverPasswd = (EditText)findViewById(R.id.EditTextPasswd);
        editTextNaverId.setText(prefs.getString("naverID", ""));
        editTextNaverPasswd.setText(prefs.getString("naverPasswd", ""));
        
        Button recordDate = (Button)findViewById(R.id.EditTextRecordDate);
        // get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
    	recordDate.setText(
            new StringBuilder()
                    // Month is 0 based so add 1
            		.append(mYear).append("년 ")
                    .append(mMonth + 1).append("월 ")
                    .append(mDay).append("일")
                    );
    }
    
    public void onSubmitAccount(View view){
        EditText editTextNaverId = (EditText)findViewById(R.id.EditTextAccount);
        EditText editTextNaverPasswd = (EditText)findViewById(R.id.EditTextPasswd);

        SharedPreferences prefs = getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("naverID", editTextNaverId.getText().toString());
		editor.putString("naverPasswd", editTextNaverPasswd.getText().toString());
		editor.commit();
		
		Toast.makeText(this, "Saved ^^", 3000).show();
    }
    
    private int mYear;
    private int mMonth;
    private int mDay;
    static final int DATE_DIALOG_ID = 0;
    public void onButtonDatePick(View v) {
        showDialog(DATE_DIALOG_ID);
    }
    
    ProgressThread progressThread;
    private ProgressDialog mProgressDialog;
	TabActivity activity = this;

    public void onSubmitRecord(View view){
    	String id, passwd, items;
    	// 네이버 계정 설정
    	SharedPreferences prefs = getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
		id = prefs.getString("naverID", null);
		passwd = prefs.getString("naverPasswd", null);
		if( id == null || passwd == null ){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle( "계정 없음" );
			alert.setMessage( "네이버 계정을 설정해 주세요" );
			alert.setPositiveButton(
					 "닫기", new DialogInterface.OnClickListener() {
					    public void onClick( DialogInterface dialog, int which) {
					        dialog.dismiss();   //닫기
					    }
					});
			alert.show();
			return;
		}
		
    	// 내용 빈칸 확인
    	EditText editText = (EditText)findViewById(R.id.EditTextRecordContents);
    	EditText editMoney = (EditText)findViewById(R.id.EditTextRecordMoney);
    	if( editText.getText().length() == 0 || editMoney.getText().length() == 0 ){
    		AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle( "내용 없음" );
			alert.setMessage( "가계부 내용을 채워주세요." );
			alert.setPositiveButton(
					 "닫기", new DialogInterface.OnClickListener() {
					    public void onClick( DialogInterface dialog, int which) {
					        dialog.dismiss();   //닫기
					    }
					});
			alert.show();
    		return;
    	}
    	
		// 날짜 v 사용내역 v 카드 or 현금 v 금액 (v=공백)
		String contents = editText.getText().toString();
		contents.replace(" ", "");
		items = new String( new StringBuilder().append(mMonth+1).append("/")
								.append(mDay).append(" ")
								.append(contents).append(" ")
								.append("현금 ")
								.append(editMoney.getText()).append("원")
								);
		
		// network state check
    	if( checkNetwork() == false ){
    		AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle( "통신 불가능" );
			alert.setMessage( "DB에 저장됩니다." );
			alert.setPositiveButton(
					 "닫기", new DialogInterface.OnClickListener() {
					    public void onClick( DialogInterface dialog, int which) {
					        dialog.dismiss();   //닫기
					    }
					});
			alert.show();
			return;
    	}
    	
		mProgressDialog = ProgressDialog.show(this, "잠시만 기다려 주세요", "네이버로 전송 중...", false);

		QuickWriter writer = new QuickWriter(id, passwd, this);
		progressThread = new ProgressThread(mHandler, writer, items, this);
		progressThread.start();
    }
    
    private class ProgressThread extends Thread {
    	Handler mHandler;
    	QuickWriter writer;
    	String items;
        ProgressThread(Handler h, QuickWriter writer, String items, Context context) {
        	this.writer = writer;
        	this.items = items;
            mHandler = h;
        }
        
        public void run() {
        	while( ! writer.quickWrite(items) ){
        		try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					mHandler.sendEmptyMessage( SYNC_NETFAIL );
					e.printStackTrace();
				}
        	}
        	mHandler.sendEmptyMessage( SYNC_SUCCESS );
       }
    }
    
	final static int SYNC_SUCCESS = 0;
	final static int SYNC_NETFAIL = 1;
    // send
    private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss(); // ProgressDialog 종료
			AlertDialog.Builder alert = new AlertDialog.Builder(activity);
			switch (msg.what) {
			case Main.SYNC_SUCCESS:
		    	EditText editText = (EditText)findViewById(R.id.EditTextRecordContents);
		    	EditText editMoney = (EditText)findViewById(R.id.EditTextRecordMoney);
				editText.setText("");
				editMoney.setText("");
				alert.setTitle( "입력 성공" );
				alert.setMessage( "저장되었습니다." );
				break;
			case Main.SYNC_NETFAIL:
				alert.setTitle( "통신 실패" );
				alert.setMessage( "DB에 저장됩니다." );
				break;
			default:
				break;
			}
			alert.setPositiveButton(
					 "닫기", new DialogInterface.OnClickListener() {
					    public void onClick( DialogInterface dialog, int which) {
					        dialog.dismiss();   //닫기
					    }
					});
			alert.show();
		}
	};
    
    // check network        
    public boolean checkNetwork() 
    {
        boolean result = true;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }
    
    // updates the date we display in the TextView
    private void updateDisplay() {
    	Button recordDate = (Button)findViewById(R.id.EditTextRecordDate);
    	recordDate.setText(
            new StringBuilder()
                    // Month is 0 based so add 1
            		.append(mYear).append("년 ")
                    .append(mMonth + 1).append("월 ")
                    .append(mDay).append("일")
                    );
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }
            };
}