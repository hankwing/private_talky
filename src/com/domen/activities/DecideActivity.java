package com.domen.activities;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.domen.openfire.RequestATeam;
import com.domen.start.LoginActivity;
import com.wxl.lettalk.R;

/**
 * 决定选择哪个观点
 * @author hankwing
 *
 */
public class DecideActivity extends Activity implements OnClickListener{

	private Button btn_pos;				//选择正方观点
	private Button btn_neg;				//选择反方观点
	private Button btn_discuss_home;
	private String theme;
	private Bundle fromBundle;
	private String roomJID;
	private Intent intentIT;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_decide);
		MainActivity.providerManager.addIQProvider("success", "com:talky:asRequestATeamIQ", new ResultIQProvider());
		btn_neg = (Button) this.findViewById(R.id.btn_negative);
		btn_neg.setOnClickListener(this);
		btn_pos = (Button) this.findViewById(R.id.btn_positive);
		btn_pos.setOnClickListener(this);
		btn_discuss_home = (Button) this.findViewById(R.id.btn_discuss_home);
		btn_discuss_home.setOnClickListener(this);
		Intent intent = this.getIntent();
		fromBundle = intent.getExtras();
		//获得该话题所属的类别
		theme = fromBundle.getString("theme");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		intentIT = new Intent(getApplicationContext(), ChatActivity.class);
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.btn_negative:
			bundle.putString("side", "negative");
			bundle.putString("theme", theme);
			intentIT.putExtras(bundle);
			//请求组队
			RequestATeam ngRA = new RequestATeam( LoginActivity.mXmppConnection.getUser(), fromBundle.getString("topicID")
					, "negative");
			ngRA.setType(IQ.Type.GET);
			LoginActivity.mXmppConnection.sendPacket(ngRA);
			Toast.makeText(this, "正在进行匹配", Toast.LENGTH_SHORT).show();
			break;
		case R.id.btn_positive:
			bundle.putString("side", "positive");
			bundle.putString("theme", theme);
			intentIT.putExtras(bundle);
			//请求组队
			RequestATeam ptRA = new RequestATeam( LoginActivity.mXmppConnection2.getUser(), fromBundle.getString("topicID")
					, "positive");
			ptRA.setType(IQ.Type.GET);
			LoginActivity.mXmppConnection2.sendPacket(ptRA);
			Toast.makeText(this, "正在进行匹配", Toast.LENGTH_SHORT).show();		
			break;
		case R.id.btn_discuss_home:
			Intent groupIT = new Intent(getApplicationContext(), GroupActivity.class);
			startActivity(groupIT);
			break;
		default:
			break;
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//ProviderManager.getInstance().removeIQProvider("success", "com:talky:asRequestATeamIQ");
		super.onDestroy();
	}

	/**
	 * 响应服务器发来的房间ID 加入该房间
	 * @author hankwing
	 *
	 */
	public class ResultIQProvider implements IQProvider {

		@Override
		public IQ parseIQ(XmlPullParser arg0) throws Exception {
			// TODO Auto-generated method stub
			boolean done = false;
			boolean isJoin = false;
			int eventType = arg0.getEventType();
			Log.i("talky", "receive an reply formATeam");
	        while (!done) {
	         if(eventType == XmlPullParser.TEXT) {
	             //获得roomJID
	        	 //parser.getText();
	        	 done = true;
	             roomJID = arg0.getText();
	             Log.i("message", "roomJId: " + roomJID);
	         } 
	         eventType = arg0.next();
	        }
	        if( !isJoin) {
	        	isJoin = true;
	        	DecideActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						ChatActivity.chat = new MultiUserChat(LoginActivity.mXmppConnection, roomJID);
						ChatActivity.chat2 = new MultiUserChat(LoginActivity.mXmppConnection2, roomJID);
						try {
							ChatActivity.chat.join(LoginActivity.mXmppConnection.getUser());
							ChatActivity.chat2.join(LoginActivity.mXmppConnection2.getUser());
							//匹配成功 进入chatActitity
							Toast.makeText(DecideActivity.this, "匹配成功", Toast.LENGTH_SHORT).show();
							DecideActivity.this.startActivity(intentIT);
							DecideActivity.this.finish();
						} catch (XMPPException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					} 
            	 });
	        }	
			return null;
		}
		
	}

}
