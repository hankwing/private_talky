package com.domen.tools;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.ping.PingManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.domen.activities.ChatActivity;
import com.domen.openfire.MultipleLoginPresenceListener;
import com.domen.openfire.MultipleLoginPresenceListener.MultiLoginDialog;
import com.domen.start.R;

public class MXMPPConnection {

	private static XMPPTCPConnection mXmppConnection;
	private final static String idAddressString = "112.124.125.228";
	private static PingManager mPingManager = null;
	private static Packet packetReady;
	private static Context context;
	private static SharedPreferences account = null;
	public final static Object loginLock = new Object();		//sync login and operation after logging
	public static boolean isLogin = false;
	// public static boolean isConnection = false;

	public static synchronized XMPPTCPConnection getInstance() {
		if (mXmppConnection == null || !mXmppConnection.isConnected()) {
			if( !Connection() ) {
				//连接失败 返回null
				return null;
			}
			// get PingManager
			mPingManager = PingManager.getInstanceFor(mXmppConnection);
			
		}
		return mXmppConnection;
	}

	/**
	 * 向服务器发送包 发送前检查状态
	 * 
	 * @param c
	 * @param p
	 */
	public static synchronized void sendPacket( Context c, Packet p) {
		context = c;
		packetReady = p;				//如果重新登录 则发送packetReady
		account = context.getSharedPreferences("accoutInfo", Context.MODE_PRIVATE);
		try {
			if (mXmppConnection != null && mXmppConnection.isConnected()) {
				// 连接状态正常
				
				if (mXmppConnection.getUser() == null) {
					// 还没登录 进行登录操作 登录后发送包
					new Login().execute(account.getString("account", null),
							account.getString("password", null),Build.MODEL);
				} else {
					Presence presence = new Presence(Presence.Type.available);
					mXmppConnection.sendPacket(presence);
					
					Roster mRoster = MXMPPConnection.getInstance().getRoster();
					mRoster.reload();
					List<Presence> presences = mRoster.getPresences(account
							.getString("userBareJID", null));
					int i = 0;
					for (Presence mPresence : presences) {
						//Log.i("message", "presenceForm : " + mPresence.getFrom() +" Type: " + mPresence.getType());
						if (mPresence.getType() == Presence.Type.available) {
							i++;
						}

					}
					if( i >= 2){
						Activity currentActivity = CurrentActivity.getCurrentActivity();
						if( currentActivity != null) {
							new MultiLoginDialog().show( currentActivity.getFragmentManager(),
									"multipleLogin");
							return;
						}
						else {
							return;
						}
					}
					
					// 直接发送包
					mXmppConnection.sendPacket(p);
				}
			}
//			else if( mXmppConnection.getUser() != null) {
//				//无连接但是登录了
//				if( !Connection()) {
//					Toast.makeText(context, context.getResources().getString(R.string.internet_failure)
//							, Toast.LENGTH_SHORT).show();
//				}
//			}
			else {
				// 无连接也没登录 下面登录后发送包
				new Login().execute(account.getString("account", null),
						account.getString("password", null), Build.MODEL);
			}
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotLoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static PingManager getPingManager() {
		return mPingManager;
	}

	/**
	 * 连接服务器
	 */
	public static boolean Connection() { 
		try {
			
			ConnectionConfiguration mConnectionConfiguration = new ConnectionConfiguration(
					idAddressString, 5222);
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, null, new SecureRandom());
			mConnectionConfiguration.setSecurityMode(SecurityMode.disabled);
			mConnectionConfiguration.setCustomSSLContext(sc);
			mXmppConnection = new XMPPTCPConnection(mConnectionConfiguration);
			// mXmppConnection2 = new XMPPConnection(mConnectionConfiguration);

			mXmppConnection.connect();
//			mXmppConnection.addPacketListener(new PacketListener() {
//
//				@Override
//				public void processPacket(Packet arg0)
//						throws NotConnectedException {
//					// TODO Auto-generated method stub
//					Log.i("message", "packet: " + arg0.toString());
//					
//				}
//				
//			}, null);
			// mXmppConnection2.connect();
			// ProviderManager.getInstance().addIQProvider("query",
			// "com:talky:formateam", new myIQProvider());
			// Looper.prepare();
			// Toast.makeText(LoginActivity.this, R.string.success_con_of,
			// Toast.LENGTH_SHORT).show();
			// Looper.loop();
			return true;
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SmackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 重新登录
	 * 
	 * @author hankwing
	 * 
	 */
	public static class Login extends AsyncTask<String, Void, Integer> {

		String[] param;
		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub
			param = params;
			if( mXmppConnection == null || !mXmppConnection.isConnected() ) {
				//还未连接
				if ( !Connection()) {
					//网络连接失败
					return 0;
				}
				mPingManager = PingManager.getInstanceFor(mXmppConnection);
			}
			try {
				try {
					mXmppConnection.login(params[0], params[1],params[2]);
					// 保存用户资料到preference
					
				} catch (SaslException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0; // 登录失败
				} catch (SmackException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0; // 登录失败
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0; // 登录失败
				}
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0; // 登录失败
			}
			// mXmppConnection2.login("wengjia999", "123456");
			if( params.length < 4) {
				return 1;
			}
			else {
				//join room after logging in
				return 2;
			}

		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			if( result == 1) {
				try {
					//登录成功 发送包
					account = context.getSharedPreferences("accoutInfo", Context.MODE_PRIVATE);
					account.edit().putString("userFullId", mXmppConnection.getUser()).commit();
					mXmppConnection.sendPacket(packetReady);
					
					//添加roaster监听器
					Roster mRoster = mXmppConnection.getRoster();
					mRoster.addRosterListener(new MultipleLoginPresenceListener());
					
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if( result == 2) {
				//join room

				ChatActivity.chat = new MultiUserChat(mXmppConnection, param[3]);
				try {
					ChatActivity.chat.join(mXmppConnection.getUser());
				} catch (NoResponseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XMPPErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if( result == 0) {
				//网络错误
				Toast.makeText(context, context.getResources().
						getString(R.string.internet_wake), Toast.LENGTH_SHORT).show();
			}
			synchronized (loginLock) {
		        // Wait while logging
				isLogin = true;
				loginLock.notifyAll(); // Wake any waiting threads
		    }
			
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

	}
	
	public static void destroyConnection() {
		mXmppConnection = null;
	}

}
