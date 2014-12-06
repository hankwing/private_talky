package com.domen.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.domen.adapter.OccupantsGridAdapter;
import com.domen.adapter.OccupantsListAdapter;
import com.domen.openfire.RequestOccupantsList;
import com.domen.start.R;
import com.domen.tools.CurrentActivity;
import com.domen.tools.JsonUtil;
import com.domen.tools.LoadAvatarManager;
import com.domen.tools.MUserChatManager;
import com.domen.tools.MXMPPConnection;
import com.domen.tools.TopicsContract.TopicsEntryContract;

public class ShowOccupantsActivity extends ListActivity implements
		OnClickListener {

	private String roomJID;
	private String topicID;
	private Map<String, String> occupantsData = null; // active occupants data
	private List<String> allOccupantsData = null; // all occupants list( fullJID with roomJID)
	private List<String> allOccupantsNickName = null; // nickname
	private List<String> allOccupantsJID = null; // user's fullJID
	private ProgressBar mProgressBar;
	private OccupantsListAdapter adapter;
	private OccupantsGridAdapter gridViewAdapter;
	private TextView emptyText;
	private ImageView returnButton;
	private GridView avatarGrid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		CurrentActivity.setCurrentActivity(this);
		setContentView(R.layout.occupants_list_layout);
		occupantsData = new HashMap<String, String>();
		allOccupantsNickName = new ArrayList<String>();
		allOccupantsJID = new ArrayList<String>();
		mProgressBar = (ProgressBar) findViewById(R.id.occupants_list_progress_bar);
		emptyText = (TextView) findViewById(R.id.occupants_list_empty_hint);
		returnButton = (ImageView) findViewById(R.id.occupants_list_roomlist_return);
		avatarGrid = (GridView) findViewById(R.id.avatar_gridview);
		returnButton.setOnClickListener(this);
		// add listener to handle occupants list data
		ProviderManager.addIQProvider("occupantsListJson",
				"com:talky:requestOccupantsList",
				new OccupantsListResultProvider());

		Bundle bundle = getIntent().getExtras();
		roomJID = bundle.getString("roomJID");
		topicID = bundle.getString(TopicsEntryContract.COLUMN_NAME_OF_ID);
		//get object from intent activity
		MUserChatManager occupantsList = (MUserChatManager) getIntent().
				getSerializableExtra("occupantsList");
		
		allOccupantsData = occupantsList.getListData(); // get full occupants list
		for( String temp : allOccupantsData ) {
			String fullJID = StringUtils.parseResource(temp);
			allOccupantsJID.add(fullJID);
			allOccupantsNickName.add(StringUtils.parseName(fullJID));
		}
		adapter = new OccupantsListAdapter(this, allOccupantsNickName);
		setListAdapter(adapter);
		LoadAvatarManager.getInstance().setOccupantsListData(occupantsData);		//set data
		LoadAvatarManager.getInstance().setAdapter(adapter);				//set adapter
		
		// send request active occupants list
		RequestOccupantsList rs = new RequestOccupantsList(topicID, roomJID);
		rs.setType(IQ.Type.GET);
		MXMPPConnection.sendPacket(this, rs);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.occupants_list_roomlist_return:
			finish();
			break;
		}
	}

	/**
	 * 清除当前界面标记
	 */
	private void clearReferences() {
		Activity currActivity = CurrentActivity.getCurrentActivity();
		if (currActivity != null && currActivity.equals(this))
			CurrentActivity.setCurrentActivity(null);
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
	 * listene to handle occupants list data
	 * 
	 * @author hankwing
	 * 
	 */
	public class OccupantsListResultProvider implements IQProvider {

		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
			// TODO Auto-generated method stub
			parser.require(XmlPullParser.START_TAG,
					"com:talky:requestOccupantsList", "occupantsListJson");
			String topicDescription = readText(parser);
			// Log.i("message", "topicDescription: "+ topicDescription);
			parser.require(XmlPullParser.END_TAG,
					"com:talky:requestOccupantsList", "occupantsListJson");
			if (topicDescription != null && topicDescription != "") {
				// 房间信息非空则分析房间信息的json数据
				JsonUtil.AnalysisOccupantsList(topicDescription, occupantsData);
				//remove from all user list so obtaining inactive user list
				allOccupantsJID.remove(occupantsData.get("positive1JID"));		
				allOccupantsJID.remove(occupantsData.get("negative1JID"));
			}

			ShowOccupantsActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mProgressBar.setVisibility(View.GONE);
					if (occupantsData.size() != 0) {
						adapter.addOccupantsData(occupantsData);
						adapter.notifyDataSetChanged();

						gridViewAdapter = new OccupantsGridAdapter(ShowOccupantsActivity.this,
								allOccupantsJID);	//show inactive avatar
						avatarGrid.setAdapter(gridViewAdapter);
					} else {
						// empty occupants list
						emptyText.setVisibility(View.VISIBLE);
					}
					
					//load user rank info
					LoadAvatarManager.startDownload(adapter, occupantsData,
							occupantsData.get("negative1BAREJID"), 0, "negative1", 
							LoadAvatarManager.REQUESTFROMOCCUPANTSACTIVITY);
					LoadAvatarManager.startDownload(adapter, occupantsData, 
							occupantsData.get("positive1BAREJID"), 0, "positive1", 
							LoadAvatarManager.REQUESTFROMOCCUPANTSACTIVITY);
					//load inactive avatar data
					
				}

			});

			return null;
		}

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

}
