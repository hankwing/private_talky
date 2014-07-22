package com.domen.lettalk;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Entity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//����xmpp�˺ŵİ�
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
//�������ӹ���
import org.jivesoftware.smack.Connection;
//������������
import org.jivesoftware.smack.ConnectionConfiguration;
//xmpp�����ӹ���
import org.jivesoftware.smack.XMPPConnection;
//xmpp���쳣
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.pubsub.ConfigurationEvent;

import com.domen.adapter.TabPagerAdapter;
import com.domen.adapter.ThemeListAdapter;
import com.domen.entity.ThemeEntity;
import com.domen.start.InitApp;
import com.domen.viewsolve.ShowView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.wxl.lettalk.R;

public class MainActivity extends Activity implements OnClickListener{

	private ViewPager themeViewPager;	//ҳ������
	private ThemeListAdapter themeAdapter;
	private List<ThemeListAdapter> adapterslist;
	private List<View> viewslist;	//Tabҳ���б�
	private TextView tv_comic;
	private TextView tv_science;
	private TextView tv_enviro;
	private SlidingMenu selfMenu;
	private ImageView headView;
	private ShowView showView;
	private TextView tv_myself;
	private int offset = 0;	//����ͼƬƫ����
	private	int currIndex = 0; //��ǰҳ������
	private int bmpW; //����ͼƬ�߶�
	private ImageView tab_under_img;

	public static float Height = 0;// ��Ļ�߶�
	public static float Width = 0;// ��Ļ���

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		tv_comic = (TextView) this.findViewById(R.id.theme_comic);
		tv_science = (TextView) this.findViewById(R.id.theme_science);
		tv_enviro = (TextView) this.findViewById(R.id.theme_enviro);
		tv_myself = (TextView) this.findViewById(R.id.tv_main_self);

		tv_comic.setOnClickListener(this);
		tv_enviro.setOnClickListener(this);
		tv_science.setOnClickListener(this);

		viewslist = new ArrayList<View>();
		adapterslist = new ArrayList<ThemeListAdapter>();

		DisplayMetrics Win = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(Win);
		Width = Win.widthPixels;
		Height = Win.heightPixels;
		showView = new ShowView(getApplicationContext(), this.Width, this.Height);

		initSelfMenu();

		//��ӽ���
		LayoutInflater inflater = LayoutInflater.from(this);
		viewslist.add(inflater.inflate(R.layout.comic_layout, null));
		viewslist.add(inflater.inflate(R.layout.science_layout, null));
		viewslist.add(inflater.inflate(R.layout.enviro_layout, null));

		themeViewPager = (ViewPager) this.findViewById(R.id.theme_view);

		adapterslist.add(initComic());
		adapterslist.add(initScience());

		themeViewPager.setAdapter(new TabPagerAdapter(this,viewslist,adapterslist));
		themeViewPager.setCurrentItem(0);
		
		//��ʼ��tab_under_img
		InitImageView();
		
		themeViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			//����ƫ��λ��
			int one = offset*2+bmpW;
			
			@Override
			public void onPageSelected(int arg0) {
				if(arg0!=0){
					selfMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
				}
				else{
					selfMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
				}
				Animation anim = new TranslateAnimation(one*currIndex,one*arg0,0,0);
				currIndex = arg0;
				anim.setFillAfter(true);//ͼƬ�����ڶ��������ĵط�
				anim.setDuration(300);
				tab_under_img.startAnimation(anim);
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});

		tv_myself.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				selfMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			}
		});

	}

	
	private void InitImageView(){
		tab_under_img = (ImageView) this.findViewById(R.id.tab_under_img);
		//��ȡͼƬ���
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.tab_under_img).getWidth();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// ��ȡ�ֱ��ʿ��
		offset = (screenW / 4 - bmpW) / 2;// ����ƫ����
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		tab_under_img.setImageMatrix(matrix);// ���ö�����ʼλ��
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.theme_comic:
			themeViewPager.setCurrentItem(0);
			break;

		case R.id.theme_science:
			themeViewPager.setCurrentItem(1);
			break;

		case R.id.theme_enviro:
			themeViewPager.setCurrentItem(2);
			break;

		default:
			break;
		}
	}

	public ThemeListAdapter initComic(){

		Drawable drawable = getResources().getDrawable(R.drawable.theme_init_view);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());

		ThemeEntity themeEnitiy = new ThemeEntity("����ħ����", "�ÿ�", drawable);

		List<ThemeEntity> themelist = new ArrayList<ThemeEntity>();
		themelist.add(themeEnitiy);

		ThemeListAdapter adapter = new ThemeListAdapter(getApplicationContext(), themelist);
		return adapter;
	}

	public ThemeListAdapter initScience(){

		ThemeEntity themeEnitiy = new ThemeEntity("����", "�ÿ�", null);
		ThemeEntity themeEnitiy2 = new ThemeEntity("�Ǻ�", "һ��", null);
		ThemeEntity themeEnitiy3 = new ThemeEntity("�ٺ�", "����", null);

		List<ThemeEntity> themelist = new ArrayList<ThemeEntity>();
		themelist.add(themeEnitiy);
		themelist.add(themeEnitiy2);
		themelist.add(themeEnitiy3);

		ThemeListAdapter adapter = new ThemeListAdapter(getApplicationContext(), themelist);
		return adapter;
	}

	private void initSelfMenu(){

		selfMenu = new SlidingMenu(getApplicationContext());
		// ���û����˵�������ֵ
		selfMenu = new SlidingMenu(this);
		selfMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		selfMenu.setShadowWidthRes(R.dimen.shadow_width);
		//selfMenu.setShadowDrawable(R.drawable.shadow);
		selfMenu.setBehindOffsetRes(R.dimen.shadow_offset);
		selfMenu.setFadeDegree(0.35f);
		selfMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		// ���û����˵�����ͼ����
		selfMenu.setMenu(R.layout.self_layout);

		selfMenu.setOnOpenListener(new OnOpenListener() {

			@Override
			public void onOpen() {
				// TODO Auto-generated method stub

			}
		});

		headView = (ImageView) findViewById(R.id.iv_head);
		headView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//ѡ��ͷ��
				String status = Environment.getExternalStorageState();
				if (status.equals(Environment.MEDIA_MOUNTED))
				{
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					if (intent != null)
					{
						startActivityForResult(intent, 49);
					}
				} else
				{
					Toast.makeText(MainActivity.this,"�򲻿�",
							Toast.LENGTH_SHORT).show();
				}
			}

		});



	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 49){
			Uri pictureUri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = managedQuery(pictureUri, proj, null, null, null);
			cursor.moveToFirst();
			//��ȡͼƬ
			Bitmap tmp = ShowView.readBitmapAutoSize(cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATA)).toString()
					,MainActivity.Width,MainActivity.Height);

			Drawable headDrawable = new BitmapDrawable(ShowView.toRoundCorner(tmp, 35));

			headView.setBackgroundDrawable(headDrawable);
		}
	}


}

