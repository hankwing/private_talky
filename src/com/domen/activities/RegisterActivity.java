package com.domen.activities;

import java.io.IOException;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.packet.GroupChatInvitation;
import org.jivesoftware.smackx.muc.provider.MUCAdminProvider;
import org.jivesoftware.smackx.muc.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.muc.provider.MUCUserProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.provider.DataFormProvider;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.domen.customView.ProgressDialogWithKeyBack;
import com.domen.entity.ConstantVariable;
import com.domen.openfire.RequestSaveUserInfo;
import com.domen.tools.MXMPPConnection;
import com.domen.start.R;

public class RegisterActivity extends Activity implements OnClickListener {

	private ImageView registerButton;

	public XMPPTCPConnection mXmppConnection = null;

	private EditText account_name;
	private EditText password;
	private SharedPreferences account; // 存账号数据到用户偏号中
	private EditText retypePassword;
	private ProgressDialog registerDialog = null; // 注册时显示的进度条
	private ProgressDialog loginDialog = null;
	private Token mToken = new Token();

	private class Token {
		private boolean flag;

		public Token() {
			setFlag(false);
		}

		public void setFlag(boolean flag) {
			this.flag = flag;
		}

		public boolean getFlag() {
			return flag;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register);
		account = getSharedPreferences("accoutInfo", Context.MODE_PRIVATE); // 记录用户账号信息的preference

		registerButton = (ImageView) findViewById(R.id.register);
		registerButton.setOnClickListener(this);
		account_name = (EditText) findViewById(R.id.register_account);
		password = (EditText) findViewById(R.id.register_password);
		retypePassword = (EditText) findViewById(R.id.register_retype_password);

	}

