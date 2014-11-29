package com.domen.start;

import com.domen.activities.RegisterActivity;
import com.wxl.lettalk.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * 初始化界面 可选择登录和注册
 * @author hankwing
 *
 */
public class PreLoginActivity extends Activity implements OnClickListener{

	private TextView tvLogin;
	private TextView tvRegister;
	private final int waitForResult = 0;
	private final int waitForRegister = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_pre_login);
		
		tvLogin = (TextView) findViewById(R.id.btn_login);
		tvLogin.setOnClickListener(this);
		tvRegister = (TextView) findViewById(R.id.btn_register);
		tvRegister.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch( v.getId() ) {
		case R.id.btn_login:
			//enter login activity
			Intent it = new Intent(this, LoginActivity.class);
			startActivityForResult(it, waitForResult);
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
			break;
		case R.id.btn_register:
			Intent registerIntent = new Intent(this, RegisterActivity.class);
			startActivityForResult(registerIntent, waitForRegister);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == waitForResult && resultCode == RESULT_OK) {
			// 登录成功与否 成功则关闭当前activity
			finish();
		}
		else if( requestCode == waitForRegister && resultCode == RESULT_OK) {
			//if register succeed
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
		
	}
	
}
