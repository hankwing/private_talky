package com.domen.start;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.domen.adapter.GuidePagerAdapter;
import com.wxl.lettalk.R;

/**
 * 软件第一次进入时的介绍图片
 * 
 * @author hankwing
 * 
 */
public class GuideActivity extends Activity implements OnPageChangeListener {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	private ViewGroup main, group;
	// private ImageView[] dots;
	private ImageButton imgbtn;
	private int currentIndex;
	private GuidePagerAdapter vpAdapter;
	private ImageView imageView;
	private static final int GO_BTN = 10;
	private static final int[] help_pics = { R.drawable.guide_cooperate };

	/**
	 * ( Javadoc) Title: onCreate Description:
	 * 
	 * @param savedInstanceState
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		pageViews = new ArrayList<View>();
		LayoutInflater inflater = getLayoutInflater();
		main = (ViewGroup) inflater.inflate(R.layout.guide_layout, null);
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		gestureDetector = new GestureDetector(this, new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return gestureDetector.onTouchEvent(event);
			}
		};

		for (int i = 0; i < help_pics.length; i++) {
			RelativeLayout rl = new RelativeLayout(this);
			rl.setLayoutParams(mParams);
			rl.setBackgroundResource(help_pics[i]);
			if (i == (help_pics.length - 1)) {

				rl.setOnTouchListener(gestureListener);

			}
			pageViews.add(rl);

		}

		group = (ViewGroup) main.findViewById(R.id.viewGroup);
		viewPager = (ViewPager) main.findViewById(R.id.guidePages);
		vpAdapter = new GuidePagerAdapter(pageViews);
		viewPager.setAdapter(vpAdapter);
		viewPager.setOnPageChangeListener(this);
		setContentView(main);
	}

	class MyGestureDetector extends SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			//Log.i("message", "onfling");
			try {
				if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					
					Intent mainIntent = new Intent(GuideActivity.this,
							PreLoginActivity.class);
					startActivity(mainIntent);
					finish();
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
