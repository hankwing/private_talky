package com.domen.start;

import java.io.IOException;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.domen.activities.MainActivity;
import com.domen.activities.RegisterActivity;
import com.domen.customView.ProgressDialogWithKeyBack;
import com.domen.entity.InternetStatu;
import com.domen.entity.UserInfo;
import com.domen.tools.MXMPPConnection;
import com.domen.tools.ResizeLayout;
import com.wxl.lettalk.R;

/**
 * 登录界面
 * @author hankwing
 *
 */
public class LoginActivity extends Activity implements OnClickListener{

	private static final int BIGGER = 1;
	private static final int SMALLER = 2;
	private static final int MSG_RESIZE = 3;
	private static final int WaitForRegister = 0;
	private EditText edt_username;
	private EditText edt_password;
	private Button btn_login;			//登录按钮
	private Button btn_regist;			//注册按钮
	//private String username;			//用户名
	//private String password;			//密码
	public XMPPTCPConnection mXmppConnection = null;
	//public static XMPPConnection mXmppConnection2 = null;
	
	private Login loginThread = null;
	private ProgressDialog loginDialog = null;
	
	private ResizeLayout mainLayout;
	private static RelativeLayout logoLayout;				//logo界面
	private static TextView theThirdTextView;				//第三方登录按钮
	private static LinearLayout theThirdLinearLayout;
	private ImageView sina;
	private ImageView qq;
	private ImageView qqWeibo;

	/**
	 * 控制输入法弹出时某些视图的可见性
	 */
	private static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what)
			{
			case MSG_RESIZE:
				if (msg.arg1 == BIGGER)
				{
					theThirdLinearLayout.setVisibility(View.VISIBLE);
					theThirdTextView.setVisibility(View.VISIBLE);
					logoLayout.setVisibility(View.VISIBLE);
				} else
				{
					theThirdLinearLayout.setVisibility(View.GONE);
					theThirdTextView.setVisibility(View.GONE);
					logoLayout.setVisibility(View.GONE);
				}
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);	
		Context context = getApplicationContext();
		SmackAndroid.init(context);				//初始化asmack
		SharedPreferences settings = getSharedPreferences("runTime", 0);
		if(settings.getInt("time", 0)==0) {
			Intent intent = new Intent( this, GuideActivity.class);
			startActivity(intent);
			settings.edit().putInt("time", 1).commit();
			finish();
			overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
		}
		else {
			btn_login = (Button) this.findViewById(R.id.loginBtn);
			btn_regist = (Button) this.findViewById(R.id.registBtn);
			edt_password = (EditText) this.findViewById(R.id.passwordEt);
			edt_username = (EditText) this.findViewById(R.id.userNameEt);
			theThirdTextView = (TextView) findViewById(R.id.other_account_login);
			theThirdLinearLayout = (LinearLayout) findViewById(R.id.other_image);
			logoLayout = (RelativeLayout) findViewById(R.id.login_logo);
			sina = (ImageView) findViewById(R.id.sina_login);
			qq = (ImageView) findViewById(R.id.qq_cqq_login);
			qqWeibo = (ImageView) findViewById(R.id.qq_weibo_login);
			mainLayout = (ResizeLayout) findViewById(R.id.common_login_fields);
			btn_login.setOnClickListener(this);
			btn_regist.setOnClickListener(this); 
			sina.setOnClickListener(this);
			qq.setOnClickListener(this);
			qqWeibo.setOnClickListener(this);
			
			mainLayout.setOnResizeListener(new ResizeLayout.OnResizeListener() {

				@Override
				public void OnResize(int w, int h, int oldw, int oldh) {
					// TODO Auto-generated method stub
					int change = BIGGER;
					if (h < oldh)
					{
						change = SMALLER;
					}
					Message msg = new Message();
					msg.what = MSG_RESIZE;
					msg.arg1 = change;
					handler.sendMessage(msg);
				}
			});
			
			//判断网络状况
			InternetStatu.isConnected = isOnline();
			InternetStatu.isWifi = isWifi();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	/**
	 * 登录线程
	 * @author hankwing
	 *
	 */
	private class Login extends AsyncTask<String, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			if( mXmppConnection == null || !mXmppConnection.isConnected()) {
				mXmppConnection = MXMPPConnection.getInstance();
			}
			if(mXmppConnection.isConnected()) {
				try {
					try {
						mXmppConnection.login(params[0], params[1]);
					} catch (SaslException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SmackException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return Boolean.valueOf(false);			//登录失败
				}
				//mXmppConnection2.login("wengjia999", "123456");
				
				return Boolean.valueOf(true);
			}
			else {
				return Boolean.valueOf(false);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			loginDialog.dismiss();				//进度条消失
			if(result.booleanValue()) {
				//登录成功
				Intent mainActivity = new Intent( LoginActivity.this, MainActivity.class);
				UserInfo.fullUserJID = mXmppConnection.getUser();				//得到登录用户的FullJID
				startActivity(mainActivity);
				LoginActivity.this.finish();
			}
			else {
				Toast.makeText(LoginActivity.this, R.string.uername_or_password_failure, Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			CharSequence message = LoginActivity.this.getResources().getString(R.string.logining);
			loginDialog = new ProgressDialogWithKeyBack(LoginActivity.this,this);
			loginDialog.setMessage(message);
			loginDialog.show();
			super.onPreExecute();
		}
		
	}
	
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch( v.getId() ) {
		case R.id.loginBtn:
			if( InternetStatu.isConnected ) {
				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
				loginThread = new Login();
				String account = edt_username.getText().toString();
				String password = edt_password.getText().toString();
				loginThread.execute( account, password);
			}
			else {
				Toast.makeText(this, R.string.internet_failure, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.registBtn:
			if( InternetStatu.isConnected ) {
				//进入注册界面
				Intent registerIntent = new Intent(this, RegisterActivity.class);
				startActivityForResult(registerIntent, WaitForRegister);
			}
			else {
				Toast.makeText(this, R.string.internet_failure, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.qq_cqq_login:
			//新功能待添加
			Toast.makeText(this, 
					getResources().getString(R.string.wait_for_update), Toast.LENGTH_SHORT).show();
			break;
		case R.id.qq_weibo_login:
			Toast.makeText(this, 
					getResources().getString(R.string.wait_for_update), Toast.LENGTH_SHORT).show();
			break;
		case R.id.sina_login:
			Toast.makeText(this, 
					getResources().getString(R.string.wait_for_update), Toast.LENGTH_SHORT).show();
			break;
		}
		
	}
	
	/**
	 * 判断是否有网络连接
	 * @return
	 */
	public boolean isOnline() {
	    ConnectivityManager connMgr = (ConnectivityManager) 
	            getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    return (networkInfo != null && networkInfo.isConnected());
	} 
	
	public boolean isWifi() {
	    ConnectivityManager connMgr = (ConnectivityManager) 
	            getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    return (networkInfo != null && networkInfo.isConnected());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == WaitForRegister && resultCode == RESULT_OK) {
			//注册成功与否 成功则关闭当前activity
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	

}
