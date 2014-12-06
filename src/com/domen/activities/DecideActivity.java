package com.domen.activities;

import java.io.IOException;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.domen.openfire.RequestATeam;
import com.domen.openfire.RequestTopicInfo;
import com.domen.service.BackgroundSeekService;
import com.domen.start.R;
import com.domen.tools.BitmapSingleton;
import com.domen.tools.CurrentActivity;
import com.domen.tools.MXMPPConnection;
import com.domen.tools.TopicsContract.TopicsEntryContract;

/**
 * show topics information
 * 
 * @author hankwing
 * 
 */
public class DecideActivity extends Activity implements OnClickListener {

	private ImageView btn_discuss_home;
	private String topicName;
	private static String topicID;
	private String topicURL;
	private Bundle fromBundle;
	private String roomJID;
	private Intent intentIT;
	private RelativeLayout svContentView;
	private ProgressBar progressBar;
	private int mShortAnimationDuration; // shot duration time
	private NetworkImageView topicPic; // image related to the topic
	private ImageLoader mImageLoader;
	private TextView topicDesc;
	private TextView positiveView;
	private TextView negativeView;
	private String topicDescription = null;
	private String positive = null;
	private String negative = null;
	//private XMPPConnection mXmppConnection = null;
	private ProgressDialog seekDialog;
	private RequestATeam ngRA;
	private static SharedPreferences accountInfo;
	private Handler seekATeamHandler = null;
	private AlertDialog backgroundDialog;
	private String mSide;				//the side is seeking for a team

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_decide);
		Intent intent = this.getIntent();
		fromBundle = intent.getExtras();
		CurrentActivity.setCurrentActivity(this);
		
		seekATeamHandler = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(final Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				final String side = (String) msg.obj;
				if( side.equals(mSide) && seekDialog.isShowing() ) {
					seekDialog.dismiss();
					//notify user
					backgroundDialog.show();
					backgroundDialog.setMessage(getResources().getString(
							R.string.background_seek));
					//set button listener
					Button positive = backgroundDialog.getButton(AlertDialog.BUTTON_POSITIVE);
					positive.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							ProviderManager.removeIQProvider("success", "com:talky:asRequestATeamIQ");
							// turn on service
							Intent intent = new Intent(DecideActivity.this,
									BackgroundSeekService.class);
							Bundle bundle = new Bundle();
							bundle.putString("userFullId", accountInfo.getString(
									"userFullId", null));
							bundle.putString("topicID", topicID);
							bundle.putString("side", side);
							bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
									topicName);
							bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_URL,
									topicURL);
							intent.putExtras(bundle);
							DecideActivity.this.startService(intent);
							
							backgroundDialog.dismiss();
							DecideActivity.this.finish();
						}
						
					});
					
					Button negative  = backgroundDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
					negative.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							// cancel request
							backgroundDialog.setMessage(getResources().getString(
									R.string.cancel_requestateam_dialog));
							
							RequestATeam cancel = new RequestATeam( accountInfo.getString(
									"userFullId", null), topicID, side, "0");
							cancel.setType(IQ.Type.GET);
							MXMPPConnection.sendPacket(DecideActivity.this, cancel);
						}
						
					});
				}
			}
			
		};
		 // get user info preference
		accountInfo = getSharedPreferences("accoutInfo", Context.MODE_PRIVATE);
		// get topic info
		if(savedInstanceState == null) {
			topicName = fromBundle
					.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME);
			topicID = fromBundle.getString(TopicsEntryContract.COLUMN_NAME_OF_ID);
			topicURL = fromBundle
					.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_URL);
		}
		else {
			// return from destroyed state
			topicName = savedInstanceState
					.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME);
			topicID = savedInstanceState.getString(TopicsEntryContract.COLUMN_NAME_OF_ID);
			topicURL = savedInstanceState
					.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_URL);
		}
		
		getActionBar().setTitle(topicName); // show topic name
		// add handler for request topic info
		ProviderManager.addIQProvider("requestTopicInfo",
				"com:talky:requestTopicInfo", new ResultTopicInfoProvider());
		// send request for topic info
		RequestTopicInfo rs = new RequestTopicInfo(topicID);
		rs.setType(IQ.Type.GET);
		MXMPPConnection.sendPacket(DecideActivity.this, rs);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		btn_discuss_home = (ImageView) this.findViewById(R.id.btn_discuss_home);
		btn_discuss_home.setOnClickListener(this);
		topicDesc = (TextView) findViewById(R.id.topicDes); // topic description
		positiveView = (TextView) findViewById(R.id.tv_positive); // positive view
		positiveView.setOnClickListener(this);
		negativeView = (TextView) findViewById(R.id.tv_negative); // negative view
		negativeView.setOnClickListener(this);
		svContentView = (RelativeLayout) findViewById(R.id.ll_content_view);
		progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
		svContentView.setVisibility(View.INVISIBLE); // hide content view
		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime); 

		mImageLoader = BitmapSingleton.getInstance(this).getImageLoader();
		topicPic = (NetworkImageView) findViewById(R.id.imgv_dec_image);
		topicPic.setImageUrl(topicURL, mImageLoader);
		seekDialog = new ProgressDialog(this);
		//seekDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// 取消组队逻辑
		seekDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				getResources().getString(R.string.cancel_requestateam),
				(DialogInterface.OnClickListener) null); // set null listener
		seekDialog.setCanceledOnTouchOutside(false); // dialog can not be canceled
		// notify the user whether to background seek
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.background_seek).
		setPositiveButton(R.string.dialog_positive, null)
				.setNegativeButton(R.string.dialog_negative,
						null);
		backgroundDialog = builder.create();
		backgroundDialog.setCanceledOnTouchOutside(false);
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		intentIT = new Intent(getApplicationContext(), ChatActivity.class);
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.tv_negative:
			mSide = "negative";
			bundle.putString(TopicsEntryContract.COLUMN_NAME_OF_ID, topicID); // put topic id
			bundle.putInt("side", -1);				//-1-negative party
			bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
					topicName);
			intentIT.putExtras(bundle);
			// request a team
			ngRA = new RequestATeam( accountInfo.getString(
					"userFullId", null), topicID, "negative", "1");
			ngRA.setType(IQ.Type.GET);
			MXMPPConnection.sendPacket(this, ngRA);
			seekDialog.setMessage(getResources()
					.getString(R.string.seeking));
			seekDialog.show();
			seekATeamHandler.sendMessageDelayed(seekATeamHandler.obtainMessage(0, "negative"),
					6000);			//within 10 seconds
			final Button cancelButton = seekDialog
					.getButton(DialogInterface.BUTTON_NEGATIVE);
			cancelButton.setVisibility(View.VISIBLE);
			cancelButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					seekDialog.setMessage(getResources().getString(
							R.string.cancel_requestateam_dialog));
					cancelButton.setVisibility(View.GONE);
					// cancel a request for forming a team
					ngRA.setIsJoin("0"); // mark
					ngRA.setType(IQ.Type.GET);
					MXMPPConnection.sendPacket(DecideActivity.this, ngRA);
				}
			});
			break;
		case R.id.tv_positive:
			mSide = "positive";
			bundle.putString(TopicsEntryContract.COLUMN_NAME_OF_ID, topicID); 
			bundle.putInt("side", 1);
			bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
					topicName);
			intentIT.putExtras(bundle);
			ngRA = new RequestATeam( accountInfo.getString(
					"userFullId", null), topicID, "positive", "1");
			ngRA.setType(IQ.Type.GET);
			MXMPPConnection.sendPacket(this, ngRA);
			seekDialog.setMessage(getResources()
					.getString(R.string.seeking));
			seekDialog.show();
			seekATeamHandler.sendMessageDelayed(seekATeamHandler.obtainMessage(0, "positive"),
					6000);			//within 10 seconds
			final Button cancel_button = seekDialog
					.getButton(DialogInterface.BUTTON_NEGATIVE);
			cancel_button.setVisibility(View.VISIBLE);
			cancel_button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					seekDialog.setMessage(getResources().getString(
							R.string.cancel_requestateam_dialog));
					cancel_button.setVisibility(View.GONE);
					// send request for cancel a team
					ngRA.setIsJoin("0"); 
					ngRA.setType(IQ.Type.GET);
					MXMPPConnection.sendPacket(DecideActivity.this, ngRA);
				}
			});
			break;
		case R.id.btn_discuss_home:
			bundle.putString(TopicsEntryContract.COLUMN_NAME_OF_ID, topicID); 
			bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME, topicName);
			Intent roomList = new Intent(getApplicationContext(),
					RoomListActivity.class);
			roomList.putExtras(bundle); 
			startActivity(roomList);
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
		clearReferences();
		super.onDestroy();
	}

	/**
	 * respond to the room id from server and join it
	 * 
	 * @author hankwing
	 * 
	 */
	public class ResultIQProvider implements IQProvider {

		@Override
		public IQ parseIQ(XmlPullParser arg0) throws Exception {
			// TODO Auto-generated method stub
			//Log.i("message", "receive an success iQ");
			boolean done = false;
			boolean isJoin = false;
			int eventType = arg0.getEventType();
			while (!done) {
				if (eventType == XmlPullParser.START_TAG) {
					if (arg0.getName().equals("success")) {
						if (arg0.getAttributeValue(0).equals("1")) {
							
							DecideActivity.this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									// dismiss dialog
									if( seekDialog.isShowing()) {
										seekDialog.dismiss();
									}
									else if( backgroundDialog.isShowing() ){
										backgroundDialog.dismiss();
									}
								}

							});
							isJoin = true; // avoid to join the room dumplicated
							break;
						}
					}
				}
				if (eventType == XmlPullParser.TEXT) {
					// get roomJID
					// parser.getText();
					done = true;
					roomJID = arg0.getText();
				}
				eventType = arg0.next();
			}
			if (!isJoin) {
				isJoin = true;
				DecideActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						// ChatActivity.chat2.join(LoginActivity.mXmppConnection2.getUser());
						// succeed enter chatActitity
						seekDialog.dismiss();
						Toast.makeText(DecideActivity.this, "匹配成功",
								Toast.LENGTH_SHORT).show();
						intentIT.putExtra("roomJID", roomJID);				//room JID
						DecideActivity.this.startActivity(intentIT);
						DecideActivity.this.finish();

					}
				});
			}
			return null;
		}

	}

	public class ResultTopicInfoProvider implements IQProvider {

		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
			// TODO Auto-generated method stub
			parser.require(XmlPullParser.START_TAG,
					"com:talky:requestTopicInfo", "requestTopicInfo");
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
			// parse finished
			DecideActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					topicDesc.setText(topicDescription);
					positiveView.setText(positive);
					negativeView.setText(negative);

					// hide progressBar and show content
					progressBar.animate().alpha(0f)
					.setDuration(0)
					.setListener(new AnimatorListenerAdapter() {

						@Override
						public void onAnimationEnd(Animator animation) {
							// TODO Auto-generated method stub
							progressBar.setVisibility(View.GONE);
							super.onAnimationEnd(animation);
						}

					});
					
					svContentView.setAlpha(0f);
					svContentView.setVisibility(View.VISIBLE);
					svContentView.animate().alpha(1f)
							.setDuration(mShortAnimationDuration)
							.setListener(null);
					
				}
			});
			return null;
		}

		/**
		 * skip a tag
		 * 
		 * @param parser
		 * @throws XmlPullParserException
		 * @throws IOException
		 */
		private void skip(XmlPullParser parser) throws XmlPullParserException,
				IOException {
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
		 * read text under a tag
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		clearReferences();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		ProviderManager.removeIQProvider("success", "com:talky:asRequestATeamIQ");
		ProviderManager.addIQProvider("success", "com:talky:asRequestATeamIQ",
				new ResultIQProvider());
		CurrentActivity.setCurrentActivity(this);
		
	}

	private void clearReferences() {
		Activity currActivity = CurrentActivity.getCurrentActivity();
		if (currActivity != null && currActivity.equals(this))
			CurrentActivity.setCurrentActivity(null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub

		outState.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME, topicName);
		outState.putString(TopicsEntryContract.COLUMN_NAME_OF_ID, topicID);
		outState.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_URL, topicURL);
		super.onSaveInstanceState(outState);
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

	}
	
}