	/**
	 * 注册线程
	 * 
	 * @author hankwing
	 * 
	 */
	private class register extends AsyncTask<String, Void, Integer> {

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
				AccountManager accountManager = AccountManager
						.getInstance(mXmppConnection);
				accountManager.createAccount(params[0], params[1]);

			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1; // 登录失败
			}
			// mXmppConnection2.login("wengjia999", "123456");
			catch (NoResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}

			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			registerDialog.dismiss(); // 进度条消失
			if (result == 1) {
				// 注册成功 进行登录
				new Login().execute(account_name.getText().toString(), password
						.getText().toString(), Build.MODEL);
			} else if (result == -1) {
				Toast.makeText(RegisterActivity.this,
						R.string.replicated_username, Toast.LENGTH_SHORT)
						.show();
				new DisConnection().execute(mXmppConnection);
			} else {
				Toast.makeText(RegisterActivity.this,
						R.string.internet_failure, Toast.LENGTH_SHORT).show();
				new DisConnection().execute(mXmppConnection);
			}

			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			CharSequence message = RegisterActivity.this.getResources()
					.getString(R.string.registering);
			registerDialog = new ProgressDialogWithKeyBack(
					RegisterActivity.this, this);
			registerDialog.setMessage(message);
			registerDialog.show();
			super.onPreExecute();
		}

	}

	/**
	 * 登录线程 同时保存用户资料到服务器
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
				//enable delivery manager
				DeliveryReceiptManager.getInstanceFor(mXmppConnection).enableAutoReceipts();
				configure();
				// 添加自己为好友
				Roster mRoster = mXmppConnection.getRoster();
				String userJID = mXmppConnection.getUser();
				String userBareJID = userJID.substring(0, userJID.indexOf("/"));
				mRoster.createEntry(userBareJID, null,
						new String[] { "friends" });

				// 保存用户资料 包括默认头像url
				VCard mVCard = new VCard();
				mVCard.setNickName(params[0]); // 设置用户昵称
				mVCard.setField(ConstantVariable.Rank, "1"); // 设置用户等级
				mVCard.save(mXmppConnection);

				RequestSaveUserInfo rs = new RequestSaveUserInfo(params[0],
						mXmppConnection.getUser(), 1, "牙牙学语", 0, 0);
				rs.setType(IQ.Type.GET);
				try {
					mXmppConnection.sendPacket(rs);
					// 等待IQHandler返回结果后才表示注册成功
					synchronized (mToken) {
						while (!mToken.getFlag()) {
							mToken.wait();
						}
					}
					// 保存用户资料到preference
					account.edit().putString("account", params[0]).commit();
					account.edit().putString("password", params[1]).commit();
					account.edit()
							.putString("userFullId", mXmppConnection.getUser())
							.commit();
					account.edit().putString("userBareJID", userBareJID)
							.commit();
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0; // 登录失败
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0;
				}

			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1; // 登录失败
			}
			// mXmppConnection2.login("wengjia999", "123456");
			catch (SaslException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			} catch (SmackException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}

			return 1;

		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			loginDialog.dismiss(); // 进度条消失
			if (result == 1) {
				// 登录成功
				Intent mainActivity = new Intent(RegisterActivity.this,
						MainActivity.class);
				// UserInfo.fullUserJID = mXmppConnection.getUser(); //
				// 得到登录用户的FullJID
				startActivity(mainActivity);
				setResult(RESULT_OK);
				RegisterActivity.this.finish();
			} else if (result == -1) {
				Toast.makeText(RegisterActivity.this,
						R.string.uername_or_password_failure,
						Toast.LENGTH_SHORT).show();
				new DisConnection().execute(mXmppConnection);
			} else {
				Toast.makeText(RegisterActivity.this,
						R.string.internet_failure, Toast.LENGTH_SHORT).show();
				new DisConnection().execute(mXmppConnection);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			CharSequence message = RegisterActivity.this.getResources()
					.getString(R.string.logining);
			loginDialog = new ProgressDialogWithKeyBack(RegisterActivity.this,
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
		case R.id.register:
			if (!password.getText().toString()
					.equals(retypePassword.getText().toString())) {
				Toast.makeText(this,
						getResources().getString(R.string.inconsistent),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (account_name.getText().toString().length() > 16
					|| password.getText().toString().length() > 16
					|| password.getText().toString().length() < 6) {
				Toast.makeText(this,
						getResources().getString(R.string.password_tip),
						Toast.LENGTH_SHORT).show();
				return;
			}
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(this.getCurrentFocus()
							.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
			// 发送注册IQ包
			new register().execute(account_name.getText().toString(), password
					.getText().toString());
			break;
		default:
			break;
		}
	}

	private void configure() {
		ProviderManager.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());
		ProviderManager.addExtensionProvider("x", "jabber:x:data",
				new DataFormProvider());
		ProviderManager.addExtensionProvider("x",
				"http://jabber.org/protocol/muc#user", new MUCUserProvider());
		ProviderManager.addIQProvider("query",
				"http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
		ProviderManager.addIQProvider("query",
				"http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
		// 添加监听用户资料同步结果IQ的IQProvider
		ProviderManager.addIQProvider("success", "com:talky:saveUserInfo",
				new UserInfoResultIQProvider());

	}

	/**
	 * 内部类 定义了服务器响应客户端同步用户资料IQ的Provider
	 * 
	 * @author hankwing
	 * 
	 */
	public class UserInfoResultIQProvider implements IQProvider {

		// private static final String PREFERRED_ENCODING = "UTF-8";
		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
			// TODO Auto-generated method stub
			// Log.i("message", "receive an IQ");
			synchronized (mToken) {
				mToken.setFlag(true);
				mToken.notify();
			}
			return null;
		}
	}

	/**
	 * disconnect after logging failed
	 * 
	 * @param mXmppConnection
	 */
	public class DisConnection extends AsyncTask<XMPPTCPConnection, Void, Void> {

		@Override
		protected Void doInBackground(XMPPTCPConnection... params) {
			// TODO Auto-generated method stub
			if (mXmppConnection != null && mXmppConnection.isConnected()) {

				try {
					mXmppConnection.disconnect();
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return null;
		}

	}

}
