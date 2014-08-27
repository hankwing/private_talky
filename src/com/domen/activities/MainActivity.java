package com.domen.activities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.domen.adapter.ThemeListAdapter;
import com.domen.adapter.ThemeTabAdapter;
import com.domen.entity.ThemeEntity;
import com.domen.other.Util;
import com.domen.start.LoginActivity;
import com.domen.viewsolve.ShowView;
import com.wxl.lettalk.R;

/**
 * 主界面
 * 
 * @author hankwing
 * 
 */
public class MainActivity extends FragmentActivity implements OnClickListener {

	private ViewPager themeViewPager; // 话题选择page
	private List<ThemeListAdapter> adapterslist; // 配合ViewPager的adapter
	private DrawerLayout mDrawerLayout;			//左侧滑动栏
	private ActionBarDrawerToggle mDrawerToggle;				//滑动栏触发器
	private ImageView headView;
	private ArrayList<ArrayList<HashMap<String, Object>>> topics;
	public static ProviderManager providerManager = null;
	public static float Height = 0; // 屏幕高度
	public static float Width = 0; // 屏幕宽度
	

	// 存储话题数据

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		forceShowActionBarOverflowMenu(); 				// 强制显示overFlowButton
		adapterslist = new ArrayList<ThemeListAdapter>();
		adapterslist.add(initComic());
		// 添加监听同步话题IQ包的IOProvider
		providerManager = ProviderManager.getInstance();
		providerManager.addIQProvider("json", "com:talky:syncTopics",
				new SyncTopicsIQProvider());
		DisplayMetrics Win = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(Win);
		Width = Win.widthPixels; 		// 获得屏幕宽度像素数
		Height = Win.heightPixels; 		// 获得屏幕高度像素数
		// showView = new ShowView(getApplicationContext(), this.Width,
		// this.Height);
		//initSelfMenu(); 				// 初始化侧边滑动菜单 定义一系列事件
		themeViewPager = (ViewPager) this.findViewById(R.id.theme_pager);
		//4个主题
		String[] titles = {getResources().getString(R.string.society),
						   getResources().getString(R.string.science),
						   getResources().getString(R.string.environment),
						   getResources().getString(R.string.women)};
		themeViewPager.setAdapter(new ThemeTabAdapter(this, 
				getSupportFragmentManager() , 4, titles, adapterslist));
		mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
		//添加打开关闭drawer的监听器
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(MainActivity.this.getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(getResources().getString(R.string.info));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);
        
	}

	/**
	 * 加载action bar
	 */
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
		// case R.id.tv_main_self:
		// //selfMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		// //暂时先放同步功能
		// RequestSync rs = new RequestSync();
		// rs.setType(IQ.Type.GET);
		// LoginActivity.mXmppConnection.sendPacket(rs);
		//
		// break;
		default:
			break;
		}
	}

	/**
	 * 添加话题	之后改为从数据库查找
	 * @return
	 */
	public ThemeListAdapter initComic() {

		Drawable drawable = getResources().getDrawable(
				R.drawable.theme_init_view);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());

		// 一个话题就是一个Entity
		ThemeEntity themeEnitiy = new ThemeEntity("0", "你好徐梦雪", "健康类",
				drawable);

		List<ThemeEntity> themelist = new ArrayList<ThemeEntity>();
		themelist.add(themeEnitiy);

		ThemeListAdapter adapter = new ThemeListAdapter(
				getApplicationContext(), themelist);
		return adapter;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		// 处理更换头像相片请求
		if (requestCode == 49) {
			Uri pictureUri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };
			CursorLoader cl = new CursorLoader(this, pictureUri, proj, null,
					null, null);
			Cursor cursor = cl.loadInBackground();
			cursor.moveToFirst();
			Bitmap tmp = ShowView
					.readBitmapAutoSize(
							cursor.getString(
									cursor.getColumnIndex(MediaStore.Images.Media.DATA))
									.toString(), MainActivity.Width,
							MainActivity.Height);
			Drawable headDrawable = new BitmapDrawable(getResources(),
					ShowView.toRoundCorner(tmp, 35));
			headView.setBackgroundDrawable(headDrawable);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		LoginActivity.mXmppConnection.disconnect();
		LoginActivity.mXmppConnection = null;
		// LoginActivity.mXmppConnection2.disconnect();
		super.onDestroy();
	}

	/**
	 * 内部类 定义了请求同步IQ的Provider
	 * 
	 * @author hankwing
	 * 
	 */
	public class SyncTopicsIQProvider implements IQProvider {

		// private static final String PREFERRED_ENCODING = "UTF-8";
		@Override
		public IQ parseIQ(XmlPullParser parser) throws Exception {
			// TODO Auto-generated method stub
			// Log.i("message", "receive an IQ");
			int eventType = parser.getEventType();
			boolean done = false;
			while (!done) {
				if (eventType == XmlPullParser.TEXT) {
					done = true;
					// 获得jsonData
					// parser.getText();
					topics = Util.AnalysisTopics(parser.getText()); // 解析这个jsondata到数组中
					for (int i = 0; i < topics.size(); i++) {
						ArrayList<HashMap<String, Object>> temp = topics.get(i); // 获得某个类下的话题
						final List<ThemeEntity> themelist = new ArrayList<ThemeEntity>();
						for (int j = 0; j < temp.size(); j++) {
							HashMap<String, Object> singleTopics = temp.get(j); // 获得某个话题，下面加入话题list
							Drawable drawable = getResources().getDrawable(
									R.drawable.theme_init_view);
							drawable.setBounds(0, 0,
									drawable.getIntrinsicWidth(),
									drawable.getIntrinsicHeight());
							ThemeEntity themeEnitiy = new ThemeEntity(
									singleTopics.get("id").toString(),
									(String) singleTopics.get("topics"),
									(String) singleTopics.get("type"), drawable);
							themelist.add(themeEnitiy);
						}
						final ThemeListAdapter tla = adapterslist.get(i);
						// 更新话题列表
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
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 调用invalidateOptionsMenu()后触发此方法
	 */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        
        return super.onPrepareOptionsMenu(menu);
    }
    
    /**
     * 在activity运行中 配置改变时 同时改变toggle的配置
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    /**
     * 在恢复activity时同时恢复drawer的状态
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    /**
     * 菜单点击事件
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		//如果在drawer显示的情况下点击了item,那么就触发该item
		if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		return super.onOptionsItemSelected(item);
	}

}
