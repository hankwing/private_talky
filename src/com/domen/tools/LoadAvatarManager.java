package com.domen.tools;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.domen.adapter.RoomListAdapter;
import com.domen.entity.ConstantVariable;

public class LoadAvatarManager {

	public static LoadAvatarManager sInstance;
	static final int DOWNLOAD_FAILED = -1;
    static final int DOWNLOAD_STARTED = 1;
    static final int DOWNLOAD_COMPLETE = 2;
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
	private RoomListAdapter mAdapter = null; 		//ui widget
	private ArrayList<Map<String, String>> mData = null;
	
	
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
				AvatarTask avatarTask = (AvatarTask) inputMessage.obj;
                // Gets the ImageView for this task
                switch (inputMessage.what) {
                    // The decoding is done
                    case DOWNLOAD_COMPLETE:
                        //String userBareJID = avatarTask.getBareJID();
                        VCard resultAvater = avatarTask.getVCard();
                        int pos = avatarTask.getRoomListPos();
                        Map<String, String> tempMap = mData.get(pos);
                        tempMap.put(avatarTask.mRole + ConstantVariable.Rank,
                        		resultAvater.getField(ConstantVariable.Rank));
//                        Log.i("message", "role: " + avatarTask.mRole+ ConstantVariable.Rank
//                        		+ " rank: "+ resultAvater.getField(ConstantVariable.Rank));
                        mAdapter.notifyDataSetChanged();
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

	static public void startDownload( RoomListAdapter adapter, 
			ArrayList<Map<String, String>> data , 
			String userBareJID, int roomListPos, String role ) {

		// Adds a download task to the thread pool for execution
		sInstance.mDownloadThreadPool.execute( sInstance.new AvatarTask(userBareJID, 
				roomListPos, role));
		
	}

	public void handleState( AvatarTask avatarTask, int state) {
        switch (state) {
            
            // The task finished downloading the image
            case DOWNLOAD_COMPLETE:
                mHandler.obtainMessage(state, avatarTask).sendToTarget();
                break;
            // In all other cases, pass along the message without any other action.
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
	public class AvatarTask implements Runnable{
		
		String userBareJID;
		VCard mVCard;
		LoadAvatarManager mManager;
		int roomListPos;
		String mRole;
		
		AvatarTask( String jid ,int pos, String role) {
			userBareJID = jid;
			roomListPos = pos;
			mRole = role;
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
			
			handleState(this, DOWNLOAD_COMPLETE);				//succeed
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
	
	public void setAdapter( RoomListAdapter adapter) {
		mAdapter = adapter;
	}
	
	public void setData( ArrayList<Map<String, String>> roomData) {
		mData = roomData;
	}

}
