package beonit.NaverMoneySync;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AccountSetting extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setting);
        SharedPreferences prefs = getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
		EditText editTextNaverId = (EditText)findViewById(R.id.EditTextAccount);
        EditText editTextNaverPasswd = (EditText)findViewById(R.id.EditTextPasswd);
        editTextNaverId.setText(prefs.getString("naverID", ""));
        editTextNaverPasswd.setText(prefs.getString("naverPasswd", ""));

	}

	public void onSubmitAccount(View view){
        EditText editTextNaverId = (EditText)findViewById(R.id.EditTextAccount);
        EditText editTextNaverPasswd = (EditText)findViewById(R.id.EditTextPasswd);

        SharedPreferences prefs = getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("naverID", editTextNaverId.getText().toString());
		editor.putString("naverPasswd", editTextNaverPasswd.getText().toString());
		editor.commit();
		
		// TODO. 로그인 체크
		
		this.finish();
    }
    

}
