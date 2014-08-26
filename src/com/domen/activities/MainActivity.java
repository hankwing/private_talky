package com.domen.activities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.domen.adapter.TabPagerAdapter;
import com.domen.adapter.ThemeListAdapter;
import com.domen.entity.ThemeEntity;
import com.domen.other.Util;
import com.domen.start.LoginActivity;
import com.domen.viewsolve.ShowView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.wxl.lettalk.R;

/**
 * 主界面
 * @author hankwing
 *
 */
public class MainActivity extends Activity implements OnClickListener{

	private ViewPager themeViewPager;				//话题选择page
	private List<ThemeListAdapter> adapterslist;	//配合ViewPager的adapter
	private List<View> viewslist;					//放置多个view给ViewPager
	private TextView tv_society;
	private TextView tv_science;
	private TextView tv_enviro;
	private SlidingMenu selfMenu;					//滑动侧边菜单
	private ImageView headView;
	private int offset = 0;
	private	int currIndex = 0;
	private int bmpW;
	private ImageView tab_under_img;
	private ArrayList<ArrayList<HashMap<String, Object>>> topics;
	public static ProviderManager providerManager = null;
	public static float Height = 0;					//屏幕高度
	public static float Width = 0;					//屏幕宽度
					//存储话题数据

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		forceShowActionBarOverflowMenu();
		tv_society = (TextView) this.findViewById(R.id.theme_comic);
		tv_science = (TextView) this.findViewById(R.id.theme_science);
		tv_enviro = (TextView) this.findViewById(R.id.theme_enviro);
		tv_society.setOnClickListener(this);
		tv_enviro.setOnClickListener(this);
		tv_science.setOnClickListener(this);
		viewslist = new ArrayList<View>();
		adapterslist = new ArrayList<ThemeListAdapter>();
		//添加监听同步话题IQ包的IOProvider
		providerManager = ProviderManager.getInstance();
		providerManager.addIQProvider("json", "com:talky:syncTopics", new SyncTopicsIQProvider());
		DisplayMetrics Win = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(Win);
		Width = Win.widthPixels;					//获得屏幕宽度像素数
		Height = Win.heightPixels;					//获得屏幕高度像素数
		//showView = new ShowView(getApplicationContext(), this.Width, this.Height);
		initSelfMenu();								//初始化侧边滑动菜单 定义一系列事件
		LayoutInflater inflater = LayoutInflater.from(this);
		viewslist.add(inflater.inflate(R.layout.comic_layout, null));				//添加子activity
		viewslist.add(inflater.inflate(R.layout.science_layout, null));
		viewslist.add(inflater.inflate(R.layout.enviro_layout, null));
		themeViewPager = (ViewPager) this.findViewById(R.id.theme_view);
		adapterslist.add(initComic());
		adapterslist.add(initScience());
		themeViewPager.setAdapter(new TabPagerAdapter(this,viewslist,adapterslist));
		themeViewPager.setCurrentItem(0);
		InitImageView();
		themeViewPager.setOnPageChangeListener(new OnPageChangeListener() {

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
				anim.setFillAfter(true);
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

	}

	private void InitImageView(){
		tab_under_img = (ImageView) this.findViewById(R.id.tab_under_img);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.tab_under_img).getWidth();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		offset = (screenW / 4 - bmpW) / 2;
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		tab_under_img.setImageMatrix(matrix);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
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
//		case R.id.tv_main_self:
//			//selfMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
//			//暂时先放同步功能
//			RequestSync rs = new RequestSync();
//			rs.setType(IQ.Type.GET);
//			LoginActivity.mXmppConnection.sendPacket(rs);
//			
//			break;
		default:
			break;
		}
	}

	public ThemeListAdapter initComic(){

		Drawable drawable = getResources().getDrawable(R.drawable.theme_init_view);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());

		//一个话题就是一个Entity
		ThemeEntity themeEnitiy = new ThemeEntity("0", "你喜欢王小亮吗", "变态类", drawable );
		
		List<ThemeEntity> themelist = new ArrayList<ThemeEntity>();
		themelist.add(themeEnitiy);

		ThemeListAdapter adapter = new ThemeListAdapter(getApplicationContext(), themelist);
		return adapter;
	}

	public ThemeListAdapter initScience(){

		ThemeEntity themeEnitiy = new ThemeEntity("2","1", "1", null);
		ThemeEntity themeEnitiy2 = new ThemeEntity("3", "2","2",null);
		List<ThemeEntity> themelist = new ArrayList<ThemeEntity>();
		themelist.add(themeEnitiy);
		themelist.add(themeEnitiy2);
		ThemeListAdapter adapter = new ThemeListAdapter(getApplicationContext(), themelist);
		return adapter;
	}

	private void initSelfMenu(){

		selfMenu = new SlidingMenu(this);
		selfMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		selfMenu.setShadowWidthRes(R.dimen.shadow_width);
		selfMenu.setBehindOffsetRes(R.dimen.shadow_offset);
		selfMenu.setFadeDegree(0.35f);
		selfMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		selfMenu.setMenu(R.layout.self_layout);
		selfMenu.setOnOpenListener(new OnOpenListener() {
			@Override
			public void onOpen() {
				// TODO Auto-generated method stub

			}
		});
		headView = (ImageView) findViewById(R.id.iv_head);
		/**
		 * 选择头像图片
		 */
		headView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
					Toast.makeText(MainActivity.this,R.string.failed_open_photo,
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
		//处理更换头像相片请求
		if(requestCode == 49){
			Uri pictureUri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };
			CursorLoader cl = new CursorLoader(this, pictureUri, proj,null,null,null);
			Cursor cursor = cl.loadInBackground();
			cursor.moveToFirst();
			Bitmap tmp = ShowView.readBitmapAutoSize(cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATA)).toString()
					,MainActivity.Width,MainActivity.Height);
			Drawable headDrawable = new BitmapDrawable(getResources(),ShowView.toRoundCorner(tmp, 35));
			headView.setBackgroundDrawable(headDrawable);
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		LoginActivity.mXmppConnection.disconnect();
		LoginActivity.mXmppConnection = null;
		//LoginActivity.mXmppConnection2.disconnect();
		super.onDestroy();
	}

	/**
	 * 内部类 定义了请求同步IQ的Provider
	 * @author hankwing
	 *
	 */
	public class SyncTopicsIQProvider implements IQProvider{
		
		//private static final String PREFERRED_ENCODING = "UTF-8";
		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
			// TODO Auto-generated method stub
			//Log.i("message", "receive an IQ");
			int eventType = parser.getEventType();
			boolean done = false;
	        while (!done) {
	         if(eventType == XmlPullParser.TEXT) {
	        	 done = true;
	             //获得jsonData
	        	 //parser.getText();
	             topics = Util.AnalysisTopics(parser.getText());				//解析这个jsondata到数组中
	             for( int i = 0; i< topics.size() ; i++ ) {
	            	 ArrayList<HashMap<String, Object>> temp = topics.get(i);				//获得某个类下的话题
	            	 final List<ThemeEntity> themelist = new ArrayList<ThemeEntity>();
	            	 for( int j = 0; j< temp.size(); j++ ) {
	            		 HashMap<String, Object> singleTopics = temp.get(j);				//获得某个话题，下面加入话题list
	            		 Drawable drawable = getResources().getDrawable(R.drawable.theme_init_view);
	            		 drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
	            					drawable.getIntrinsicHeight());
	            		 ThemeEntity themeEnitiy = new ThemeEntity( singleTopics.get("id").toString(), (String)singleTopics.get("topics"),
	            				 (String)singleTopics.get("type"), drawable);
	            		 themelist.add(themeEnitiy);
	            	 }
	            	 final ThemeListAdapter tla = adapterslist.get(i);
	            	 //更新话题列表
	            	 MainActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							tla.updateData(themelist);
							tla.notifyDataSetChanged();
						} 
	            	 }); 	             	 
	             }
	         } 
	         eventType = parser.next();
	        }
			return null;
		}
	}
	
	/**
	 * 在有物理按键的手机上依旧显示overflow按键
	 */
	private void forceShowActionBarOverflowMenu() {  
	    try {  
	        ViewConfiguration config = ViewConfiguration.get(this);  
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");  
	        if (menuKeyField != null) {  
	            menuKeyField.setAccessible(true);  
	            menuKeyField.setBoolean(config, false);  
	        }  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}  
	

}

