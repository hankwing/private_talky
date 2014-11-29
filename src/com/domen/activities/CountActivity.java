package com.domen.activities;

import com.domen.start.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * 比谁的屎多
 * @author hankwing
 *
 */
public class CountActivity extends Activity{

	
	class TimeCount extends CountDownTimer{
		
		Context context;

		public TimeCount(long millisInFuture, long countDownInterval,Context context) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
			this.context = context;
		}

		@Override
		public void onFinish() {
			CountActivity.this.finish();
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			tv_num_count.setText(millisUntilFinished / 1000 + "次");
		}
		
	}
	
	ImageView ibt_act_count;
	TextView tv_num_count;
	TextView tv_start;
	static int count = 0;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_count);
		ibt_act_count = (ImageView) this.findViewById(R.id.ibt_act_count);
		tv_num_count = (TextView) this.findViewById(R.id.tv_num_count);
		tv_start = (TextView) this.findViewById(R.id.tv_start);
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation translateanim = new TranslateAnimation(0,0f,Animation.RELATIVE_TO_SELF,1f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		translateanim.setDuration(2000);
		animationSet.setStartOffset(800);
		animationSet.addAnimation(translateanim);
		animationSet.setFillAfter(true);
		tv_start.startAnimation(animationSet);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
		}, 3000);
		ibt_act_count.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				count++;
			}
		});
		super.onCreate(savedInstanceState);
	}
	
}


