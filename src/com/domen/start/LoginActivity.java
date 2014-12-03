package com.domen.start;

import java.io.IOException;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
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
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.domen.activities.MainActivity;
import com.domen.customView.ProgressDialogWithKeyBack;
import com.domen.tools.MXMPPConnection;
import com.domen.start.R;

/**
 * 登录界面
 * 
 * @author hankwing
 * 
 */
public class LoginActivity extends Activity implements OnClickListener {

	private static final int WaitForRegister = 0;
	private EditText edt_username;
	private EditText edt_password;
	private ImageView btn_login; // 登录按钮
	public XMPPTCPConnection mXmppConnection = null;
	// public static XMPPConnection mXmppConnection2 = null;
	private ProgressDialog loginDialog = null;
	
	private SharedPreferences account = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		Context context = getApplicationContext();
		account = getSharedPreferences("accoutInfo", Context.MODE_PRIVATE); // 保存用户资料的preference
		SmackAndroid.init(context); // 初始化asmack
//		SharedPreferences settings = getSharedPreferences("runTime", 0);
//		if (settings.getInt("time", 0) == 0) {
//			Intent intent = new Intent(this, GuideActivity.class);
//			startActivity(intent);
//			settings.edit().putInt("time", 1).commit();
//			finish();
//			overridePendingTransition(android.R.anim.fade_in,
//					android.R.anim.fade_out);
//		} else {
			btn_login = (ImageView) this.findViewById(R.id.loginBtn);
			edt_password = (EditText) this.findViewById(R.id.passwordEt);
			edt_username = (EditText) this.findViewById(R.id.userNameEt);
			btn_login.setOnClickListener(this);
//		}

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
	 * 
	 * @author hankwing
	 * 
	 */
	private class Login extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub
			mXmppConnection = MXMPPConnection.getInstance();
			if (mXmppConnection == null) {
				// 无连接 下面尝试连接
				if (MXMPPConnection.Connection()) {
					// 连接成功
					mXmppConnection = MXMPPConnection.getInstance();
				} else {
					// 连接失败
					return 0;
				}
			}
			try {
				mXmppConnection.login(params[0], params[1], params[2]);
				// 保存用户资料到preference
				String userFullJID = mXmppConnection.getUser();
				String userBareJID = userFullJID.substring(0,
						userFullJID.indexOf("/"));
				account.edit().putString("account", params[0]).commit();
				account.edit().putString("password", params[1]).commit();
				account.edit()
						.putString("userFullId", mXmppConnection.getUser())
						.commit();
				account.edit().putString("userBareJID", userBareJID).commit();
			} catch (SaslException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1; // 登录失败
			} catch (SmackException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1; // 登录失败
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0; // 登录失败 网络问题
			}
			// mXmppConnection2.login("wengjia999", "123456");
			catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}

			return 1; // 登录成功
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			loginDialog.dismiss(); // 进度条消失

			if (result == 1) {
				// 登录成功 发送presence
				Intent mainActivity = new Intent(LoginActivity.this,
						MainActivity.class);
				startActivity(mainActivity);
				setResult(RESULT_OK); // login succeed close preLogin
				LoginActivity.this.finish();
			} else if (result == -1) {
				Toast.makeText(LoginActivity.this,
						R.string.uername_or_password_failure,
						Toast.LENGTH_SHORT).show();
				new DisConnection().execute(mXmppConnection);
				
			} else {
				Toast.makeText(LoginActivity.this, R.string.internet_failure,
						Toast.LENGTH_SHORT).show();
				new DisConnection().execute(mXmppConnection);
			}

			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			CharSequence message = LoginActivity.this.getResources().getString(
					R.string.logining);
			loginDialog = new ProgressDialogWithKeyBack(LoginActivity.this,
					this);
			loginDialog.setMessage(message);
			loginDialog.show();
			super.onPreExecute();
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.loginBtn:

			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(
							getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
			String account = edt_username.getText().toString();
			String password = edt_password.getText().toString();
			new Login().execute(account, password, Build.MODEL);

			break;
		}

	}

	/**
	 * 判断是否有网络连接
	 * 
	 * @return
	 */
	public boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	public boolean isWifi() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return (networkInfo != null && networkInfo.isConnected());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == WaitForRegister && resultCode == RESULT_OK) {
			// 注册成功与否 成功则关闭当前activity
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * disconnect after logging failed
	 * 
	 * @param mXmppConnection
	 */
	public static class DisConnection extends AsyncTask<XMPPTCPConnection, Void, Void> {

		@Override
		protected Void doInBackground(XMPPTCPConnection... params) {
			// TODO Auto-generated method stub
			if (params[0] != null && params[0].isConnected()) {

				try {
					params[0].disconnect();
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return null;
		}

	}

}
