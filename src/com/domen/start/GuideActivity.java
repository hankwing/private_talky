package com.domen.start;

import java.util.ArrayList;

import com.domen.adapter.GuidePagerAdapter;
import com.wxl.lettalk.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * 软件第一次进入时的介绍图片
 * @author hankwing
 *
 */
public class GuideActivity extends Activity implements OnClickListener,
OnPageChangeListener{

	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	private ViewGroup main, group;
	private ImageView[] dots;
	private ImageButton imgbtn;
	private int currentIndex;
	private GuidePagerAdapter vpAdapter;
	private ImageView imageView;
	private static final int GO_BTN = 10;
	private static final int[] help_pics = { R.drawable.guide0,
		R.drawable.guide1, R.drawable.guide2, R.drawable.guide_cooperate};

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
		main = (ViewGroup)inflater.inflate(R.layout.guide_layout, null); 
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		for (int i = 0; i < help_pics.length; i++) {
			RelativeLayout rl  = new RelativeLayout(this);
			rl.setLayoutParams(mParams);
			rl.setBackgroundResource(help_pics[i]);
			if(i == (help_pics.length -1)){
				imgbtn = new ImageButton(this);
				imgbtn.setBackgroundResource(R.drawable.help_go_btn_selector);
				imgbtn.setOnClickListener(this);
				imgbtn.setTag(GO_BTN);
				imgbtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent mainIntent = new Intent(GuideActivity.this, LoginActivity.class);
						startActivity(mainIntent);
						finish();
					}
				});
				RelativeLayout .LayoutParams lp1 =
						new RelativeLayout .LayoutParams(
								RelativeLayout.LayoutParams.WRAP_CONTENT,
								RelativeLayout.LayoutParams.WRAP_CONTENT);
				lp1.bottomMargin = 80;
				lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				lp1.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
				rl.addView(imgbtn,lp1);

			}
			pageViews.add(rl);

		}
		/*RelativeLayout rl  = new RelativeLayout(this);
		rl.setLayoutParams(mParams);
		pageViews.add(rl);*/
		group = (ViewGroup) main.findViewById(R.id.viewGroup);
		viewPager = (ViewPager) main.findViewById(R.id.guidePages);
		vpAdapter = new GuidePagerAdapter(pageViews);
		viewPager.setAdapter(vpAdapter);
		viewPager.setOnPageChangeListener(this);
		initDots();
		setContentView(main); 
	}

	private void initDots() {
		// TODO Auto-generated method stub
		dots = new ImageView[help_pics.length];  
		for (int i = 0; i < help_pics.length; i++) {
			imageView = new ImageView(GuideActivity.this);
			imageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			imageView.setMaxHeight(20);
			imageView.setMaxWidth(40);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setAdjustViewBounds(true);
			imageView.setPadding(10, 0, 10, 0);
			imageView.setImageResource(R.drawable.dot_selector);
			dots[i] = imageView;
			dots[i].setEnabled(false);
			dots[i].setTag(i);
			dots[i].setOnClickListener(this); 
			group.addView(dots[i]); 
		}
		currentIndex = 0;
		dots[currentIndex].setEnabled(true);
	}

	private void setCurView(int position) {
		if (position < 0 || position >= help_pics.length) {
			return;
		}
		viewPager.setCurrentItem(position);
	}

	private void setCurDot(int positon) {
		if (positon < 0 || positon > help_pics.length - 1
				|| currentIndex == positon) {
			return;
		}
		dots[positon].setEnabled(true);
		dots[currentIndex].setEnabled(false);
		currentIndex = positon;
	}
	/**
	 * (�� Javadoc) Title: onPageScrollStateChanged Description:
	 * 
	 * @param arg0
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
	 */
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * (�� Javadoc) Title: onPageScrolled Description:
	 * 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int,
	 *      float, int)
	 */
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	/**
	 * (�� Javadoc) Title: onPageSelected Description:
	 * 
	 * @param arg0
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
	 */
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		/*if(arg0 == pageViews.size()-1){
			viewPager.setCurrentItem (arg0 - 1);
			ToHome();
			return;
		}*/

		setCurDot(arg0);
	}

	/**
	 * (�� Javadoc) Title: onClick Description:
	 * 
	 * @param v
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int position = (Integer)v.getTag();
		if(position == GO_BTN){
			ToHome();
		}else{
			setCurView(position);  
			setCurDot(position); 
		}
	}

	public void ToHome(){
		/*Intent intentHome = new Intent();
		intentHome.setClass(HelpActivity.this, HomeActivity.class);
        startActivity(intentHome); 
        finish();*/
	}

	/** (�� Javadoc) 
	 * Title: onKeyDown
	 * Description:
	 * @param keyCode
	 * @param event
	 * @return 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent) 
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		finish();
		return super.onKeyDown(keyCode, event);
	}
}
