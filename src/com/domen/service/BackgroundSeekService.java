package com.domen.service;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.domen.activities.ChatActivity;
import com.domen.start.R;
import com.domen.tools.TopicsContract.TopicsEntryContract;

public class BackgroundSeekService extends Service {

	private String roomJID;
	private Notification mNotfication;
	private String userFullId;
	private String topicID;
	private String side;
	private String topicName;
	private NotificationManager mNotificationManager;
	private Handler mHandler = null;
	private String topicURL;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mHandler = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					// BackgroundSeekService.this.stopForeground(true);
					BackgroundSeekService.this.stopSelf();
					break;
				case 2:
					stopForeground(true);
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
							BackgroundSeekService.this).setSmallIcon(R.drawable.ic_launcher)
							.setTicker("组队成功 赶快来应战吧").setAutoCancel(true).
							setLights(Color.argb(255,255 ,98, 98), 1000, 1000)
							.setContentTitle("组队成功").setContentText("点击进入聊天");
					 Intent resultIntent = new Intent(BackgroundSeekService.this,
							 ChatActivity.class);
					 Bundle bundle = new Bundle();
					 bundle.putString(TopicsEntryContract.COLUMN_NAME_OF_ID, topicID); // put topic id
					 if( side.equals("positive")) {
						 bundle.putInt("side", 1);
					 }
					 else {
						 bundle.putInt("side", -1);
					 }
					bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
								topicName);
					bundle.putString("roomJID",(String) msg.obj);
					bundle.putBoolean("returnFromNotification", true);
					bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_URL, topicURL);
					bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME, topicName);
					resultIntent.putExtras(bundle);
					 TaskStackBuilder stackBuilder = TaskStackBuilder.create(
							 BackgroundSeekService.this);
					 stackBuilder.addParentStack(ChatActivity.class);
					 stackBuilder.addNextIntent(resultIntent);
					 
					 Intent decideActivity = stackBuilder.editIntentAt(1);	//decideActivity
					 decideActivity.putExtras(bundle);
					 
					 PendingIntent resultPendingIntent =
					 stackBuilder.getPendingIntent(
					 0,
					 PendingIntent.FLAG_UPDATE_CURRENT
					 );
					 mBuilder.setContentIntent(resultPendingIntent);
					mNotificationManager.notify(1, mBuilder.build());
					
					break;
				}
			}

		};
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		ProviderManager.addIQProvider("success", "com:talky:asRequestATeamIQ",
				new BackgroundProvider());

		Bundle bundle = intent.getExtras();
		userFullId = bundle.getString("userFullId");
		topicID = bundle.getString("topicID");
		side = bundle.getString("side");
		topicName = bundle.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME);
		topicURL = bundle.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_URL);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setTicker("正在寻找对手")
				.setContentTitle("正在寻找对手").setContentText("点击可退出队列");

		Intent notifyIntent = new Intent(this, CancelNoUiActivity.class);
		notifyIntent.putExtras(bundle);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent mNotify = PendingIntent.getActivity(this, 0,
				notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(mNotify);
		mNotfication = mBuilder.build();
		startForeground(1, mNotfication);
		return START_REDELIVER_INTENT;
	}

	/**
	 * respond to the room id from server and join it
	 * 
	 * @author hankwing
	 * 
	 */
	public class BackgroundProvider implements IQProvider {

		@Override
		public IQ parseIQ(XmlPullParser arg0) throws Exception {
			// TODO Auto-generated method stub
			boolean done = false;
			boolean isJoin = false;
			int eventType = arg0.getEventType();
			while (!done) {
				if (eventType == XmlPullParser.START_TAG) {
					if (arg0.getName().equals("success")) {
						if (arg0.getAttributeValue(0).equals("1")) {
							// dismiss the notification
							mHandler.obtainMessage(1).sendToTarget();
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
				// succeed form a team!
				// change the status of the notification
				mHandler.obtainMessage(2, roomJID).sendToTarget();

			}
			return null;
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
