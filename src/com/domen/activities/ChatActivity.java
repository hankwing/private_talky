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
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
 * chat activity. when user leave , active the 
 * service to listen for new message
 * 
 * @author hankwing
 * 
 */
public class ChatActivity extends Activity implements OnClickListener {

	// private Chat chat;
	public MultiUserChat chat = null;
	private RelativeLayout bottomBar;
	private static int side; // 0-audience 1-positive -1-negative
	// static MultiUserChat chat2 = null;
	private ImageView btn_send; // send button
	private EditText edt_message; // chat content editView
	private ListView listView; // show chat messages listview
	private InputMethodManager imm;
	private static ArrayList<String> userList = null; // room active occupants list
	private List<MsgEntity> msgList = new ArrayList<MsgEntity>(); // list for messages
	private ChatMsgAdapter chatMsgAdapter; // chat messages list's adapter
	private String UserFullId;
	private static Map<String, Integer> userAgreeCache = null; // cache favour data
	private static Map<String, Integer> userShitCache = null; // cache shit data
	private static int topicID;
	private String topicName;
	private static String roomJID = null;
	private Map<String, VCard> vcardList = null;
	private SharedPreferences accountInfo;
	private String nickName;
	private List<String> notSuerMessageIDs = null;		//message id waiting for confirm
	private List<String> failedMessages = null;
	private Handler failedMessageHandler = null;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		CurrentActivity.setCurrentActivity(this);
		getActionBar().setDisplayHomeAsUpEnabled(true); // show return to home button in actionbar
		getActionBar().setDisplayShowTitleEnabled(true);
		setContentView(R.layout.activity_chat);
		accountInfo = getSharedPreferences("accoutInfo", Context.MODE_PRIVATE);
		UserFullId = accountInfo.getString("userFullId", null);
		nickName = accountInfo.getString("account", null);
		notSuerMessageIDs = new ArrayList<String>();
		failedMessages = new ArrayList<String>();
		//control failed message
		failedMessageHandler = new Handler() {

			@Override
			public void handleMessage(android.os.Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				//Log.i("message", "message failed");
				String messageID = (String) msg.obj;
				if( notSuerMessageIDs.remove(messageID) ) {
					failedMessages.add(messageID);
					chatMsgAdapter.notifyDataSetChanged();
				}
				
			}
			
		};
		boolean isFromNotify = getIntent().getExtras().getBoolean("returnFromNotification");
		if( isFromNotify ) {
			// return from background
			NotificationManager mNotificationManager = 
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(1);				//hide the notification
			Intent intent = this.getIntent();
			Bundle bundle = intent.getExtras();
			topicName = bundle
					.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME);
			side = bundle.getInt("side");
			topicID = Integer.valueOf(bundle
					.getString(TopicsEntryContract.COLUMN_NAME_OF_ID));
			roomJID = bundle.getString("roomJID");

