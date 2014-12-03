package com.domen.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.domen.adapter.RoomListAdapter;
import com.domen.entity.ConstantVariable;

public class LoadAvatarManager {

	public static LoadAvatarManager sInstance;
	public static final int DOWNLOAD_FAILED = -1;
	public static final int DOWNLOAD_STARTED = 1;
	public static final int DOWNLOAD_COMPLETE = 2;
	public static final int REQUESTFROMROOMLISTACTIVITY = 3;
	public static final int REQUESTFROMOCCUPANTSACTIVITY = 4;
	public static final int REQUESTINACTIVE = 6;
	public static final int REQUESTPARSERAVATAR = 7;
	
	// A managed pool of background download threads
	private ThreadPoolExecutor mDownloadThreadPool = null;
	private Handler mHandler;
	private static int NUMBER_OF_CORES = Runtime.getRuntime()
			.availableProcessors();
	// Sets the initial threadpool size to 8
//	private static final int CORE_POOL_SIZE = 8;
//
//	// Sets the maximum threadpool size to 8
//	private static final int MAXIMUM_POOL_SIZE = 8;
	// Sets the amount of time an idle thread waits before terminating
	private static final int KEEP_ALIVE_TIME = 5;
	// Sets the Time Unit to seconds
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

	// A queue of Runnables for the image download pool
	private final BlockingQueue<Runnable> mDownloadWorkQueue;
	private BaseAdapter mAdapter = null; 		//ui widget
	private ArrayList<Map<String, String>> mData = null;
	private Map<String, String> occupantsData = null;
	
	
	// private construct method
	private LoadAvatarManager() {

		mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();
		// Creates a thread pool manager
		mDownloadThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, // Initial
																		// pool
																		// size
				NUMBER_OF_CORES, // Max pool size
				KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadWorkQueue);
		// Defines a Handler object that's attached to the UI thread
		mHandler = new Handler(Looper.getMainLooper()) {

			/*
			 * handleMessage() defines the operations to perform when the
			 * Handler receives a new Message to process.
			 */
			@Override
			public void handleMessage(Message inputMessage) {
				// Gets the task from the incoming Message object.
				
                // Gets the ImageView for this task
                switch (inputMessage.what) {
                    // The decoding is done
                    case DOWNLOAD_COMPLETE:
                    	// request from roo list activity
                        //String userBareJID = avatarTask.getBareJID();
                    	VCardTask avatarTask = (VCardTask) inputMessage.obj;
        				VCard resultAvatar = avatarTask.getVCard();
                        int pos = avatarTask.getRoomListPos();
                        Map<String, String> tempMap = mData.get(pos);
                        tempMap.put(avatarTask.mRole + ConstantVariable.Rank,
                        		resultAvatar.getField(ConstantVariable.Rank));
//                        Log.i("message", "role: " + avatarTask.mRole+ ConstantVariable.Rank
//                        		+ " rank: "+ resultAvater.getField(ConstantVariable.Rank));
                        mAdapter.notifyDataSetChanged();
                        break;
                    case REQUESTFROMOCCUPANTSACTIVITY:
                    	//request from occupants list activity
                    	VCardTask mAvatarTask = (VCardTask) inputMessage.obj;
        				VCard mResultAvatar = mAvatarTask.getVCard();
                    	occupantsData.put(mAvatarTask.mRole + ConstantVariable.Rank,
                    			mResultAvatar.getField(ConstantVariable.Rank));
                    	mAdapter.notifyDataSetChanged();
                    	break;
                    case REQUESTINACTIVE:
                    	AvatarTask inActiveTask = (AvatarTask) inputMessage.obj;
        				VCard inActiveAvatar = inActiveTask.getVCard();
        				inActiveAvatar.setNickName( inActiveTask.getNickName());
                    	//parser image data
                    	startParserAvatar(inActiveAvatar, inActiveTask.getAdapter());
                    	break;
                    case REQUESTPARSERAVATAR:
                    	//notify the adapter to refresh avatar
                    	ParserAvatarTask parserTask = (ParserAvatarTask) inputMessage.obj;
                    	parserTask.getAdapter().notifyDataSetChanged();
                    	break;
                    default:
                        /*
                         * Pass along other messages from the UI
                         */
                        super.handleMessage(inputMessage);
                }

			}

		};

	}

	static {

		// Creates a single static instance of PhotoManager
		sInstance = new LoadAvatarManager();
	}

	/**
	 * Returns the PhotoManager object
	 * 
	 * @return The global PhotoManager object
	 */
	public static LoadAvatarManager getInstance() {

		return sInstance;
	}

	/**
	 * load vcard
	 * @param adapter
	 * @param data
	 * @param userBareJID
	 * @param roomListPos
	 * @param role
	 * @param kind the kind of request code
	 */
	static public void startDownload( RoomListAdapter adapter, 
			ArrayList<Map<String, String>> data , 
			String userBareJID, int roomListPos, String role, int kind ) {

		// Adds a download task to the thread pool for execution
		sInstance.mDownloadThreadPool.execute( sInstance.new VCardTask(userBareJID, 
				roomListPos, role, kind));
		
	}
	
	/**
	 * load vcard
	 * @param adapter
	 * @param data
	 * @param userBareJID
	 * @param roomListPos
	 * @param role
	 * @param kind the kind of request code
	 */
	static public void startDownload( BaseAdapter adapter, 
			Map<String, String> data , 
			String userBareJID, int roomListPos, String role, int kind ) {

		// Adds a download task to the thread pool for execution
		sInstance.mDownloadThreadPool.execute( sInstance.new VCardTask(userBareJID, 
				roomListPos, role, kind));
		
	}
	
	/**
	 * load inactive user vcard
	 * @param adapter
	 * @param data
	 * @param userBareJID
	 * @param roomListPos
	 * @param role
	 * @param kind the kind of request code
	 */
	static public void startDownload( BaseAdapter adapter, 
			List<String> data , int kind ) {

		// Adds a download task to the thread pool for execution
		for(String temp : data) {
			sInstance.mDownloadThreadPool.execute( sInstance.new VCardTask(
					StringUtils.parseBareAddress(temp)));
		}
		
	}
	
	/**
	 * load inactive user vcard
	 * @param vcard
	 * @param adapter
	 */
	static public void startParserAvatar( VCard vcard , BaseAdapter adapter) {
		// Adds a download task to the thread pool for execution
		sInstance.mDownloadThreadPool.execute( sInstance.new ParserAvatarTask(
				null, vcard, adapter));
		
	}
	
	/**
	 * load avatar
	 * @param adapter
	 * @param data
	 * @param userBareJID
	 * @param roomListPos
	 * @param role
	 * @param kind the kind of request code
	 */
	static public void startDownloadAvatar( BaseAdapter adapter, 
			ImageView imageView , 
			String key, String bareJID, int kind ) {

		// Adds a download task to the thread pool for execution
		sInstance.mDownloadThreadPool.execute( sInstance.new AvatarTask(bareJID, 
				kind, adapter));
		
	}

	public void handleState( Runnable avatarTask, int state) {
        switch (state) {
            
            // The task finished downloading the image
            case DOWNLOAD_COMPLETE:
                mHandler.obtainMessage(state, avatarTask).sendToTarget();
                break;
            // In all other cases, pass along the message without any other action.
            case REQUESTFROMOCCUPANTSACTIVITY:
            	mHandler.obtainMessage(state, avatarTask).sendToTarget();
            	break;
            case REQUESTINACTIVE:
            	mHandler.obtainMessage(state, avatarTask).sendToTarget();
            	break;
            case REQUESTPARSERAVATAR:
            	mHandler.obtainMessage(state, avatarTask).sendToTarget();
            	break;
            default:
                mHandler.obtainMessage(state, avatarTask).sendToTarget();
                break;
        }

    }
	
	/**
	 * 加载用户资料的task
	 * @author hankwing
	 *
	 */
	public class VCardTask implements Runnable {
		
		String userBareJID;
		VCard mVCard;
		LoadAvatarManager mManager;
		int roomListPos;
		String mRole;
		int requestType;
		
		VCardTask( String jid ,int pos, String role, int kind) {
			userBareJID = jid;
			roomListPos = pos;
			mRole = role;
			requestType = kind;
			mVCard = new VCard();
		}
		
		VCardTask( String bareJID ) {
			userBareJID = bareJID;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				mVCard.load(MXMPPConnection.getInstance(), userBareJID);
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
			switch( requestType ) {
			case REQUESTFROMROOMLISTACTIVITY:
				handleState(this, DOWNLOAD_COMPLETE);				//succeed
				break;
			case REQUESTFROMOCCUPANTSACTIVITY:
				handleState(this, REQUESTFROMOCCUPANTSACTIVITY);
				break;
			case REQUESTINACTIVE:
				handleState(this, REQUESTINACTIVE);
				break;
			}
			
		}
		
		public String getBareJID() {
			return userBareJID;
		}
		
		public VCard getVCard() {
			return mVCard;
		}
		
		public int getRoomListPos() {
			return roomListPos;
		}
		
	}
	
	/**
	 * load user avatar data
	 * @author hankwing
	 *
	 */
	public class AvatarTask implements Runnable {
		
		String userBareJID;
		VCard mVCard;
		int requestType;
		BaseAdapter mAdapter;
		String nickName;
		
		AvatarTask( String jid , int kind, BaseAdapter adapter) {
			userBareJID = jid;
			nickName = StringUtils.parseName(userBareJID);
			requestType = kind;
			mAdapter = adapter;
			mVCard = new VCard();
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				mVCard.load(MXMPPConnection.getInstance(), userBareJID);
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

			handleState(this, REQUESTINACTIVE);				//succeed
			
		}
		
		public String getBareJID() {
			return userBareJID;
		}
		
		public VCard getVCard() {
			return mVCard;
		}
		
		public BaseAdapter getAdapter() {
			return mAdapter;
		}
		
		public String getNickName() {
			return nickName;
		}
		
	}
	
	/**
	 * parser user avatar data
	 * @author hankwing
	 *
	 */
	public class ParserAvatarTask implements Runnable {
		
		BitmapMemAndDiskCache cacheTools;
		Context mContext;
		VCard mVCard;
		Bitmap mBitmap;
		BaseAdapter mAdapter;
		
		ParserAvatarTask( Context context, VCard vcard, BaseAdapter adapter ) {
			mContext = context;
			cacheTools = BitmapMemAndDiskCache.getInstance(mContext);
			mVCard = vcard;
			mAdapter = adapter;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Bitmap bitmap = null;
			byte[] avatarByte = mVCard.getAvatar();
			if (bitmap == null && avatarByte != null) { // Not found in disk cache
	            // 将byte转为bitmap
	            bitmap = BitmapFactory.decodeByteArray(
						avatarByte, 0, avatarByte.length);
	        }
			if( bitmap != null) {
				// 利用VCard获得图像数据并放入内存和disk缓存中
				mBitmap = bitmap;
				cacheTools.addBitmapToCache(mVCard.getNickName(), bitmap);
			}
			handleState(this, REQUESTPARSERAVATAR);
			
		}
		
		public Bitmap getBitmap() {
			return mBitmap;
		}
		
		public BaseAdapter getAdapter() {
			return mAdapter;
		}
		
	}
	
	
	public void setAdapter( BaseAdapter adapter) {
		mAdapter = adapter;
	}
	
	public void setRoomListData( ArrayList<Map<String, String>> roomData) {
		mData = roomData;
	}
	
	public void setOccupantsListData( Map<String, String> data) {
		occupantsData = data;
	}

}
