package com.domen.lettalk;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;


import com.domen.adapter.ChatMsgAdapter;
import com.domen.entity.MsgEntity;
import com.domen.other.Expressions;
import com.domen.start.LoginActivity;
import com.domen.viewsolve.ShowView;
import com.wxl.lettalk.R;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ChatActivity extends Activity{

	Chat chat;

	Button btn_send;
	EditText edt_message;
	ListView listView;
	TextView tv_theme;
	ImageButton btn_ranks;
	Boolean isPositive;
	PopupWindow rankWindow;
	ImageButton btn_add_image;
	ImageButton btn_add_face;
	ImageButton btn_add_record;
	ImageButton ibtn_back;
	
	private InputMethodManager imm;

	//����
	private ImageView page0;
	private ImageView page1;
	private ImageView page2;
	private GridView gView1;
	private GridView gView2;
	private GridView gView3;
	private ArrayList<GridView> grids;
	private ViewPager facePager;
	private int[] expressionImages;
	private String[] expressionImageNames;
	private int[] expressionImages1;
	private String[] expressionImageNames1;
	private int[] expressionImages2;
	private String[] expressionImageNames2;
	private LinearLayout page_select;
	private String faceString;

	//����һ��list���淢�͵���Ϣ����Ϣ������ MsgEntity
	List<MsgEntity> msgList = new ArrayList<MsgEntity>();
	ChatMsgAdapter chatMsgAdapter;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_chat);


		Bundle bundle = new Bundle();
		Intent intent = this.getIntent();
		bundle = intent.getExtras();

		if(bundle.get("side").equals("positive")){
			isPositive = true;
		}else{
			isPositive = false;
		}

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		
		tv_theme =(TextView) this.findViewById(R.id.chat_theme);
		listView =(ListView) this.findViewById(R.id.chat_list);
		edt_message = (EditText) this.findViewById(R.id.chat_message);
		btn_send = (Button) this.findViewById(R.id.btn_send);
		btn_ranks = (ImageButton) this.findViewById(R.id.ibt_ranks);
		tv_theme.setText(bundle.getString("theme"));
		btn_add_image = (ImageButton) this.findViewById(R.id.btn_add_image);
		btn_add_face = (ImageButton) this.findViewById(R.id.btn_add_face);
		btn_add_record = (ImageButton) this.findViewById(R.id.btn_add_record);
		facePager = (ViewPager) findViewById(R.id.vip_face);
		page0 = (ImageView) findViewById(R.id.page0_select);
		page1 = (ImageView) findViewById(R.id.page1_select);
		page2 = (ImageView) findViewById(R.id.page2_select);
		page_select = (LinearLayout) findViewById(R.id.page_select);
		//���ص����˵�
		ibtn_back = (ImageButton) findViewById(R.id.ibtn_chat_back);



		chatMsgAdapter = new ChatMsgAdapter(getApplicationContext(),msgList);

		listView.setAdapter(chatMsgAdapter);

		initViewPager();

		btn_add_record.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ChatActivity.this,RecordActivity.class);
				startActivityForResult(intent, 28);
			}
		});

		btn_add_face.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				facePager.setVisibility(View.VISIBLE);
				page_select.setVisibility(View.VISIBLE);
			}
		});


		btn_ranks.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ChatActivity.this, RanksActivity.class);
				startActivity(intent);

			}
		});

		btn_add_image.setOnClickListener(new OnClickListener() {

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
						startActivityForResult(intent, 7);
					}
				} else
				{
					Toast.makeText(ChatActivity.this,"�򲻿�",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		edt_message.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				facePager.setVisibility(View.GONE);
				page_select.setVisibility(View.GONE);
			}
		});



		btn_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//����һ���µ�Message
				MsgEntity msgEnitiy = new MsgEntity();
				String content = "";
				content = edt_message.getText().toString();
				msgEnitiy.setContent(content);
				Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
				msgEnitiy.setDate(getTime());
				msgEnitiy.setName("Legend");
				Drawable head = getResources().getDrawable(R.drawable.icon_temp_head);
				msgEnitiy.setHead(head);
				msgEnitiy.setIsPositive(isPositive);
				/*//�ж��Ƿ���
				if(!"".equals(content)){
					try {
						chat.sendMessage(content);
						edt_message.setText("");

					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/

				//��ʾ��view��
				msgList.add(msgEnitiy);
				chatMsgAdapter.notifyDataSetChanged();
				edt_message.setText("");	
				listView.setSelection(listView.getCount() - 1);

			}
		});

		ibtn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog dialog = new AlertDialog.Builder(
						ChatActivity.this)
				.setTitle("�˳���ʾ")
				.setMessage("�������û�н����˳�����ܵ��ͷ�")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						Intent intent = new Intent(getApplicationContext(), CountActivity.class);
						startActivity(intent);
						ChatActivity.this.finish();

					}
				})
				.setNegativeButton("�˳�", new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// TODO Auto-generated method stub
						ChatActivity.this.finish();
					}
				}).create();
				dialog.show();
			}
		});

		imm.hideSoftInputFromWindow(edt_message.getWindowToken(), 0);
		
	}

	public String getTime(){
		String time="";
		Calendar cal = Calendar.getInstance();
		String year = String.valueOf(cal.get(Calendar.YEAR));
		String month = String.valueOf(cal.get(Calendar.MONTH));
		String day = String.valueOf(cal.get(Calendar.DATE));
		time = year + "��" + month + "��" + day + "��";
		return time;

	}

	public PopupWindow ranksPopupWindow(Context context){
		rankWindow = new PopupWindow(context);
		View conterView = LayoutInflater.from(this).inflate(R.layout.rank_layout, null);
		rankWindow.setContentView(conterView);
		rankWindow.setWidth(LayoutParams.WRAP_CONTENT);
		rankWindow.setHeight(LayoutParams.WRAP_CONTENT);
		return rankWindow;
	}


	//��ʼ������Paper
	private void initViewPager() {

		expressionImages = Expressions.expressionImgs;
		expressionImageNames = Expressions.expressionImgNames;
		expressionImages1 = Expressions.expressionImgs1;
		expressionImageNames1 = Expressions.expressionImgNames1;
		expressionImages2 = Expressions.expressionImgs2;
		expressionImageNames2 = Expressions.expressionImgNames2;

		LayoutInflater inflater = LayoutInflater.from(this);
		grids = new ArrayList<GridView>();
		gView1 = (GridView) inflater.inflate(R.layout.grid1, null);
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		// ����24������

		for (int i = 0; i < 24; i++)
		{
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("image", expressionImages[i]);
			listItems.add(listItem);
		}

		SimpleAdapter simpleAdapter = new SimpleAdapter(ChatActivity.this, listItems,
				R.layout.face_layout, new String[] { "image" },
				new int[] { R.id.faceImage });
		gView1.setAdapter(simpleAdapter);
		gView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Bitmap bitmap = null;
				bitmap = BitmapFactory.decodeResource(getResources(),
						expressionImages[arg2 % expressionImages.length]);
				ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap,
						ImageSpan.ALIGN_BASELINE);

				faceString = "Face:"
						+ expressionImageNames[arg2].substring(1,
								expressionImageNames[arg2].length() - 1);

				SpannableString spannableString = new SpannableString(faceString);

				spannableString.setSpan(imageSpan, 0, faceString.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				SetCursor(spannableString);

			}
		});

		grids.add(gView1);

		gView2 = (GridView) inflater.inflate(R.layout.grid2, null);
		grids.add(gView2);

		gView3 = (GridView) inflater.inflate(R.layout.grid3, null);
		grids.add(gView3);

		// ���ViewPager������������
		PagerAdapter mPagerAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return grids.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager) container).removeView(grids.get(position));
			}

			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager) container).addView(grids.get(position));
				return grids.get(position);
			}
		};

		facePager.setAdapter(mPagerAdapter);

		facePager.setOnPageChangeListener(new GuidePageChangeListener());
	}

	class GuidePageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void onPageSelected(int arg0) {
			switch (arg0)
			{
			case 0:
				page0.setImageDrawable(getResources().getDrawable(
						R.drawable.page_focused));
				page1.setImageDrawable(getResources().getDrawable(
						R.drawable.page_unfocused));
				break;
			case 1:
				page1.setImageDrawable(getResources().getDrawable(
						R.drawable.page_focused));
				page0.setImageDrawable(getResources().getDrawable(
						R.drawable.page_unfocused));
				page2.setImageDrawable(getResources().getDrawable(
						R.drawable.page_unfocused));
				List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
				// ����24������
				for (int i = 0; i < 24; i++)
				{
					Map<String, Object> listItem = new HashMap<String, Object>();
					listItem.put("image", expressionImages1[i]);
					listItems.add(listItem);
				}

				SimpleAdapter simpleAdapter = new SimpleAdapter(ChatActivity.this,
						listItems, R.layout.face_layout,
						new String[] { "image" }, new int[] { R.id.faceImage });
				gView2.setAdapter(simpleAdapter);
				gView2.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Bitmap bitmap = null;
						bitmap = BitmapFactory.decodeResource(getResources(),
								expressionImages1[arg2
								                  % expressionImages1.length]);
						ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap,
								ImageSpan.ALIGN_BASELINE);

						faceString = "Face:"
								+ expressionImageNames[arg2].substring(1,
										expressionImageNames[arg2].length() - 1);

						SpannableString spannableString = new SpannableString(faceString);

						spannableString.setSpan(imageSpan, 0, faceString.length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

						SetCursor(spannableString);
					}
				});
				break;
			case 2:
				page2.setImageDrawable(getResources().getDrawable(
						R.drawable.page_focused));
				page1.setImageDrawable(getResources().getDrawable(
						R.drawable.page_unfocused));
				page0.setImageDrawable(getResources().getDrawable(
						R.drawable.page_unfocused));
				List<Map<String, Object>> listItems1 = new ArrayList<Map<String, Object>>();
				// ����24������
				for (int i = 0; i < 24; i++)
				{
					Map<String, Object> listItem = new HashMap<String, Object>();
					listItem.put("image", expressionImages2[i]);
					listItems1.add(listItem);
				}

				SimpleAdapter simpleAdapter1 = new SimpleAdapter(ChatActivity.this,
						listItems1, R.layout.face_layout,
						new String[] { "image" }, new int[] { R.id.faceImage });
				gView3.setAdapter(simpleAdapter1);
				gView3.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Bitmap bitmap = null;
						bitmap = BitmapFactory.decodeResource(getResources(),
								expressionImages2[arg2
								                  % expressionImages2.length]);

						ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap,
								ImageSpan.ALIGN_BASELINE);
						faceString = "Face:"
								+ expressionImageNames[arg2].substring(1,
										expressionImageNames[arg2].length() - 1);

						SpannableString spannableString = new SpannableString(faceString);

						spannableString.setSpan(imageSpan, 0, faceString.length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

						SetCursor(spannableString);
					}
				});
				break;

			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 7 && resultCode != RESULT_CANCELED){

			Uri pictureUri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = managedQuery(pictureUri, proj, null, null, null);
			cursor.moveToFirst();

			//��ȡͼƬ
			Bitmap tmp = ShowView.readBitmapAutoSize(cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATA)).toString()
					,MainActivity.Width,MainActivity.Height);

			Drawable image = new BitmapDrawable(tmp);

			//�����µ�����
			MsgEntity msgEnitiy = new MsgEntity();
			msgEnitiy.setDate(getTime());
			msgEnitiy.setName("Legend");
			Drawable head = getResources().getDrawable(R.drawable.icon_temp_head);
			msgEnitiy.setHead(head);
			msgEnitiy.setIsText(false);
			msgEnitiy.setIsPositive(isPositive);
			msgEnitiy.setImage(image);

			/*//�ж��Ƿ���
			if(!"".equals(content)){
				try {
					chat.sendMessage(content);
					edt_message.setText("");

				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/

			//��ʾ��view��
			msgList.add(msgEnitiy);
			chatMsgAdapter.notifyDataSetChanged();
			edt_message.setText("");	
			listView.setSelection(listView.getCount() - 1);

		}
		if(requestCode == 28 && resultCode != RESULT_CANCELED){
			Toast.makeText(getApplicationContext(), "¼��", Toast.LENGTH_SHORT).show();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		page_select.setVisibility(View.GONE);
		facePager.setVisibility(View.GONE);
		return super.onTouchEvent(event);
	}
	

	/**
	 * ��EditText����ʾ��������
	 * @param span
	 */
	public void SetCursor(SpannableString span) {
		int start = edt_message.getSelectionStart();
		Editable mbody = edt_message.getText();
		mbody.insert(start, span);

	}
	
	/**
	 * �����۽�����ʱ����е������
	 */
	
	
}