			if( MXMPPConnection.getInstance() == null || 
					MXMPPConnection.getInstance().getUser() == null) {
				new Login().execute(accountInfo.getString("account", null),
						accountInfo.getString("password", null), Build.MODEL);
				new JoinRoomAndAddListener().execute(1); // need join room
			}
			else {
				new JoinRoomAndAddListener().execute(0); // do not need join room
			}
			
		}
		else if (savedInstanceState != null) {
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
		} else {
			Intent intent = this.getIntent();
			Bundle bundle = intent.getExtras();
			topicName = bundle
					.getString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME);
			side = bundle.getInt("side");
			topicID = Integer.valueOf(bundle
					.getString(TopicsEntryContract.COLUMN_NAME_OF_ID));
			roomJID = bundle.getString("roomJID");
			new JoinRoomAndAddListener().execute(0); // do not need join room

		}

		getActionBar().setTitle(topicName); // show topic's title

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		bottomBar = (RelativeLayout) this
				.findViewById(R.id.activity_chat_bottom);
		listView = (ListView) this.findViewById(R.id.chat_list);
		edt_message = (EditText) this.findViewById(R.id.chat_message);
		edt_message.setOnClickListener(this);
		btn_send = (ImageView) this.findViewById(R.id.btn_send);
		btn_send.setOnClickListener(this);
		chatMsgAdapter = new ChatMsgAdapter(getApplicationContext(), msgList,
				side == 0 ? true : false, notSuerMessageIDs, failedMessages);
		listView.setAdapter(chatMsgAdapter);
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

		if (side == 0) {
			// the thrid party , do not have speak right
			bottomBar.setVisibility(View.GONE);
			
		}
		// initViewPager(); // init emoji choose window
		imm.hideSoftInputFromWindow(edt_message.getWindowToken(), 0);
		vcardList = chatMsgAdapter.getVcardList();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_send:
			// send message
			String messageString = edt_message.getText().toString();
			if (messageString == null || messageString.equals("")) {
				break;
			}	
			// send message to server
			try {
				Message message = chat.createMessage();
				message.setBody(edt_message.getText().toString());
				// add message extension
				DefaultPacketExtension extension = new DefaultPacketExtension(
						"message", "com:talky:message");
				extension.setValue("side", String.valueOf(side));
				notSuerMessageIDs.add(message.getPacketID());
				message.addExtension(extension);
				
				DeliveryReceiptManager.addDeliveryReceiptRequest(message);
				chat.sendMessage(message);
				failedMessageHandler.sendMessageDelayed(
						failedMessageHandler.obtainMessage(0, message.getPacketID()),
						10000);
				updateMesList( UserFullId, nickName,
						messageString, false, message.getPacketID(), message); // show in the right side
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				Toast.makeText(this, getResources().getString(R.string.internet_failure), 
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				Toast.makeText(this, getResources().getString(R.string.internet_failure), 
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			edt_message.setText(""); // clear edit view
			break;

		}
	}

	/**
	 * add message to window
	 * @param userJID
	 * @param nickName
	 * @param content
	 * @param isLeft	is the message should show in the left or not
	 * @param messageID
	 */
	public void updateMesList(String userJID, String nickName, String content,
			boolean isLeft, String messageID, Message message) {

		MsgEntity msgEnitiy = new MsgEntity();
		msgEnitiy.setContent(content);
		msgEnitiy.setName(nickName); // set nickName
		msgEnitiy.setUserJID(userJID);
		// Toast.makeText(getApplicationContext(), content,
		// Toast.LENGTH_SHORT).show();
		msgEnitiy.setDate(getTime());
		// msgEnitiy.setName(MXMPPConnection.getInstance().getUser());
		// Drawable head =
		// getResources().getDrawable(R.drawable.default_avatar);
		// msgEnitiy.setHead(head);
		msgEnitiy.setIsLeft(isLeft);
		msgEnitiy.setMessageID(messageID);
		msgEnitiy.setMessage(message);
		msgList.add(msgEnitiy);
		chatMsgAdapter.notifyDataSetChanged(); // refresh ui

	}

	/**
	 * obtain system calendar
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
			MUserChatManager listData = new MUserChatManager();
			listData.setData(chat.getOccupants());
			
			Intent intent = new Intent(this, ShowOccupantsActivity.class);
			intent.putExtra("occupantsList", listData);
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

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		new ConfirmQuitDialogFragment().show(getFragmentManager(),
				"ConfirmQuitDialogFragment");
		// super.onBackPressed();

	}

	/**
	 * confirm to quit and send data to server
	 * @author hankwing
	 *
	 */
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

									if (side == 0) {
										// send the audience favours and shit data
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
	 * init favour and shit data list
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
	 * add favour to some user
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
			chatMsgAdapter.notifyDataSetChanged(); // refresh ui
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
	 * clear mark
	 */
	private void clearReferences() {
		Activity currActivity = CurrentActivity.getCurrentActivity();
		if (currActivity != null && currActivity.equals(this))
			CurrentActivity.setCurrentActivity(null);
	}

	/**
	 * save state when destroyed
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
				// add listener for occupants state and message listener

				showMessageHistory(); // show history message
				//enable delivery manager
				DeliveryReceiptManager.getInstanceFor(MXMPPConnection.getInstance()).
				addReceiptReceivedListener(new ReceiptReceivedListener()
				{

					@Override
					public void onReceiptReceived(String fromJid, String toJid,
							String receiptId) {
						// TODO Auto-generated method stub
						if( !StringUtils.parseResource(fromJid).equals(UserFullId)) {
							//confirm
							if(notSuerMessageIDs.remove(receiptId)) {
								//modify succeed
								ChatActivity.this.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										chatMsgAdapter.notifyDataSetChanged();
									}
									
								});
							}	
							
						}
					}
				        
				});
				
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
					// message listener set for audiences
					chat.addMessageListener(new PacketListener() {

						@Override
						public void processPacket(final Packet arg0) {
							// TODO Auto-generated method stub
							// receive messages
							final Message msg = (Message) arg0;
							final String messageJID = msg.getFrom().substring(
									msg.getFrom().indexOf("/") + 1);
							String bareJID = messageJID.substring(0,
									messageJID.indexOf("/"));

							final String messageNickName = messageJID
									.substring(0, messageJID.indexOf("@"));
							if (!vcardList.containsKey(messageNickName)) {
								// add to vcardList
								LoadAvatar loadVCard = new LoadAvatar();
								// clear avatar data in sdcard
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

									// other occupants messages, should show in the ui
									updateMesList(messageJID, messageNickName,
											msg.getBody(), isLeft == 1 ? true
													: false, null, null);

								}

							});
						}

					});
				} else {
					// listener for the occupants
					chat.addMessageListener(new PacketListener() {

						@Override
						public void processPacket(Packet arg0) {
							// TODO Auto-generated method stub
							
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

										updateMesList(messageJID,
												messageNickName, msg.getBody(),
												true, msg.getPacketID(), null);
										

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
	 * show history messages
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
				// add to vcardList
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

					updateMesList(messageJID, messageNickName, body,
							isLeft == 1 ? true : false, null, null);

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
