package com.domen.activities;

import java.io.IOException;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.domen.entity.UserInfo;
import com.domen.openfire.RequestATeam;
import com.domen.openfire.RequestTopicInfo;
import com.domen.tools.BitmapSingleton;
import com.domen.tools.MXMPPConnection;
import com.domen.tools.TopicsContract.TopicsEntryContract;
import com.wxl.lettalk.R;

/**
 * 决定选择哪个观点
 * 
 * @author hankwing
 * 
 */
public class DecideActivity extends Activity implements OnClickListener {

	private Button btn_pos; // 选择正方观点
	private Button btn_neg; // 选择反方观点
	private Button btn_discuss_home;
	private String topicName;
	private String topicID;
	private String topicURL;
	private Bundle fromBundle;
	private String roomJID;
	private Intent intentIT;
	private LinearLayout svContentView;
	private ProgressBar progressBar;
	private int mShortAnimationDuration; // 动画时间
	private NetworkImageView topicPic; // 话题相关图片
	private ImageLoader mImageLoader;
	private TextView topicDesc;
	private TextView positiveView;
	private TextView negativeView;
	private String topicDescription = null;
	private	String positive = null;
	private String negative = null;
	private XMPPConnection mXmppConnection;
	private ProgressDialog registerDialog;
	private RequestATeam ngRA;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_decide);
		Intent intent = this.getIntent();
		fromBundle = intent.getExtras();
		
		// 获得该话题的名称 
		topicName = fromBundle
				.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME);
		getActionBar().setTitle(topicName); // 显示话题名称
		topicID = fromBundle.getString(TopicsEntryContract.COLUMN_NAME_OF_ID);
		topicURL = fromBundle  
				.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_URL);
		// 添加监听请求组队结果IQ包
		ProviderManager.addIQProvider("success",
				"com:talky:asRequestATeamIQ", new ResultIQProvider());
		// 添加话题详情IQ监听器
		ProviderManager.addIQProvider("requestTopicInfo",
				"com:talky:requestTopicInfo", new ResultTopicInfoProvider());
		// 发送话题详情请求IQ包
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mXmppConnection = MXMPPConnection.getInstance();
				RequestTopicInfo rs = new RequestTopicInfo(topicID);
				rs.setType(IQ.Type.GET);
				try {
					mXmppConnection.sendPacket(rs);
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}).start();
		

		getActionBar().setDisplayHomeAsUpEnabled(true);
		btn_neg = (Button) this.findViewById(R.id.btn_negative);
		btn_neg.setOnClickListener(this);
		btn_pos = (Button) this.findViewById(R.id.btn_positive);
		btn_pos.setOnClickListener(this);
		btn_discuss_home = (Button) this.findViewById(R.id.btn_discuss_home);
		btn_discuss_home.setOnClickListener(this);
		topicDesc = (TextView) findViewById(R.id.topicDes); // 话题描述
		positiveView = (TextView) findViewById(R.id.tv_positive); // 正反观点
		negativeView = (TextView) findViewById(R.id.tv_negative); // 反方观点
		svContentView = (LinearLayout) findViewById(R.id.ll_content_view);
		progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
		svContentView.setVisibility(View.GONE); // 内容界面先隐藏
		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);

		mImageLoader = BitmapSingleton.getInstance(this).getImageLoader();
		topicPic = (NetworkImageView) findViewById(R.id.imgv_dec_image);
		topicPic.setImageUrl(topicURL, mImageLoader);
		registerDialog = new ProgressDialog(this);
		//取消组队逻辑
		registerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, 
				getResources().getString(R.string.cancel_requestateam), 
				(DialogInterface.OnClickListener)null);				//先设置空监听器
		registerDialog.setCanceledOnTouchOutside(false);				//设置该dialog不可取消 包括点击屏幕外和按返回键
		
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
			bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
					topicName);
			intentIT.putExtras(bundle);
			// 请求组队
			ngRA = new RequestATeam(
					mXmppConnection.getUser(), topicID,
					"negative", "1");
			ngRA.setType(IQ.Type.GET);
			try {
				mXmppConnection.sendPacket(ngRA);
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			registerDialog.setMessage(getResources().getString(R.string.seeking));
			registerDialog.show();
			final Button cancelButton = registerDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			cancelButton.setVisibility(View.VISIBLE); 
			cancelButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					registerDialog.setMessage(
							getResources().getString(R.string.cancel_requestateam_dialog));
					cancelButton.setVisibility(View.GONE);
					//发送取消组队IQ包
					ngRA.setIsJoin("0");				//标志为取消组队
					ngRA.setType(IQ.Type.GET);
					try {
						mXmppConnection.sendPacket(ngRA);
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			break;
		case R.id.btn_positive:
			bundle.putString("side", "positive");
			bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
					topicName);
			intentIT.putExtras(bundle);
			// 请求组队
			ngRA = new RequestATeam(
					mXmppConnection.getUser(), topicID,
					"positive", "1");
			ngRA.setType(IQ.Type.GET);
			try {
				mXmppConnection.sendPacket(ngRA);
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			registerDialog.setMessage(getResources().getString(R.string.seeking));
			registerDialog.show();
			final Button cancel_button = registerDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			cancel_button.setVisibility(View.VISIBLE);
			cancel_button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					registerDialog.setMessage(
							getResources().getString(R.string.cancel_requestateam_dialog));
					cancel_button.setVisibility(View.GONE);
					//发送取消组队IQ包
					ngRA.setIsJoin("0");				//标志为取消组队
					ngRA.setType(IQ.Type.GET);
					try {
						mXmppConnection.sendPacket(ngRA);
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			break;
		case R.id.btn_discuss_home:
			Toast.makeText(this, getResources().getString(R.string.wait_for_update), 
					Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// ProviderManager.getInstance().removeIQProvider("success",
		// "com:talky:asRequestATeamIQ");
		super.onDestroy();
	}

	/**
	 * 响应服务器发来的房间ID 加入该房间
	 * 
	 * @author hankwing
	 * 
	 */
	public class ResultIQProvider implements IQProvider {

		@Override
		public IQ parseIQ(XmlPullParser arg0) throws Exception {
			// TODO Auto-generated method stub
			Log.i("message", "receive an success iQ");
			boolean done = false;
			boolean isJoin = false;
			int eventType = arg0.getEventType();
			while (!done) {
				if (eventType == XmlPullParser.START_TAG) {
					if (arg0.getName().equals("success")) {
						if( arg0.getAttributeValue(0).equals("1")) {
							//是取消组队时服务器返回的结果IQ包
							DecideActivity.this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									//dismiss dialog
									registerDialog.dismiss();
								}
								
							});
							isJoin = true;				//避免执行下面的加入房间的代码
							break;
						}
					}
				}
				if (eventType == XmlPullParser.TEXT) {
					// 获得roomJID
					// parser.getText();
					done = true;
					roomJID = arg0.getText();
					UserInfo.roomJID = roomJID;
				}
				eventType = arg0.next();
			}
			if (!isJoin) {
				isJoin = true;
				DecideActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						ChatActivity.chat = new MultiUserChat(
								mXmppConnection, roomJID);
						// ChatActivity.chat2 = new
						// MultiUserChat(LoginActivity.mXmppConnection2,
						// roomJID);
						try {
							ChatActivity.chat
									.join(mXmppConnection
											.getUser());
							
							// ChatActivity.chat2.join(LoginActivity.mXmppConnection2.getUser());
							// 匹配成功 进入chatActitity
							registerDialog.dismiss();
							Toast.makeText(DecideActivity.this, "匹配成功",
									Toast.LENGTH_SHORT).show();
							DecideActivity.this.startActivity(intentIT);
							DecideActivity.this.finish();
						} catch (XMPPException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoResponseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NotConnectedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});
			}
			return null;
		}

	}

	/**
	 * 响应服务器发来的房间ID 加入该房间
	 * 
	 * @author hankwing
	 * 
	 */
	public class ResultTopicInfoProvider implements IQProvider {

		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
			// TODO Auto-generated method stub
			parser.require(XmlPullParser.START_TAG, "com:talky:requestTopicInfo", "requestTopicInfo");
			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) {
		            continue;
		        }
				String name = parser.getName();
							
				if (name.equals("description")) {
					parser.require(XmlPullParser.START_TAG, null, "description");
					topicDescription = readText(parser);
				    parser.require(XmlPullParser.END_TAG, null, "description");	
				} else if (name.equals("positive")) {
					parser.require(XmlPullParser.START_TAG, null, "positive");
					positive = readText(parser);
				    parser.require(XmlPullParser.END_TAG, null, "positive");	
				} else if (name.equals("negative")) {
					parser.require(XmlPullParser.START_TAG, null, "negative");
					negative = readText(parser);
				    parser.require(XmlPullParser.END_TAG, null, "negative");	
				} else {
					skip(parser);
				}
			}
			//解析结束 界面操作开始
			DecideActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					topicDesc.setText(topicDescription);
					positiveView.setText(positive);
					negativeView.setText(negative);
					
					//hide progressBar and show content
					svContentView.setAlpha(0f);
					svContentView.setVisibility(View.VISIBLE);
					svContentView.animate()
		            	.alpha(1f)
		            	.setDuration(mShortAnimationDuration)
		            	.setListener(null);
					progressBar.animate().alpha(0f).setDuration(mShortAnimationDuration)
					.setListener(new AnimatorListenerAdapter() {

						@Override
						public void onAnimationEnd(Animator animation) {
							// TODO Auto-generated method stub
							progressBar.setVisibility(View.GONE);
							super.onAnimationEnd(animation);
						}
						
					});
				}
			});
			return null;
		}
		
		/**
		 * 跳过一个标签
		 * @param parser
		 * @throws XmlPullParserException
		 * @throws IOException
		 */
		private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		    if (parser.getEventType() != XmlPullParser.START_TAG) {
		        throw new IllegalStateException();
		    }
		    int depth = 1;
		    while (depth != 0) {
		        switch (parser.next()) {
		        case XmlPullParser.END_TAG:
		            depth--;
		            break;
		        case XmlPullParser.START_TAG:
		            depth++;
		            break;
		        }
		    }
		 }
		
		/**
		 * 读取标签下的内容
		 * @param parser
		 * @return
		 * @throws IOException
		 * @throws XmlPullParserException
		 */
		private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		    String result = "";
		    if (parser.next() == XmlPullParser.TEXT) {
		        result = parser.getText();
		        parser.nextTag();
		    }
		    return result;
		}

	}

}
