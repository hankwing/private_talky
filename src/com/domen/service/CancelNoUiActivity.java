package com.domen.service;

import org.jivesoftware.smack.packet.IQ;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.domen.openfire.RequestATeam;
import com.domen.start.R;
import com.domen.tools.MXMPPConnection;

public class CancelNoUiActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// cancel form a team
		Bundle bundle = getIntent().getExtras();
		
		RequestATeam cancel = new RequestATeam( bundle.getString("userFullId"), 
				bundle.getString("topicID"), bundle.getString("side"), "0");
		
		cancel.setType(IQ.Type.GET);
		MXMPPConnection.sendPacket( this, cancel);
		
		//change the status of the notification 
		NotificationManager mNotificationManager =
		        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		NotificationCompat.Builder mBuilder = 
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setTicker("正在取消组队")
		        .setContentTitle("正在取消组队请求");
		mNotificationManager.notify(
	            1,
	            mBuilder.build());
		
		finish();
	}

}
