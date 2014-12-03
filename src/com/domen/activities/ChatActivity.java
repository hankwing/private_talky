package com.domen.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.domen.adapter.ChatMsgAdapter;
import com.domen.entity.MsgEntity;
import com.domen.openfire.SyncAgreeAndShit;
import com.domen.start.R;
import com.domen.tools.BitmapMemAndDiskCache;
import com.domen.tools.CurrentActivity;
import com.domen.tools.MUserChatManager;
import com.domen.tools.MXMPPConnection;
import com.domen.tools.MXMPPConnection.Login;
import com.domen.tools.TopicsContract.TopicsEntryContract;

/**
 * 聊天窗口 重要
 * 
 * @author hankwing
 * 
 */
public class ChatActivity extends Activity implements OnClickListener {

	// private Chat chat;
	public MultiUserChat chat = null;
	private RelativeLayout bottomBar;
	private static int side; // 0-观众 1-正方 -1-反方
	// static MultiUserChat chat2 = null;
	private ImageView btn_send; // 发送按钮
	private EditText edt_message; // 用户聊天内容输入框
	private ListView listView; // 聊天显示listview
	// private TextView tv_theme; //话题名称
	// private ImageButton btn_ranks; //显示排名
	// private Boolean isPositive; //是否是正方观点
	// private PopupWindow rankWindow;
	// private ImageButton btn_add_image; //添加图片按钮
	// private ImageButton btn_add_face; //add facial
	// private ImageButton btn_add_record; //add sound record
	// private ImageButton ibtn_back; //return button
	private InputMethodManager imm;
	// private ImageView page0; //表情页下的小点点
	// private ImageView page1; //2
	// private ImageView page2; //3
	// private GridView gView1; //表情页1
	// private GridView gView2; //2
	// private GridView gView3; //3
	// private ArrayList<GridView> grids; //表情页容器
	// private ViewPager facePager; //表情页的viewPager
	// private int[] expressionImages;
	// private String[] expressionImageNames;
	// private int[] expressionImages1;
	// private String[] expressionImageNames1;
	// private int[] expressionImages2;
	// private String[] expressionImageNames2;
	// private LinearLayout page_select; //表情页面下小点点的布局
	// private String faceString;
	// private boolean isJudge = false; //判断用户是否点击了点赞和点屎 如果没有 避免多余操作
	private static ArrayList<String> userList = null; // 房间内用户列表
	private List<MsgEntity> msgList = new ArrayList<MsgEntity>(); // 聊天记录容器
	private ChatMsgAdapter chatMsgAdapter; // 聊天记录listView的adapter
	private String UserFullId;
	private static Map<String, Integer> userAgreeCache = null; // 缓存用户点赞数据
	private static Map<String, Integer> userShitCache = null; // 缓存用户点屎数据
	private static int topicID;
	private String topicName;
	private static String roomJID = null;
	private Map<String, VCard> vcardList = null;
	private SharedPreferences accountInfo;
	private String nickName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		CurrentActivity.setCurrentActivity(this);
		getActionBar().setDisplayHomeAsUpEnabled(true); // 显示返回上级的按钮
		getActionBar().setDisplayShowTitleEnabled(true);
		setContentView(R.layout.activity_chat);
		accountInfo = getSharedPreferences("accoutInfo", Context.MODE_PRIVATE);
		UserFullId = accountInfo.getString("userFullId", null);
		nickName = accountInfo.getString("account", null);
		if (savedInstanceState == null) {
			Bundle bundle = new Bundle();
			Intent intent = this.getIntent();
			bundle = intent.getExtras();
			topicName = bundle
					.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME);
			side = bundle.getInt("side");
			topicID = Integer.valueOf(bundle
					.getString(TopicsEntryContract.COLUMN_NAME_OF_ID));
			roomJID = bundle.getString("roomJID");
			new JoinRoomAndAddListener().execute(0); // do not need join room
		} else {
			// rebuild activity
			topicName = savedInstanceState
					.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME);
			side = savedInstanceState.getInt("side");
			topicID = savedInstanceState
					.getInt(TopicsEntryContract.COLUMN_NAME_OF_ID);
			roomJID = savedInstanceState.getString("roomJID");
			// login
			new Login().execute(accountInfo.getString("account", null),
					accountInfo.getString("password", null), Build.MODEL);
			new JoinRoomAndAddListener().execute(1); // need join room

		}

		getActionBar().setTitle(topicName); // 显示话题名称

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		bottomBar = (RelativeLayout) this
				.findViewById(R.id.activity_chat_bottom);
		listView = (ListView) this.findViewById(R.id.chat_list);
		edt_message = (EditText) this.findViewById(R.id.chat_message);
		edt_message.setOnClickListener(this);
		btn_send = (ImageView) this.findViewById(R.id.btn_send);
		btn_send.setOnClickListener(this);
		chatMsgAdapter = new ChatMsgAdapter(getApplicationContext(), msgList,
				side == 0 ? true : false);
		listView.setAdapter(chatMsgAdapter); // 定义适配器
		listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				// Log.i("message", "click!");
				chatMsgAdapter.setSelectedPosition(arg2); // set position

			}

		});

		// initViewPager(); //初始化表情选择界面
		imm.hideSoftInputFromWindow(edt_message.getWindowToken(), 0);
		vcardList = chatMsgAdapter.getVcardList();

		if (side == 0) {
			// 旁观者 不能发言
			bottomBar.setVisibility(View.GONE);
			showMessageHistory(); // 显示聊天历史记录
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// case R.id.btn_add_record:
		// //进入录音界面
		// Intent intent = new Intent(ChatActivity.this,RecordActivity.class);
		// startActivityForResult(intent, 28);
		// break;
		// case R.id.btn_add_face:
		// //打开表情选择窗口
		// facePager.setVisibility(View.VISIBLE);
		// page_select.setVisibility(View.VISIBLE);
		// break;
		// case R.id.ibt_ranks:
		// Intent ranksIntent = new Intent(ChatActivity.this,
		// RanksActivity.class);
		// startActivity(ranksIntent);
		// break;
		// case R.id.btn_add_image:
		// String status = Environment.getExternalStorageState();
		// if (status.equals(Environment.MEDIA_MOUNTED))
		// {
		// Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
		// getIntent.setType("image/*");
		// if (getIntent != null)
		// {
		// startActivityForResult(getIntent, 7);
		// }
		// } else
		// {
		// Toast.makeText(ChatActivity.this,
		// getResources().getString(R.string.failed_open_photo),
		// Toast.LENGTH_SHORT).show();
		// }
		// break;
		// case R.id.chat_message:
		// //控制表情界面的消失与显示
		// facePager.setVisibility(View.GONE);
		// page_select.setVisibility(View.GONE);
		// break;
		case R.id.btn_send:
			// 发送消息
			String messageString = edt_message.getText().toString();
			if (messageString == null || messageString.equals("")) {
				break;
			}
			updateMesList(MXMPPConnection.getInstance().getUser(), nickName,
					messageString, false); // 自己发出的信息右侧
			// 下面向服务器发送信息
			try {
				Message message = chat.createMessage();
				message.setBody(edt_message.getText().toString());
				// 添加消息包的附加消息
				DefaultPacketExtension extension = new DefaultPacketExtension(
						"message", "com:talky:message");
				extension.setValue("side", String.valueOf(side));
				message.addExtension(extension);
				chat.sendMessage(message);
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			edt_message.setText(""); // 清空编辑框
			break;
		// case R.id.ibtn_chat_back:
		// AlertDialog dialog = new AlertDialog.Builder(
		// ChatActivity.this)
		// .setTitle("点屎")
		// .setMessage("呵呵")
		// .setPositiveButton("ȷ��", new DialogInterface.OnClickListener()
		// {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which)
		// {
		// Intent intent = new Intent(getApplicationContext(),
		// CountActivity.class);
		// startActivity(intent);
		// ChatActivity.this.finish();
		// }
		// })
		// .setNegativeButton("�˳�", new DialogInterface.OnClickListener()
		// {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which)
		// {
		// // TODO Auto-generated method stub
		// ChatActivity.this.finish();
		// }
		// }).create();
		// dialog.show();
		// break;

		}
	}

	public void updateMesList(String userJID, String nickName, String content,
			boolean isLeft) {

		MsgEntity msgEnitiy = new MsgEntity();
		msgEnitiy.setContent(content);
		msgEnitiy.setName(nickName); // 设置nickName
		msgEnitiy.setUserJID(userJID);
		// Toast.makeText(getApplicationContext(), content,
		// Toast.LENGTH_SHORT).show();
		msgEnitiy.setDate(getTime());
		// msgEnitiy.setName(MXMPPConnection.getInstance().getUser());
		// Drawable head =
		// getResources().getDrawable(R.drawable.default_avatar);
		// msgEnitiy.setHead(head);
		msgEnitiy.setIsLeft(isLeft);
		msgList.add(msgEnitiy);
		chatMsgAdapter.notifyDataSetChanged(); // 通知适配器界面刷新

	}

	/**
	 * 获得系统时间
	 * 
	 * @return
	 */
	public String getTime() {
		String time = "";
		Calendar cal = Calendar.getInstance();
		String year = String.valueOf(cal.get(Calendar.YEAR));
		String month = String.valueOf(cal.get(Calendar.MONTH));
		String day = String.valueOf(cal.get(Calendar.DATE));
		time = year + "/" + month + "/" + day + "/";
		return time;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			new ConfirmQuitDialogFragment().show(getFragmentManager(),
					"ConfirmQuitDialogFragment");
			return true;
		case R.id.action_show_occupants:
			// perform showing pccupants
			MUserChatManager.setChat(chat);
			Intent intent = new Intent(this, ShowOccupantsActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("roomJID", roomJID);
			bundle.putString(TopicsEntryContract.COLUMN_NAME_OF_ID, ""
					+ topicID);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	//
	// public PopupWindow ranksPopupWindow(Context context){
	// rankWindow = new PopupWindow(context);
	// View conterView = LayoutInflater.from(this).inflate(R.layout.rank_layout,
	// null);
	// rankWindow.setContentView(conterView);
	// rankWindow.setWidth(LayoutParams.WRAP_CONTENT);
	// rankWindow.setHeight(LayoutParams.WRAP_CONTENT);
	// return rankWindow;
	// }

	// private void initViewPager() {
	//
	// expressionImages = Expressions.expressionImgs;
	// expressionImageNames = Expressions.expressionImgNames;
	// expressionImages1 = Expressions.expressionImgs1;
	// //expressionImageNames1 = Expressions.expressionImgNames1;
	// expressionImages2 = Expressions.expressionImgs2;
	// //expressionImageNames2 = Expressions.expressionImgNames2;
	//
	// LayoutInflater inflater = LayoutInflater.from(this);
	// grids = new ArrayList<GridView>();
	// gView1 = (GridView) inflater.inflate(R.layout.grid1, null);
	// List<Map<String, Object>> listItems = new ArrayList<Map<String,
	// Object>>();
	//
	// for (int i = 0; i < 24; i++)
	// {
	// Map<String, Object> listItem = new HashMap<String, Object>();
	// listItem.put("image", expressionImages[i]);
	// listItems.add(listItem);
	// }
	//
	// SimpleAdapter simpleAdapter = new SimpleAdapter(ChatActivity.this,
	// listItems,
	// R.layout.face_layout, new String[] { "image" },
	// new int[] { R.id.faceImage });
	// gView1.setAdapter(simpleAdapter);
	// gView1.setOnItemClickListener(new OnItemClickListener() {
	// @Override
	// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
	// long arg3) {
	// Bitmap bitmap = null;
	// bitmap = BitmapFactory.decodeResource(getResources(),
	// expressionImages[arg2 % expressionImages.length]);
	// ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap,
	// ImageSpan.ALIGN_BASELINE);
	//
	// faceString = "Face:"
	// + expressionImageNames[arg2].substring(1,
	// expressionImageNames[arg2].length() - 1);
	//
	// SpannableString spannableString = new SpannableString(faceString);
	//
	// spannableString.setSpan(imageSpan, 0, faceString.length(),
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	//
	// SetCursor(spannableString);
	//
	// }
	// });
	//
	// grids.add(gView1);
	//
	// gView2 = (GridView) inflater.inflate(R.layout.grid2, null);
	// grids.add(gView2);
	//
	// gView3 = (GridView) inflater.inflate(R.layout.grid3, null);
	// grids.add(gView3);
	//
	// PagerAdapter mPagerAdapter = new PagerAdapter() {
	// @Override
	// public boolean isViewFromObject(View arg0, Object arg1) {
	// return arg0 == arg1;
	// }
	//
	// @Override
	// public int getCount() {
	// return grids.size();
	// }
	//
	// @Override
	// public void destroyItem(View container, int position, Object object) {
	// ((ViewPager) container).removeView(grids.get(position));
	// }
	//
	// @Override
	// public Object instantiateItem(View container, int position) {
	// ((ViewPager) container).addView(grids.get(position));
	// return grids.get(position);
	// }
	// };
	//
	// facePager.setAdapter(mPagerAdapter);
	//
	// facePager.setOnPageChangeListener(new GuidePageChangeListener());
	// }

	// class GuidePageChangeListener implements OnPageChangeListener {
	//
	// @Override
	// public void onPageScrollStateChanged(int arg0) {
	//
	// }
	//
	// @Override
	// public void onPageScrolled(int arg0, float arg1, int arg2) {
	// }
	//
	// public void onPageSelected(int arg0) {
	// switch (arg0)
	// {
	// case 0:
	// page0.setImageDrawable(getResources().getDrawable(
	// R.drawable.page_focused));
	// page1.setImageDrawable(getResources().getDrawable(
	// R.drawable.page_unfocused));
	// break;
	// case 1:
	// page1.setImageDrawable(getResources().getDrawable(
	// R.drawable.page_focused));
	// page0.setImageDrawable(getResources().getDrawable(
	// R.drawable.page_unfocused));
	// page2.setImageDrawable(getResources().getDrawable(
	// R.drawable.page_unfocused));
	// List<Map<String, Object>> listItems = new ArrayList<Map<String,
	// Object>>();
	//
	// for (int i = 0; i < 24; i++)
	// {
	// Map<String, Object> listItem = new HashMap<String, Object>();
	// listItem.put("image", expressionImages1[i]);
	// listItems.add(listItem);
	// }
	//
	// SimpleAdapter simpleAdapter = new SimpleAdapter(ChatActivity.this,
	// listItems, R.layout.face_layout,
	// new String[] { "image" }, new int[] { R.id.faceImage });
	// gView2.setAdapter(simpleAdapter);
	// gView2.setOnItemClickListener(new OnItemClickListener() {
	// @Override
	// public void onItemClick(AdapterView<?> arg0, View arg1,
	// int arg2, long arg3) {
	// Bitmap bitmap = null;
	// bitmap = BitmapFactory.decodeResource(getResources(),
	// expressionImages1[arg2
	// % expressionImages1.length]);
	// ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap,
	// ImageSpan.ALIGN_BASELINE);
	//
	// faceString = "Face:"
	// + expressionImageNames[arg2].substring(1,
	// expressionImageNames[arg2].length() - 1);
	//
	// SpannableString spannableString = new SpannableString(faceString);
	//
	// spannableString.setSpan(imageSpan, 0, faceString.length(),
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	//
	// SetCursor(spannableString);
	// }
	// });
	// break;
	// case 2:
	// page2.setImageDrawable(getResources().getDrawable(
	// R.drawable.page_focused));
	// page1.setImageDrawable(getResources().getDrawable(
	// R.drawable.page_unfocused));
	// page0.setImageDrawable(getResources().getDrawable(
	// R.drawable.page_unfocused));
	// List<Map<String, Object>> listItems1 = new ArrayList<Map<String,
	// Object>>();
	//
	// for (int i = 0; i < 24; i++)
	// {
	// Map<String, Object> listItem = new HashMap<String, Object>();
	// listItem.put("image", expressionImages2[i]);
	// listItems1.add(listItem);
	// }
	//
	// SimpleAdapter simpleAdapter1 = new SimpleAdapter(ChatActivity.this,
	// listItems1, R.layout.face_layout,
	// new String[] { "image" }, new int[] { R.id.faceImage });
	// gView3.setAdapter(simpleAdapter1);
	// gView3.setOnItemClickListener(new OnItemClickListener() {
	// @Override
	// public void onItemClick(AdapterView<?> arg0, View arg1,
	// int arg2, long arg3) {
	// Bitmap bitmap = null;
	// bitmap = BitmapFactory.decodeResource(getResources(),
	// expressionImages2[arg2
	// % expressionImages2.length]);
	//
	// ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap,
	// ImageSpan.ALIGN_BASELINE);
	// faceString = "Face:"
	// + expressionImageNames[arg2].substring(1,
	// expressionImageNames[arg2].length() - 1);
	//
	// SpannableString spannableString = new SpannableString(faceString);
	//
	// spannableString.setSpan(imageSpan, 0, faceString.length(),
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	//
	// SetCursor(spannableString);
	// }
	// });
	// break;
	//
	// }
	// }
	// }

	// /* (non-Javadoc)
	// * @see android.app.Activity#onActivityResult(int, int,
	// android.content.Intent)
	// */
	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// // TODO Auto-generated method stub
	// super.onActivityResult(requestCode, resultCode, data);
	// if(requestCode == 7 && resultCode != RESULT_CANCELED){
	//
	// Uri pictureUri = data.getData();
	// String[] proj = { MediaStore.Images.Media.DATA };
	// CursorLoader cl = new CursorLoader(this, pictureUri,
	// proj,null,null,null);
	// Cursor cursor = cl.loadInBackground();
	// cursor.moveToFirst();
	// Bitmap tmp = ShowView.readBitmapAutoSize(cursor.getString(cursor
	// .getColumnIndex(MediaStore.Images.Media.DATA)).toString()
	// ,MainActivity.Width,MainActivity.Height);
	//
	// @SuppressWarnings("deprecation")
	// Drawable image = new BitmapDrawable(tmp);
	// MsgEntity msgEnitiy = new MsgEntity();
	// msgEnitiy.setDate(getTime());
	// msgEnitiy.setName(LoginActivity.mXmppConnection.getUser());
	// Drawable head = getResources().getDrawable(R.drawable.icon_temp_head);
	// msgEnitiy.setHead(head);
	// msgEnitiy.setIsText(false);
	// msgEnitiy.setIsPositive(isPositive);
	// msgEnitiy.setImage(image);
	// msgList.add(msgEnitiy);
	// chatMsgAdapter.notifyDataSetChanged();
	// edt_message.setText("");
	// listView.setSelection(listView.getCount() - 1);
	//
	// }
	// if(requestCode == 28 && resultCode != RESULT_CANCELED){
	// Toast.makeText(getApplicationContext(), "cancle",
	// Toast.LENGTH_SHORT).show();
	// }
	// }

	// /* (non-Javadoc)
	// * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	// */
	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// page_select.setVisibility(View.GONE);
	// facePager.setVisibility(View.GONE);
	// return super.onTouchEvent(event);
	// }

	// public void SetCursor(SpannableString span) {
	// int start = edt_message.getSelectionStart();
	// Editable mbody = edt_message.getText();
	// mbody.insert(start, span);
	//
	// }

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		new ConfirmQuitDialogFragment().show(getFragmentManager(),
				"ConfirmQuitDialogFragment");
		// super.onBackPressed();

	}

	public static class ConfirmQuitDialogFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.quitchat)
					.setPositiveButton(R.string.dialog_positive,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// FIRE ZE MISSILES!

									if (side == 0) {
										// 发送用户点赞和点屎的数据
										SyncAgreeAndShit sync = new SyncAgreeAndShit(
												topicID, roomJID, userList,
												userAgreeCache, userShitCache);
										sync.setType(IQ.Type.GET);
										MXMPPConnection.sendPacket(
												getActivity(), sync);
									}

									getActivity().finish();

								}
							})
					.setNegativeButton(R.string.dialog_negative,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
									dismiss();
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

	/**
	 * 初始化用户点赞和点屎缓存
	 */
	public static void initUserCache(ArrayList<String> userlist) {
		userList = userlist;
		userAgreeCache = new HashMap<String, Integer>();
		userShitCache = new HashMap<String, Integer>();
		for (int i = 0; i < userlist.size(); i++) {
			userAgreeCache.put(userlist.get(i), 0);
			userShitCache.put(userlist.get(i), 0);
		}

	}

	/**
	 * 往用户jid上增加点赞次数
	 * 
	 * @param jid
	 */
	public static void addUserAgree(String jid) {

		Integer before = userAgreeCache.get(jid);
		before++;
		userAgreeCache.put(jid, before);
	}

	public static void addUserShit(String jid) {
		Integer before = userShitCache.get(jid);
		before++;
		userShitCache.put(jid, before);
	}

	public class LoadAvatar extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			VCard vcard = new VCard();

			try {
				vcard.load(MXMPPConnection.getInstance(), params[0]);
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
			vcardList.put(params[1], vcard);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			chatMsgAdapter.notifyDataSetChanged(); // 通知适配器界面刷新
			super.onPostExecute(result);
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		clearReferences();
		try {
			chat.leave();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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

	/**
	 * 销毁activity时保存状态
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub

		outState.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
				topicName);
		outState.putInt(TopicsEntryContract.COLUMN_NAME_OF_ID, topicID);
		outState.putString("roomJID", roomJID);
		outState.putInt("side", side);
		super.onSaveInstanceState(outState);

	}

	/**
	 * join and add listener
	 * 
	 * @author hankwing
	 * 
	 */
	public class JoinRoomAndAddListener extends
			AsyncTask<Integer, Void, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			if (params[0] == 1) {

				// wait for logging
				synchronized (MXMPPConnection.loginLock) {
					// Wait while logging
					while (!MXMPPConnection.isLogin) {
						try {
							MXMPPConnection.loginLock.wait();
						} catch (InterruptedException e) {
						}
					}
				}

			}

			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			chat = new MultiUserChat(MXMPPConnection.getInstance(), roomJID);
			try {
				chat.join(MXMPPConnection.getInstance().getUser());
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

			if (chat != null && chat.isJoined()) {
				// 添加监听器
				// 监听房间内成员的状态
				chat.addParticipantStatusListener(new ParticipantStatusListener() {

					@Override
					public void adminGranted(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void adminRevoked(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void banned(String arg0, String arg1, String arg2) {
						// TODO Auto-generated method stub

					}

					@Override
					public void joined(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void kicked(String arg0, String arg1, String arg2) {
						// TODO Auto-generated method stub

					}

					@Override
					public void left(final String arg0) {
						// TODO Auto-generated method stub
						// 成员离开时 如果自己或者房间内无成员 就退出聊天
						ChatActivity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								String userJID = arg0.substring(arg0
										.indexOf("/") + 1);
								String userName = userJID.substring(0,
										userJID.indexOf("@"));
								Toast.makeText(ChatActivity.this,
										"用户 " + userName + " 离开房间了",
										Toast.LENGTH_SHORT).show();
							}

						});

						// if (arg0.equals(UserInfo.roomJID + "/" +
						// UserInfo.fullUserJID)) {
						// // 自己退出
						// finish();
						// } else if (chat.getOccupantsCount() == 1) {
						// // 房间内无成员
						// try {
						// chat.leave();
						// } catch (NotConnectedException e) {
						// // TODO Auto-generated catch block
						// e.printStackTrace();
						// }
						// Toast.makeText(ChatActivity.this,
						// getResources().getString(R.string.no_memeber),
						// Toast.LENGTH_SHORT).show();
						// finish();
						// }
						// try {
						// Log.i("message", "occupation left!");
						// chat.leave();
						// finish();
						// } catch (NotConnectedException e) {
						// // TODO Auto-generated catch block
						// e.printStackTrace();
						// }

					}

					@Override
					public void membershipGranted(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void membershipRevoked(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void moderatorGranted(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void moderatorRevoked(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void nicknameChanged(String arg0, String arg1) {
						// TODO Auto-generated method stub

					}

					@Override
					public void ownershipGranted(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void ownershipRevoked(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void voiceGranted(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void voiceRevoked(String arg0) {
						// TODO Auto-generated method stub

					}

				});

				if (side == 0) {
					// 旁观者的消息监听器
					chat.addMessageListener(new PacketListener() {

						@Override
						public void processPacket(final Packet arg0) {
							// TODO Auto-generated method stub
							// 接收到消息 应该更新界面
							final Message msg = (Message) arg0;
							final String messageJID = msg.getFrom().substring(
									msg.getFrom().indexOf("/") + 1);
							String bareJID = messageJID.substring(0,
									messageJID.indexOf("/"));

							final String messageNickName = messageJID
									.substring(0, messageJID.indexOf("@"));
							if (!vcardList.containsKey(messageNickName)) {
								// 加入vcardList
								LoadAvatar loadVCard = new LoadAvatar();
								// 清理sd卡上的avatar缓存
								BitmapMemAndDiskCache.getInstance(
										ChatActivity.this)
										.removeBitmapFromCache(messageNickName);
								loadVCard.execute(bareJID, messageNickName);
							}
							ChatActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub

									DefaultPacketExtension extension = msg
											.getExtension("message",
													"com:talky:message");
									int isLeft = Integer.valueOf(extension
											.getValue("side"));
									// Log.i("message", "side: " + isLeft);

									// 房间内其他成员发来的信息 应该更新界面
									updateMesList(messageJID, messageNickName,
											msg.getBody(), isLeft == 1 ? true
													: false);

								}

							});
						}

					});
				} else {
					// 参与者的消息监听器
					chat.addMessageListener(new PacketListener() {

						@Override
						public void processPacket(Packet arg0) {
							// TODO Auto-generated method stub
							// 接收到消息 应该更新界面
							final Message msg = (Message) arg0;
							final String messageJID = msg.getFrom().substring(
									msg.getFrom().indexOf("/") + 1);
							// Log.i("message", "JID：" + messageJID);
							String bareJID = messageJID.substring(0,
									messageJID.indexOf("/"));
							final String messageNickName = messageJID
									.substring(0, messageJID.indexOf("@"));
							if (!vcardList.containsKey(messageNickName)) {
								// 加入vcardList
								LoadAvatar loadVCard = new LoadAvatar();
								BitmapMemAndDiskCache.getInstance(
										ChatActivity.this)
										.removeBitmapFromCache(messageNickName);
								loadVCard.execute(bareJID, messageNickName);
							}
							ChatActivity.this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub

									if (!msg.getFrom().equals(
											roomJID + "/" + UserFullId)) {

										// 房间内其他成员发来的信息 应该更新界面
										updateMesList(messageJID,
												messageNickName, msg.getBody(),
												true); // 收到消息更新界面

									}

								}

							});
						}

					});
				}

			}
		}

	}

	/**
	 * 旁观者显示聊天历史记录
	 */
	public void showMessageHistory() {
		Message msg = null;
		msg = chat.pollMessage();
		while (msg != null) {
			// show in activity
			final String messageJID = msg.getFrom().substring(
					msg.getFrom().indexOf("/") + 1);
			String bareJID = messageJID.substring(0, messageJID.indexOf("/"));
			final String messageNickName = messageJID.substring(0,
					messageJID.indexOf("@"));
			if (!vcardList.containsKey(messageNickName)) {
				// 加入vcardList
				LoadAvatar loadVCard = new LoadAvatar();
				loadVCard.execute(bareJID, messageNickName);
			}
			final DefaultPacketExtension extension = msg.getExtension(
					"message", "com:talky:message");
			final String body = msg.getBody();
			ChatActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					int isLeft = Integer.valueOf(extension.getValue("side"));
					// Log.i("message", "side: " + isLeft);

					// 房间内其他成员发来的信息 应该更新界面
					updateMesList(messageJID, messageNickName, body,
							isLeft == 1 ? true : false);

				}

			});
			msg = chat.pollMessage();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat_activity_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

}
