package beonit.NaverMoneySync;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
        String encryptPasswd = prefs.getString("naverPasswd", "");
        if( encryptPasswd == null || encryptPasswd.length() == 0 )
        	return;
        try {
			editTextNaverPasswd.setText( SimpleCrypto.decrypt("SECGAL", encryptPasswd));
		} catch (Exception e) {
			e.printStackTrace();
			this.finish();
		}
	}

	public void onSubmitAccount(View view){
        EditText editTextNaverId = (EditText)findViewById(R.id.EditTextAccount);
        EditText editTextNaverPasswd = (EditText)findViewById(R.id.EditTextPasswd);

        SharedPreferences prefs = getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("naverID", editTextNaverId.getText().toString());
		try {
			editor.putString("naverPasswd", SimpleCrypto.encrypt("SECGAL", editTextNaverPasswd.getText().toString()) );
		} catch (Exception e) {
			Log.e("beonit", "encrypt fail");
			e.printStackTrace();
		}
		editor.commit();
		this.setResult(RESULT_OK);
		this.finish();
    }
    

}
