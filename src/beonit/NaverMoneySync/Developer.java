package beonit.NaverMoneySync;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class Developer extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.developer);
		
		TextView tx = (TextView)findViewById(R.id.beonit);
		tx.setText(Html.fromHtml("<a href=\"http://m.facebook.com/#!/profile.php?refid=7\">beonit</a>"));
		tx.setMovementMethod(LinkMovementMethod.getInstance());
		
		tx = (TextView)findViewById(R.id.beonitEmail);
		tx.setText(Html.fromHtml("<a href=\"mailto:beonit@gmail.com\">beonit@gmail.com</a>"));
		tx.setMovementMethod(LinkMovementMethod.getInstance());
		
		tx = (TextView)findViewById(R.id.beonitBlog);
		tx.setText(Html.fromHtml("<a href=\"http://beonit2.tistory.com\">http://boenit2.tistory.com</a>"));
		tx.setMovementMethod(LinkMovementMethod.getInstance());
		
		tx = (TextView)findViewById(R.id.beonitGithub);
		tx.setText(Html.fromHtml("<a href=\"https://github.com/beonit/NaverMoneySync\">github/NaverMoneySync</a>"));
		tx.setMovementMethod(LinkMovementMethod.getInstance());
		
	}
}
