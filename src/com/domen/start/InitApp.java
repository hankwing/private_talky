package com.domen.start;

import com.wxl.lettalk.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

/**
 * 应用初始化时的工作，如果第一次打开那么就打开介绍界面，否则正常进入应用
 * @author hankwing
 *
 */
public class InitApp extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		//handler
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				SharedPreferences settings = getSharedPreferences("runTime", 0);
				if(settings.getInt("time", 0)==0) {
					Intent intent = new Intent(InitApp.this, GuideActivity.class);
					InitApp.this.startActivity(intent);
					settings.edit().putInt("time", 1).commit();
					InitApp.this.finish();
					overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
				}
				else {
					Intent mainIntent = new Intent(InitApp.this, LoginActivity.class);
					if(getIntent().getExtras() != null) {
						mainIntent.putExtra("flag", getIntent().getExtras().getInt("flag"));
					}
					InitApp.this.startActivity(mainIntent);
					InitApp.this.finish();
					//overridePendingTransition(R.anim.zoomin, R.anim.zoomout);  
					//overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
					overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
				}
			}
		}, 1000);
	}

}
