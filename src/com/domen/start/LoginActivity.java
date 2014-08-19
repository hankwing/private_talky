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

import com.domen.activities.MainActivity;
import com.wxl.lettalk.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
	private String username;			//用户名
	private String password;			//密码
	private String idAddressString;
	public static XMPPConnection mXmppConnection = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		idAddressString = "121.229.27.13";
		btn_login = (Button) this.findViewById(R.id.loginBtn);
		btn_regist = (Button) this.findViewById(R.id.registBtn);
		edt_password = (EditText) this.findViewById(R.id.passwordEt);
		edt_username = (EditText) this.findViewById(R.id.userNameEt);

		btn_login.setOnClickListener(this);
		btn_regist.setOnClickListener(this);
		
		XMPPCon conn = new XMPPCon();
		conn.start();				//连接openfire服务器
		//添加监听同步IQ包的IQProvider
		
	}
	
	/**
	 * Connect to of server
	 * @author hankwing
	 *
	 */
	private class XMPPCon extends Thread {
		public void run() {			
			ConnectionConfiguration mConnectionConfiguration = 
					new ConnectionConfiguration(idAddressString, 5222);
			mConnectionConfiguration.setReconnectionAllowed(true);      
			mConnectionConfiguration.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);       
			mConnectionConfiguration.setSASLAuthenticationEnabled(true);      
			mConnectionConfiguration .setTruststorePath("/system/etc/security/cacerts.bks");       
			mConnectionConfiguration.setTruststorePassword("changeit");       
			mConnectionConfiguration.setTruststoreType("bks");    
			mXmppConnection = new XMPPConnection(mConnectionConfiguration);
			try
			{
				mXmppConnection.connect();
				//ProviderManager.getInstance().addIQProvider("query", "com:talky:formateam", new myIQProvider());
				Looper.prepare();
				Toast.makeText(LoginActivity.this, R.string.success_con_of, Toast.LENGTH_SHORT).show();
				Looper.loop();
			} catch (XMPPException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * login with the content of "account" textview.
	 * @author hankwing
	 *
	 */
	private class XMPPLogIn extends Thread {
			public void run() {			
			try
			{
				//Log.i("Login", accountText.getText().toString());
				if(mXmppConnection.isConnected()) {
					mXmppConnection.login(edt_username.getText().toString(), edt_password.getText().toString());
					configure(ProviderManager.getInstance());
					Looper.prepare();
					Toast.makeText(LoginActivity.this, R.string.success_login, Toast.LENGTH_SHORT).show();
					Intent mainActivity = new Intent( LoginActivity.this, MainActivity.class);
					startActivity(mainActivity);
					LoginActivity.this.finish();
					Looper.loop();
				}
				else {
					Looper.prepare();
					Toast.makeText(LoginActivity.this, R.string.internet_failure, Toast.LENGTH_SHORT).show();
					Looper.loop();
				}
				
			} catch (XMPPException e)
			{
				// TODO Auto-generated catch block
				Looper.prepare();
				Toast.makeText(LoginActivity.this, R.string.uername_or_password_failure, Toast.LENGTH_SHORT).show();
				Looper.loop();
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch( v.getId() ) {
		case R.id.loginBtn:
			XMPPLogIn mXMPPlLogIn = new XMPPLogIn();
			mXMPPlLogIn.start();
			break;
		case R.id.registBtn:
			username = edt_username.getText().toString();
			password = edt_password.getText().toString();
			try {
				AccountManager accountManager = mXmppConnection.getAccountManager();
				accountManager.createAccount(username,password);

				Toast.makeText(LoginActivity.this, R.string.success_rigester, Toast.LENGTH_SHORT).show();

			} catch (XMPPException e) {
				// TODO: handle exception
				Toast.makeText(LoginActivity.this, R.string.replicated_username, Toast.LENGTH_SHORT).show();
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

}
