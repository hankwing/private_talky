package com.domen.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.domen.adapter.RoomListAdapter;
import com.domen.openfire.RequestExistingRoom;
import com.domen.tools.CurrentActivity;
import com.domen.tools.JsonUtil;
import com.domen.tools.LoadAvatarManager;
import com.domen.tools.MXMPPConnection;
import com.domen.tools.TopicsContract.TopicsEntryContract;
import com.domen.start.R;

public class RoomListActivity extends ListActivity implements OnClickListener {

	private String topicOfId; // 话题的ID号 用于服务器标识
	private ArrayList<Map<String, String>> roomData ;				//房间列表
	private RoomListAdapter adapter = null;
	private XMPPTCPConnection mXmppConnection = null;
	private String topicName;	//话题名称
	private ImageView close;
	private ProgressBar mProgressBar;
	private TextView emptyText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getActionBar().setDisplayHomeAsUpEnabled(true); // 显示返回上级的按钮
		//getActionBar().setDisplayShowTitleEnabled(true);
		CurrentActivity.setCurrentActivity(this);
		setContentView(R.layout.roomlist_layout);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		emptyText = (TextView) findViewById(R.id.empty_hint);
		
		// 添加话题详情IQ监听器
		ProviderManager.addIQProvider("roomListJson",
				"com:talky:requestExistingRoom", new ResultTopicInfoProvider());
		close = (ImageView) findViewById(R.id.roomlist_return);
		close.setOnClickListener(this);
		
		if( savedInstanceState == null) {
			Bundle bundle = this.getIntent().getExtras();
			topicOfId = bundle.getString(TopicsEntryContract.COLUMN_NAME_OF_ID);
			topicName = bundle.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME);
			
		}else {
			topicOfId = savedInstanceState.getString(TopicsEntryContract.COLUMN_NAME_OF_ID);
			topicName = savedInstanceState.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME);
		}
		
		roomData = new ArrayList<Map<String, String>>();
		LoadAvatarManager.getInstance().setData(roomData);				//set data
		//发送请求获得已有话题的IQ包
		RequestExistingRoom rs = new RequestExistingRoom(topicOfId);
		rs.setType(IQ.Type.GET);
		MXMPPConnection.sendPacket( this, rs);
		adapter = new RoomListAdapter(this, roomData);
		setListAdapter(adapter);
		LoadAvatarManager.getInstance().setAdapter(adapter);
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch( v.getId()) {
		case R.id.roomlist_return:
			finish();
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

	/**
	 * 点击条目监听器
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		mXmppConnection = MXMPPConnection.getInstance();
		if( mXmppConnection != null) {
			//连接正常 加入房间 先初始化用户点赞点屎列表
			Map<String, String> choosenRoom = roomData.get(position);
			ArrayList<String> userList = new ArrayList<String>();
			userList.add(choosenRoom.get("positive1JID"));
			userList.add(choosenRoom.get("negative1JID"));
			ChatActivity.initUserCache(userList);
			
			ChatActivity.chat = new MultiUserChat(mXmppConnection,
					roomData.get(position).get("roomJID"));
			
			try {
				ChatActivity.chat.join(mXmppConnection.getUser());
				Intent intentIT = new Intent(getApplicationContext(), ChatActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(TopicsEntryContract.COLUMN_NAME_OF_ID, topicOfId); // 带上选择话题的ID
				bundle.putInt("side", 0);				//0代表观众
				bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
						topicName);
				bundle.putString("roomJID", roomData.get(position).get("roomJID"));
				intentIT.putExtras(bundle);
				startActivity(intentIT);				//打开聊天窗口
				finish();
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
		else {
			Toast.makeText(this, getResources().getString(R.string.internet_failure), 
				Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 
	 * 
	 * @author hankwing
	 * 
	 */
	public class ResultTopicInfoProvider implements IQProvider {

		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
			// TODO Auto-generated method stub
			parser.require(XmlPullParser.START_TAG, "com:talky:requestExistingRoom", "roomListJson");
			String topicDescription = readText(parser);
			//Log.i("message", "topicDescription: "+ topicDescription);
			parser.require(XmlPullParser.END_TAG, "com:talky:requestExistingRoom", "roomListJson");
			if( topicDescription != null && topicDescription != "") {
				//房间信息非空则分析房间信息的json数据
				JsonUtil.AnalysisRoomList(topicDescription, roomData);
				//obtain user vcard
				
			}
			
			RoomListActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if( roomData.size() != 0) {
						mProgressBar.setVisibility(View.GONE);
						adapter.notifyDataSetChanged();
					}
					else {
						//empty room list
						mProgressBar.setVisibility(View.GONE);
						emptyText.setVisibility(View.VISIBLE);
					}
					
					for( int i = 0; i < roomData.size(); i++) {
						LoadAvatarManager.startDownload(adapter, roomData, 
								roomData.get(i).get("negative1BAREJID"), i, "negative1");
						LoadAvatarManager.startDownload(adapter, roomData, 
								roomData.get(i).get("positive1BAREJID"), i, "positive1");
					}
				}
				
			});
			
			return null;
		}

//		/**
//		 * 跳过一个标签
//		 * 
//		 * @param parser
//		 * @throws XmlPullParserException
//		 * @throws IOException
//		 */
//		private void skip(XmlPullParser parser) throws XmlPullParserException,
//				IOException {
//			if (parser.getEventType() != XmlPullParser.START_TAG) {
//				throw new IllegalStateException();
//			}
//			int depth = 1;
//			while (depth != 0) {
//				switch (parser.next()) {
//				case XmlPullParser.END_TAG:
//					depth--;
//					break;
//				case XmlPullParser.START_TAG:
//					depth++;
//					break;
//				}
//			}
//		}

		/**
		 * 读取标签下的内容
		 * 
		 * @param parser
		 * @return
		 * @throws IOException
		 * @throws XmlPullParserException
		 */
		private String readText(XmlPullParser parser) throws IOException,
				XmlPullParserException {
			String result = "";
			if (parser.next() == XmlPullParser.TEXT) {
				result = parser.getText();
				parser.nextTag();
			}
			return result;
		}

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		clearReferences();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		clearReferences();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		CurrentActivity.setCurrentActivity(this);
	}

	/**
	 * 清除当前界面标记
	 */
	private void clearReferences() {
		Activity currActivity = CurrentActivity.getCurrentActivity();
		if (currActivity != null && currActivity.equals(this))
			CurrentActivity.setCurrentActivity(null);
	}
	
	/**
	 * 销毁activity时保存状态
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME, topicName);
		outState.putString(TopicsEntryContract.COLUMN_NAME_OF_ID, topicOfId);

		super.onSaveInstanceState(outState);
		
	}

}
