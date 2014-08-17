package com.domen.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.wxl.lettalk.R;

/**
 * 决定选择哪个观点
 * @author hankwing
 *
 */
public class DecideActivity extends Activity implements OnClickListener{

	Button btn_pos;
	Button btn_neg;
	Button btn_discuss_home;
	String theme;
	Intent intent=null;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_decide);
		btn_neg = (Button) this.findViewById(R.id.btn_negative);
		btn_neg.setOnClickListener(this);
		btn_pos = (Button) this.findViewById(R.id.btn_positive);
		btn_pos.setOnClickListener(this);
		btn_discuss_home = (Button) this.findViewById(R.id.btn_discuss_home);
		btn_discuss_home.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(), GroupActivity.class);
				startActivity(intent);
			}
		});
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		//获得该话题所属的类别
		theme = bundle.getString("theme");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		intent = new Intent(getApplicationContext(), ChatActivity.class);
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.btn_negative:
			bundle.putString("side", "negative");
			break;
		case R.id.btn_positive:
			bundle.putString("side", "positive");

		default:
			break;
		}
		bundle.putString("theme", theme);
		intent.putExtras(bundle);
		startActivity(intent);
		finish();

	}

}
