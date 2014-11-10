package com.domen.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.ping.packet.Ping;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.domen.adapter.ThemeListAdapter;
import com.domen.adapter.ThemeTabAdapter;
import com.domen.adapter.ThemeTabAdapter.ThemeFragment;
import com.domen.openfire.MultipleLoginPresenceListener;
import com.domen.openfire.RequestSync;
import com.domen.tools.BitmapMemAndDiskCache;
import com.domen.tools.BitmapTool;
import com.domen.tools.CurrentActivity;
import com.domen.tools.JsonUtil;
import com.domen.tools.MXMPPConnection;
import com.domen.tools.TopicDatabaseOpenHelper;
import com.wxl.lettalk.R;

/**
 * 主界面
 * 
 * @author hankwing
 * 
 */
public class MainActivity extends FragmentActivity implements OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	public static final int CAPTURE_PICTURE = 1;
	public static final int CHOOSE_FROM_GALLERY = 2;
	public static final int GALLERY_URL = 10; // query code
	public static final String LOG_TAG = "mainActivity";
	private static ViewPager themeViewPager; // 话题选择page
	private ThemeTabAdapter themeTabAdapter;
	private static List<ThemeListAdapter> adapterslist; // 配合ViewPager的adapter
	private DrawerLayout mDrawerLayout; // 左侧滑动栏
	private ActionBarDrawerToggle mDrawerToggle; // 滑动栏触发器

	private ImageView drawerAvatar; // 用户头像
	private TextView drawerUserName; // 用户名
	private TextView drawerRank; // 等级
	private TextView drawerSubTitle; // 称号
	private TextView drawerFavour; // 被赞
	private TextView drawerShit; // 被屎

	private ArrayList<HashMap<String, Object>> topics;
	public static float Height = 0; // 屏幕高度
	public static float Width = 0; // 屏幕宽度
	private TopicDatabaseOpenHelper dbOpenHelper = null;
	private static SharedPreferences sharedPref;
	public static SharedPreferences accountInfo;

	private int favour; // 从服务器获取的用户被赞被屎的次数
	private int shit;
	private BitmapMemAndDiskCache avatarCache = null;
	private VCard mVCard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		avatarCache = BitmapMemAndDiskCache.getInstance(this); // 获得缓存实例
		
		forceShowActionBarOverflowMenu(); // 强制显示overFlowButton
		sharedPref = getPreferences(Context.MODE_PRIVATE); // 保存刷新时间 用户账号信息等
		accountInfo = getSharedPreferences("accoutInfo", Context.MODE_PRIVATE); // 保存用户资料的preference

		dbOpenHelper = TopicDatabaseOpenHelper.getInstance(this); // 获得数据库helper
		if (savedInstanceState != null) {
			// 应用被销毁并重建时ping主机
			CurrentActivity.setCurrentActivity(this);
			Ping pingIQ = new Ping();
			MXMPPConnection.sendPacket(this, pingIQ);
		} else {
			// 从login 或者 register 转到main 里 添加roaster监听器
			CurrentActivity.setCurrentActivity(this);
			Roster mRoster = MXMPPConnection.getInstance().getRoster();
			mRoster.addRosterListener(new MultipleLoginPresenceListener());
		}
		getLoaderManager().initLoader(0, null, this).forceLoad(); // 初始化话题获取Loader
		getLoaderManager().initLoader(1, null, this).forceLoad(); // 初始化话题获取Loader
		getLoaderManager().initLoader(2, null, this).forceLoad(); // 初始化话题获取Loader
		getLoaderManager().initLoader(3, null, this).forceLoad(); // 初始化话题获取Loader
		adapterslist = new ArrayList<ThemeListAdapter>(); // 适配器list
															// 传给tabAdapter
		adapterslist.add(new ThemeListAdapter(this, null, 0));
		adapterslist.add(new ThemeListAdapter(this, null, 0));
		adapterslist.add(new ThemeListAdapter(this, null, 0));
		adapterslist.add(new ThemeListAdapter(this, null, 0));
		// 添加监听同步话题IQ包的IOProvider
		ProviderManager.addIQProvider("json", "com:talky:syncTopics",
				new SyncTopicsIQProvider());
		DisplayMetrics Win = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(Win);
		Width = Win.widthPixels; // 获得屏幕宽度像素数
		Height = Win.heightPixels; // 获得屏幕高度像素数
		// showView = new ShowView(getApplicationContext(), this.Width,
		// this.Height);
		// initSelfMenu(); // 初始化侧边滑动菜单 定义一系列事件
		themeViewPager = (ViewPager) this.findViewById(R.id.theme_pager);
		// 4个主题
		String[] titles = { getResources().getString(R.string.society),
				getResources().getString(R.string.science),
				getResources().getString(R.string.environment),
				getResources().getString(R.string.women) };
		themeTabAdapter = new ThemeTabAdapter(this,
				getSupportFragmentManager(), 4, titles, adapterslist);
		themeViewPager.setAdapter(themeTabAdapter);

		/**
		 * 超过固定时间 则自动刷新话题列表
		 */
		themeViewPager.setOnPageChangeListener(new OnPageChangeListener() {

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
				long currentTime = System.currentTimeMillis();
				long previousTime = sharedPref.getLong("refreshTime" + arg0, 0);
				if (previousTime == 0 || currentTime - previousTime > 300000) {
					// 刷新
					SwipeRefreshListFragmentFragment page = (SwipeRefreshListFragmentFragment) themeViewPager
							.getAdapter().instantiateItem(themeViewPager, arg0);
					page.setRefreshing(true); // 开始刷新
					page.getOnFreshListener().onRefresh();

				}
			}

		});

		mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
		drawerAvatar = (ImageView) findViewById(R.id.drawer_avatar);
		drawerUserName = (TextView) findViewById(R.id.drawer_username);
		drawerRank = (TextView) findViewById(R.id.drawer_rank);
		drawerSubTitle = (TextView) findViewById(R.id.drawer_subtitle);
		drawerFavour = (TextView) findViewById(R.id.drawer_favour);
		drawerShit = (TextView) findViewById(R.id.drawer_shit);
		// 暂时由客户端指定用户信息
		drawerUserName.setText(accountInfo.getString("account", null)); // 得到用户名
		drawerRank.setText("1级");
		drawerSubTitle.setText("牙牙学语");
		drawerFavour.setText("0次");
		drawerShit.setText("0次");

		drawerAvatar.setOnClickListener(this); // 头像点击监听器
		mVCard = new VCard();
		if (MXMPPConnection.getInstance() != null) {
			// 有连接 获得用户的VCard
			try {
				mVCard.load(MXMPPConnection.getInstance());
			} catch (NoResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMPPErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		avatarCache.loadAvatarBitmap(accountInfo.getString("account", null),
				mVCard, drawerAvatar, true);

		// 添加打开关闭drawer的监听器
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getActionBar().setTitle(MainActivity.this.getTitle());
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()

			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar()
						.setTitle(getResources().getString(R.string.info));
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// if restart then login
		// if (savedInstanceState != null
		// && savedInstanceState.getBoolean("restart")) {
		// // Log.i("message", "account: " + sharedPref.getString("account",
		// // null));
		// new Login().execute(accountInfo.getString("account", null),
		// accountInfo.getString("password", null));
		// }
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
		case R.id.drawer_avatar:
			XMPPTCPConnection mConnection = MXMPPConnection.getInstance();
			if (mConnection == null) {
				// 无连接
				Toast.makeText(this,
						getResources().getString(R.string.internet_failure),
						Toast.LENGTH_SHORT).show();
			} else {
				// VCard mVCard = new VCard();
				// 获得头像的bitmap对象
				// Bitmap defaultAvatar = BitmapFactory.
				// decodeResource(getResources(), R.drawable.default_avatar);
				// ByteArrayOutputStream stream = new ByteArrayOutputStream();
				// //格式化输出到stream
				// defaultAvatar.compress(Bitmap.CompressFormat.JPEG, 100,
				// stream);

				new SelectAvatarDialog().show(getFragmentManager(),
						"chooseWhatWay");
				// 暂且放上传头像的代码
				// mVCard.setJabberId(MXMPPConnection.getInstance().getUser());
				// try {
				// // vcard.setAvatar(new URL( "http://img2.ph.126.net/5VnOqN" +
				// // "ppTtF_45pMyK9gqQ==/6619174348421271779.jpg"));
				// // vcard.setField("avatarURL",
				// "http://img2.ph.126.net/5VnOqN" +
				// // "ppTtF_45pMyK9gqQ==/6619174348421271779.jpg");
				// Bitmap defaultAvatar = BitmapFactory.
				// decodeResource(getResources(), R.drawable.default_avatar);
				// ByteArrayOutputStream stream = new ByteArrayOutputStream();
				//
				// //格式化输出到stream
				// defaultAvatar.compress(Bitmap.CompressFormat.JPEG, 100,
				// stream);
				// byte[] avatarByte = stream.toByteArray();
				// mVCard.setAvatar(avatarByte);
				// mVCard.save(MXMPPConnection.getInstance());
				// }catch (NoResponseException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (XMPPErrorException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (NotConnectedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}
			break;
		default:
			break;
		}
	}

	public void processStartElement(XmlPullParser xpp) {
		String name = xpp.getName();
		String uri = xpp.getNamespace();
		if ("".equals(uri)) {
			System.out.println("Start element: " + name);
		} else {
			System.out.println("Start element: {" + uri + "}" + name);
		}
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
			String requestType = null; // 请求类型
			int position = 0; // 适配器编号
			String topicType = null;
			while (!done) {
				if (eventType == XmlPullParser.START_TAG) {
					if (parser.getName().equals("json")) {
						requestType = parser.getAttributeValue(0);
						position = Integer.valueOf(parser.getAttributeValue(1));
						topicType = parser.getAttributeValue(2);
						if ("society".equals(topicType)) {
							// 如果是第一个界面 则获得个人被赞被屎数据
							favour = Integer.valueOf(parser
									.getAttributeValue(3));
							shit = Integer.valueOf(parser.getAttributeValue(4));
						}
					}
				} else if (eventType == XmlPullParser.TEXT) {
					done = true;
					// 获得jsonData
					// parser.getText();
					topics = JsonUtil.AnalysisTopics(parser.getText(),
							requestType); // 解析这个jsondata到数组中
					dbOpenHelper.updateTopics(topics, topicType); // 将新话题数据存入数据库中
					getLoaderManager().restartLoader(position, null,
							MainActivity.this).forceLoad(); // 重新获取刷新后的话题数据
					// // 更新话题列表
					MainActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (favour != 0 || shit != 0) {
								// 如果是第一个界面 则进行刷新个人被赞被屎数据
								drawerFavour.setText(favour + "次");
								drawerShit.setText(shit + "次");
							}
							SwipeRefreshListFragmentFragment page = (SwipeRefreshListFragmentFragment) themeViewPager
									.getAdapter().instantiateItem(
											themeViewPager,
											themeViewPager.getCurrentItem());
							page.setRefreshing(false); // 停止界面刷新

						}
					});
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
		// If the nav drawer is open, hide action items related to the content
		// view

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
		// 如果在drawer显示的情况下点击了item,那么就触发该item
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_quit:
			 new LogOut().execute(); //断开连接后退出应用
			
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class SwipeRefreshListFragmentFragment extends ThemeFragment {

		private String topicType;
		private int requestType;
		private int position;
		private OnRefreshListener mOnRefreshListener;
		private int itemPosition;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle bundle = getArguments();
			topicType = bundle.getString(RequestSync.TOPICTYPE);
			itemPosition = bundle.getInt("itemPosition");
			requestType = bundle.getInt(RequestSync.REQUESTTYPE);
			position = bundle.getInt(RequestSync.POSITION);

			mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					int i = themeViewPager.getCurrentItem(); // 获取当前的话题类别
					long currentTime = System.currentTimeMillis();
					long previousTime = sharedPref
							.getLong("refreshTime" + i, 0);
					if (previousTime == 0
							|| currentTime - previousTime > 300000) {
						// 第一次刷新
						RequestSync rs = new RequestSync(topicType,
								requestType, position);
						rs.setType(IQ.Type.GET);
						// 发送IQ包
						MXMPPConnection.sendPacket(getActivity(), rs);
						sharedPref.edit()
								.putLong("refreshTime" + i, currentTime)
								.commit();
					} else {
						setRefreshing(false);
					}

				}
			};
			// Notify the system to allow an options menu for this fragment.
		}

		public OnRefreshListener getOnFreshListener() {
			return mOnRefreshListener;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			// 添加监听器
			setListAdapter(adapterslist.get(itemPosition));
			setOnRefreshListener(mOnRefreshListener);
			// END_INCLUDE (setup_refreshlistener)
			// 检查话题更新
			long currentTime = System.currentTimeMillis();
			long previousTime = sharedPref.getLong("refreshTime" + 0, 0);
			if (previousTime == 0 || currentTime - previousTime > 300000) {
				// 刷新第一个屏幕
				setRefreshing(true); // 开始刷新
				getOnFreshListener().onRefresh();

			}
		}
	}

	/**
	 * 得到AsyncTaskLoader 加载话题数据
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		switch (id) {
		case GALLERY_URL:
			String[] projection = { MediaStore.Images.Media.DATA };
			Uri myUri = Uri.parse(args.getString("uri"));
			return new CursorLoader(this, // Parent activity context
					myUri, // Table to query
					projection, // Projection to return
					null, // No selection clause
					null, // No selection arguments
					null // Default sort order
			);
		default:
			return new TopicDatabaseOpenHelper.TopicsLoader(this, id);
		}

	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		// 处理返回的结果cursor;
		switch (arg0.getId()) {
		case GALLERY_URL:
			int column_index = arg1
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			arg1.moveToFirst();
			File f = new File(arg1.getString(column_index));
			Bitmap bm = BitmapTool.decodeSampledBitmapFromFile(f, 65, 65);
			saveAvatar(bm);

			break;
		default:
			adapterslist.get(arg0.getId()).changeCursor(arg1);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case GALLERY_URL:

			break;
		default:
			adapterslist.get(arg0.getId()).swapCursor(null);
		}

	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		// ping to server
		Ping pingIQ = new Ping();
		MXMPPConnection.sendPacket(this, pingIQ);
	}

	/**
	 * 出现选择用户头像的获取方式的对话框
	 * 
	 * @author hankwing
	 * 
	 */
	public static class SelectAvatarDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			final CharSequence[] items = { "Take Photo", "Choose from Library",
					"Cancel" };

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					if (items[item].equals("Take Photo")) {
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						File f = new File(android.os.Environment
								.getExternalStorageDirectory(), "temp.jpg");
						intent.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(f));
						getActivity().startActivityForResult(intent,
								CAPTURE_PICTURE);
					} else if (items[item].equals("Choose from Library")) {
						Intent intent = new Intent(
								Intent.ACTION_PICK,
								android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						intent.setType("image/*");
						getActivity().startActivityForResult(
								Intent.createChooser(intent, "Select File"),
								CHOOSE_FROM_GALLERY);
					} else if (items[item].equals("Cancel")) {
						dialog.dismiss();
					}
				}
			});
			Dialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(true); // 设置点击屏幕其他部分消失
			return dialog;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == CAPTURE_PICTURE) {
				// 拍照返回
				File f = new File(Environment.getExternalStorageDirectory()
						.toString());
				for (File temp : f.listFiles()) {
					if (temp.getName().equals("temp.jpg")) {
						f = temp;
						break;
					}
				}
				try {

					Bitmap bm = BitmapTool.decodeSampledBitmapFromFile(f, 65,
							65);
					// 存储到VCard中 并且加载在界面上
					saveAvatar(bm);
					// 保存到外存上
					String path = android.os.Environment
							.getExternalStorageDirectory()
							+ File.separator
							+ "Avatar" + File.separator + "default";
					f.delete();
					OutputStream fOut = null;
					File file = new File(path, String.valueOf(System
							.currentTimeMillis()) + ".jpg");
					try {
						fOut = new FileOutputStream(file);
						bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
						fOut.flush();
						fOut.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requestCode == CHOOSE_FROM_GALLERY) {
				// 从相册返回
				Uri url = data.getData();
				Bundle bundle = new Bundle();
				bundle.putString("uri", url.toString());
				if (getLoaderManager().getLoader(GALLERY_URL) != null) {
					getLoaderManager().restartLoader(GALLERY_URL, bundle, this)
							.forceLoad();
				} else {
					getLoaderManager().initLoader(GALLERY_URL, bundle, this)
							.forceLoad();
				}

			}
		}
	}

	/**
	 * 存储用户头像到VCard 返回值代表修改成功与否
	 */
	public boolean saveAvatar(Bitmap avatar) {
		XMPPTCPConnection connection = MXMPPConnection.getInstance();
		if (connection != null) {
			// 连接正常
			VCard mVCard = new VCard();
			try {
				mVCard.load(connection);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				avatar.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				mVCard.setAvatar(stream.toByteArray());
				mVCard.save(connection);
				// 加载头像到界面上并缓存
				avatarCache.loadAvatarBitmap(
						accountInfo.getString("account", null), mVCard,
						drawerAvatar, true);
			} catch (NoResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMPPErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} else {
			Toast.makeText(this,
					getResources().getString(R.string.avatar_failure),
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	/**
	 * 登出线程
	 * 
	 * @author hankwing
	 * 
	 */
	public class LogOut extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			final XMPPTCPConnection mXmppConnection = MXMPPConnection
					.getInstance();
			if (mXmppConnection != null && mXmppConnection.isConnected()) {
				try {
					mXmppConnection.disconnect();
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // 断开连接
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			MainActivity.this.finish();
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		clearReferences();
		new LogOut().execute(); // 断开连接后退出应用
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
		CurrentActivity.setCurrentActivity(this);
		
	}

	/**
	 * 清除当前界面标记
	 */
	private void clearReferences() {
		Activity currActivity = CurrentActivity.getCurrentActivity();
		if (currActivity != null && currActivity.equals(this))
			CurrentActivity.setCurrentActivity(null);
	}

}
