package com.domen.start;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.domen.activities.MainActivity;
import com.domen.customView.ProgressDialogWithKeyBack;
import com.domen.entity.InternetStatu;
import com.domen.entity.UserInfo;
import com.wxl.lettalk.R;

/**
 * 登录界面
 * @author hankwing
 *
 */
public class LoginActivity extends Activity implements OnClickListener{

	private EditText edt_username;
	private EditText edt_password;
	private Button btn_login;			//登录按钮
	private Button btn_regist;			//注册按钮
	//private String username;			//用户名
	//private String password;			//密码
	private String idAddressString;
	public static XMPPConnection mXmppConnection = null;
	//public static XMPPConnection mXmppConnection2 = null;
	private register registerThread = null;
	private Login loginThread = null;
	private ProgressDialog registerDialog = null;
	private ProgressDialog loginDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		SharedPreferences settings = getSharedPreferences("runTime", 0);
		if(settings.getInt("time", 0)==0) {
			Intent intent = new Intent( this, GuideActivity.class);
			startActivity(intent);
			settings.edit().putInt("time", 1).commit();
			finish();
			overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
		}
		else {
			idAddressString = "180.110.249.174";
			btn_login = (Button) this.findViewById(R.id.loginBtn);
			btn_regist = (Button) this.findViewById(R.id.registBtn);
			edt_password = (EditText) this.findViewById(R.id.passwordEt);
			edt_username = (EditText) this.findViewById(R.id.userNameEt);
			btn_login.setOnClickListener(this);
			btn_regist.setOnClickListener(this);
			//判断网络状况
			InternetStatu.isConnected = isOnline();
			InternetStatu.isWifi = isWifi();
		}
		
	}
	
	/**
	 * 连接XMPP服务器
	 */
	private void XMPPconnection() {
		ConnectionConfiguration mConnectionConfiguration = 
				new ConnectionConfiguration(idAddressString, 5222);
		mConnectionConfiguration.setReconnectionAllowed(false);      
		mConnectionConfiguration.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);       
		mConnectionConfiguration.setSASLAuthenticationEnabled(true);      
		mConnectionConfiguration .setTruststorePath("/system/etc/security/cacerts.bks");       
		mConnectionConfiguration.setTruststorePassword("changeit");       
		mConnectionConfiguration.setTruststoreType("bks");    
		mXmppConnection = new XMPPConnection(mConnectionConfiguration);
		//mXmppConnection2 = new XMPPConnection(mConnectionConfiguration);
		try
		{
			mXmppConnection.connect();
			//mXmppConnection2.connect();
			//ProviderManager.getInstance().addIQProvider("query", "com:talky:formateam", new myIQProvider());
//			Looper.prepare();
//			Toast.makeText(LoginActivity.this, R.string.success_con_of, Toast.LENGTH_SHORT).show();
//			Looper.loop();
		} catch (XMPPException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			if( mXmppConnection == null ) {
				XMPPconnection();				//连接服务器
			}
			if(mXmppConnection.isConnected()) {
				try {
					mXmppConnection.login(params[0], params[1]);
					configure(ProviderManager.getInstance());
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
	
	/**
	 * 注册线程
	 * @author hankwing
	 *
	 */
	private class register extends AsyncTask<String, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			if( mXmppConnection == null ) {
				XMPPconnection();				//连接服务器
			}
			if(mXmppConnection.isConnected()) {
				try {
					AccountManager accountManager = mXmppConnection.getAccountManager();
					accountManager.createAccount(params[0],params[1]);
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
			registerDialog.dismiss();				//进度条消失
			if(result.booleanValue()) {
				//注册成功
				new Login().execute( edt_username.getText().toString(), edt_password.getText().toString());			
			}
			else {
				Toast.makeText(LoginActivity.this, R.string.replicated_username, Toast.LENGTH_SHORT).show();
			}
			
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			CharSequence message = LoginActivity.this.getResources().getString(R.string.registering);
			registerDialog = new ProgressDialogWithKeyBack(LoginActivity.this, this);
			registerDialog.setMessage(message);
			registerDialog.show();
			super.onPreExecute();
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch( v.getId() ) {
		case R.id.loginBtn:
			if( InternetStatu.isConnected ) {
				loginThread = new Login();
				loginThread.execute( edt_username.getText().toString(), edt_password.getText().toString());
			}
			else {
				Toast.makeText(this, R.string.internet_failure, Toast.LENGTH_SHORT).show();
			}
//			Intent mainActivity = new Intent( LoginActivity.this, MainActivity.class);
//			startActivity(mainActivity);
//			LoginActivity.this.finish();
			break;
		case R.id.registBtn:
			if( InternetStatu.isConnected ) {
				registerThread = new register();		
				registerThread.execute(edt_username.getText().toString(), edt_password.getText().toString());
			}
			else {
				Toast.makeText(this, R.string.internet_failure, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		
	}
	
	private void configure(ProviderManager pm) {
		pm.addExtensionProvider("x", "jabber:x:conference", 
				new GroupChatInvitation.Provider());
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", 
				new MUCUserProvider());
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", 
				new MUCAdminProvider());
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", 
				new MUCOwnerProvider());
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

}
