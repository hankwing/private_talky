package com.domen.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.domen.start.BuildConfig;
import com.domen.start.R;

/**
 * 加载VCard的头像数据并缓存
 * 
 * @author hankwing
 * 
 */
public class BitmapMemAndDiskCache {

	private static BitmapMemAndDiskCache mInstance;
	private static Context mContext;
	private LruCache<String, Bitmap> mMemoryCache; // 内存缓存

	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "avatar";

	private CompressFormat mCompressFormat = CompressFormat.JPEG;
	private int mCompressQuality = 100;

	public BitmapMemAndDiskCache(Context context) {

		mContext = context;
		final int cacheSize = 400 * 1024; // 400k

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};

		File cacheDir = getDiskCacheDir(mContext, DISK_CACHE_SUBDIR);
		new InitDiskCacheTask().execute(cacheDir);

	}

	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
		@Override
		protected Void doInBackground(File... params) {
			synchronized (mDiskCacheLock) {
				File cacheDir = params[0];
				try {
					mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1,
							DISK_CACHE_SIZE);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mDiskCacheStarting = false; // Finished initialization
				mDiskCacheLock.notifyAll(); // Wake any waiting threads
			}
			return null;
		}
	}

	public static synchronized BitmapMemAndDiskCache getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new BitmapMemAndDiskCache(context);
		}
		return mInstance;
	}

	/**
	 * 放入内存以及disk缓存
	 * @param key
	 * @param bitmap
	 */
	public void addBitmapToCache(String key, Bitmap bitmap) {
	    // Add to memory cache as before
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }

	    // Also add to disk cache
	    synchronized (mDiskCacheLock) {
	        // Wait while disk cache is started from background thread
	        while (mDiskCacheStarting) {
	            try {
	                mDiskCacheLock.wait();
	            } catch (InterruptedException e) {}
	        }
	        if (mDiskLruCache != null) {
            	put(key, bitmap);
	        }
	    }
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	/**
	 * 异步加载图片数据
	 * 
	 * @author hankwing
	 * 
	 */
	public class BitmapWorkerTask extends AsyncTask<VCard, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private VCard data;

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(VCard... params) {
			data = params[0];
			//Log.i("message", "key : " + data.getNickName());
			Bitmap bitmap = getBitmapFromDiskCache(data.getNickName());		//先检查在disk上有无缓存
			byte[] avatarByte = data.getAvatar();
			if (bitmap == null && avatarByte != null) { // Not found in disk cache
	            // 将byte转为bitmap
				//Log.i("message", "disk non");
	            bitmap = BitmapFactory.decodeByteArray(
						avatarByte, 0, avatarByte.length);
	        }
			if( bitmap != null) {
				// 利用VCard获得图像数据并放入内存和disk缓存中
				addBitmapToCache(data.getNickName(), bitmap);
				//Log.i("message", "disk not non");
			}
			return bitmap;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	/**
	 * 加载VCard的头像数据到imageView中 供外部类使用的方法
	 * @param key
	 * @param vcard
	 * @param imageView
	 * @param isReset		是否代表重置该用户的头像
	 */
	public void loadAvatarBitmap(String key, VCard vcard, ImageView imageView, boolean isReset) {
		//final String imageKey = vcard.getNickName(); // 以VCard用户名作为key

		if( isReset) {
			mMemoryCache.remove(key);				//缓存中移除
			removeBitmapFromDiskCache(key);			//disk中移除
			vcard.setNickName(key);
			BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			task.execute(vcard);
		}
		else {
			final Bitmap bitmap = getBitmapFromMemCache(key);
			if (bitmap != null) {
				//Log.i("message", "mem not non");
				imageView.setImageBitmap(bitmap);
			} else {
				//Log.i("message", "mem non");
				vcard.setNickName(key);
				imageView.setImageResource(R.drawable.default_avatar);
				BitmapWorkerTask task = new BitmapWorkerTask(imageView);
				task.execute(vcard);
			}
		}
	}

	// Creates a unique subdirectory of the designated app cache directory.
	// Tries to use external
	// but if not mounted, falls back on internal storage.
	public static File getDiskCacheDir(Context context, String uniqueName) {
		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable() ? mContext
				.getExternalCacheDir().getPath() : context.getCacheDir()
				.getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * 将bitmap放入disk缓存
	 * 
	 * @param key
	 * @param data
	 */
	public void put(String key, Bitmap data) {

		DiskLruCache.Editor editor = null;
		try {
			editor = mDiskLruCache.edit(key);
			if (editor == null) {
				return;
			}

			if (writeBitmapToFile(data, editor)) {
				mDiskLruCache.flush();
				editor.commit();
				if (BuildConfig.DEBUG) {
					Log.d("cache_test_DISK_", "image put on disk cache " + key);
				}
			} else {
				editor.abort();
				if (BuildConfig.DEBUG) {
					Log.d("cache_test_DISK_",
							"ERROR on: image put on disk cache " + key);
				}
			}
		} catch (IOException e) {
			if (BuildConfig.DEBUG) {
				Log.d("cache_test_DISK_", "ERROR on: image put on disk cache "
						+ key);
			}
			try {
				if (editor != null) {
					editor.abort();
				}
			} catch (IOException ignored) {
			}
		}

	}

	private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor)
			throws IOException, FileNotFoundException {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(editor.newOutputStream(0),
					DiskCacheUtils.IO_BUFFER_SIZE);
			return bitmap.compress(mCompressFormat, mCompressQuality, out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public Bitmap getBitmap(String key) {

		Bitmap bitmap = null;
		DiskLruCache.Snapshot snapshot = null;
		try {

			snapshot = mDiskLruCache.get(key);
			if (snapshot == null) {
				return null;
			}
			final InputStream in = snapshot.getInputStream(0);
			if (in != null) {
				//final BufferedInputStream buffIn = new BufferedInputStream(in);
				bitmap = BitmapFactory.decodeStream(in);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (snapshot != null) {
				snapshot.close();
			}
		}

		if (BuildConfig.DEBUG) {
			Log.d("cache_test_DISK_", bitmap == null ? ""
					: "image read from disk " + key);
		}

		return bitmap;

	}

	public boolean containsKey(String key) {

		boolean contained = false;
		DiskLruCache.Snapshot snapshot = null;
		try {
			snapshot = mDiskLruCache.get(key);
			contained = snapshot != null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (snapshot != null) {
				snapshot.close();
			}
		}

		return contained;

	}
	
	public Bitmap getBitmapFromDiskCache(String key) {
	    synchronized (mDiskCacheLock) {
	        // Wait while disk cache is started from background thread
	        while (mDiskCacheStarting) {
	            try {
	                mDiskCacheLock.wait();
	            } catch (InterruptedException e) {}
	        }
	        if (mDiskLruCache != null) {
	            return getBitmap(key);
	        }
	    }
	    return null;
	}
	
	public void removeBitmapFromDiskCache(String key) {
	    synchronized (mDiskCacheLock) {
	        // Wait while disk cache is started from background thread
	        while (mDiskCacheStarting) {
	            try {
	                mDiskCacheLock.wait();
	            } catch (InterruptedException e) {}
	        }
	        if (mDiskLruCache != null) {
	            try {
					mDiskLruCache.remove(key);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
	    
	}

}
