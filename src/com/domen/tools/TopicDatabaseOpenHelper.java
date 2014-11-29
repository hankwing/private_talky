package com.domen.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.domen.tools.TopicsContract.TopicsEntryContract;

public class TopicDatabaseOpenHelper extends SQLiteOpenHelper {

	private static TopicDatabaseOpenHelper mInstance;
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "talky_topics.db";
	private static final String DATE_TYPE = " TIMESTAMP";
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	// 创建表的sql语句
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
			+ TopicsEntryContract.TABLE_NAME + " (" + TopicsEntryContract._ID
			+ " INTEGER PRIMARY KEY autoincrement,"
			+ TopicsEntryContract.COLUMN_NAME_OF_ID + INTEGER_TYPE + COMMA_SEP
			+ TopicsEntryContract.COLUMN_NAME_TOPIC_NAME + TEXT_TYPE
			+ COMMA_SEP + TopicsEntryContract.COLUMN_NAME_TOPIC_TYPE
			+ TEXT_TYPE + COMMA_SEP + TopicsEntryContract.COLUMN_NAME_TOPIC_URL
			+ TEXT_TYPE + COMMA_SEP
			+ TopicsEntryContract.COLUMN_NAME_TOPIC_POSITIVE + TEXT_TYPE
			+ COMMA_SEP + TopicsEntryContract.COLUMN_NAME_TOPIC_NEGATIVE
			+ TEXT_TYPE + COMMA_SEP
			+ TopicsEntryContract.COLUMN_NAME_TOPIC_DESC + TEXT_TYPE
			+ COMMA_SEP + TopicsEntryContract.COLUMN_NAME_TOPIC_DATE
			+ DATE_TYPE +
			// Any other options for the CREATE command
			" )";
	// 删除表的SQL语句
	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ TopicsEntryContract.TABLE_NAME;
	private static final String SQL_DELETE_TOPICS_PRE = "DELETE FROM "
			+ TopicsEntryContract.TABLE_NAME + " WHERE "
			+ TopicsEntryContract.COLUMN_NAME_TOPIC_TYPE + " = ";
	private static SQLiteDatabase mDb = null;
	private final static Semaphore semp = new Semaphore(0); // 为了保证数据库打开后才进行取话题数据的信号量

	/**
	 * 获得实例
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized TopicDatabaseOpenHelper getInstance(
			final Context context) {
		if (mInstance == null) {
			mInstance = new TopicDatabaseOpenHelper(context);
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mDb = mInstance.getWritableDatabase();
					semp.release(4);
				}

			}).start();
		}
		return mInstance;
	}

	TopicDatabaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// Log.i("message", "create: " + SQL_CREATE_ENTRIES);
		db.execSQL(SQL_CREATE_ENTRIES);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	/**
	 * 当服务端发来话题数据时 清空有关话题数据库 重新写入数据
	 * 
	 * @param topics
	 * @param topicType
	 */
	public void updateTopics(final ArrayList<HashMap<String, Object>> topics,
			final String topicType) {

		final String SQL_DELETE = SQL_DELETE_TOPICS_PRE + "'" + topicType + "'";

		// TODO Auto-generated method stub
		mDb.execSQL(SQL_DELETE); // 清空数据库数据
		for (int j = 0; j < topics.size(); j++) {
			HashMap<String, Object> singleTopics = topics.get(j); // 获得某个话题，下面加入话题数据库
			ContentValues values = new ContentValues();
			values.put(TopicsEntryContract.COLUMN_NAME_OF_ID,
					Integer.valueOf((String) singleTopics.get("id")));
			values.put(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
					(String) singleTopics.get("topicName"));
			values.put(TopicsEntryContract.COLUMN_NAME_TOPIC_TYPE, topicType);
			values.put(TopicsEntryContract.COLUMN_NAME_TOPIC_URL,
					(String) singleTopics.get("url"));
			values.put(TopicsEntryContract.COLUMN_NAME_TOPIC_DATE,
					(String) singleTopics.get("date"));
			mDb.insert(TopicsEntryContract.TABLE_NAME, null, values);

		}

	}

	/**
	 * 异步查询数据库话题数据
	 * 
	 * @author hankwing
	 * 
	 */
	public static class TopicsLoader extends AsyncTaskLoader<Cursor> {

		int id;

		public TopicsLoader(Context context, int id) {
			super(context);
			// TODO Auto-generated constructor stub
			this.id = id;
		}

		@Override
		public Cursor loadInBackground() {
			// TODO Auto-generated method stub
			if (mDb == null) {
				try {
					semp.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Cursor temp = null;
			switch (id) {
			case 0:

				temp = mDb.query(TopicsEntryContract.TABLE_NAME, new String[] {
						TopicsEntryContract._ID,
						TopicsEntryContract.COLUMN_NAME_OF_ID,
						TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
						TopicsEntryContract.COLUMN_NAME_TOPIC_URL },
						TopicsEntryContract.COLUMN_NAME_TOPIC_TYPE + "=?",
						new String[] { "society" }, null, null,
						TopicsEntryContract.COLUMN_NAME_TOPIC_DATE + " DESC");
				break;
			case 1:
				temp = mDb.query(TopicsEntryContract.TABLE_NAME, new String[] {
						TopicsEntryContract._ID,
						TopicsEntryContract.COLUMN_NAME_OF_ID,
						TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
						TopicsEntryContract.COLUMN_NAME_TOPIC_URL },
						TopicsEntryContract.COLUMN_NAME_TOPIC_TYPE + "=?",
						new String[] { "science" }, null, null,
						TopicsEntryContract.COLUMN_NAME_TOPIC_DATE + " DESC");
				break;
			case 2:
				temp = mDb.query(TopicsEntryContract.TABLE_NAME, new String[] {
						TopicsEntryContract._ID,
						TopicsEntryContract.COLUMN_NAME_OF_ID,
						TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
						TopicsEntryContract.COLUMN_NAME_TOPIC_URL },
						TopicsEntryContract.COLUMN_NAME_TOPIC_TYPE + "=?",
						new String[] { "environment" }, null, null,
						TopicsEntryContract.COLUMN_NAME_TOPIC_DATE + " DESC");
				break;
			case 3:
				temp = mDb.query(TopicsEntryContract.TABLE_NAME, new String[] {
						TopicsEntryContract._ID,
						TopicsEntryContract.COLUMN_NAME_OF_ID,
						TopicsEntryContract.COLUMN_NAME_TOPIC_NAME,
						TopicsEntryContract.COLUMN_NAME_TOPIC_URL },
						TopicsEntryContract.COLUMN_NAME_TOPIC_TYPE + "=?",
						new String[] { "women" }, null, null,
						TopicsEntryContract.COLUMN_NAME_TOPIC_DATE + " DESC");
				break;
			}
			return temp;
		}

	}

}
