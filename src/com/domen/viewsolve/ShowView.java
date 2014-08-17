package com.domen.viewsolve;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 图片处理类
 * @author hankwing
 *
 */
public class ShowView {
	
	private static Context context;
	static float Width;
	static float Height;

	public ShowView(Context context, float Width, float Height) {
		ShowView.context = context;
		ShowView.Width = Width;
		ShowView.Height = Height;
	}
	
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {  

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);  
		Canvas canvas = new Canvas(output);  

		final int color = 0xff424242;  
		final Paint paint = new Paint();  
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
		final RectF rectF = new RectF(rect);  
		final float roundPx = pixels;  

		paint.setAntiAlias(true);  
		canvas.drawARGB(0, 0, 0, 0);  
		paint.setColor(color);  
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;  
		
	}
	
	public static Bitmap readBitmapAutoSize(String filePath, float outWidth,
			float outHeight) {
		// outWidth��outHeight��Ŀ��ͼƬ������Ⱥ͸߶ȣ���������
		FileInputStream fs = null;
		BufferedInputStream bs = null;
		if(new File(filePath).exists()) {
			try {
				fs = new FileInputStream(filePath);
				bs = new BufferedInputStream(fs);
				BitmapFactory.Options options = setBitmapOption(filePath,
						(int) outWidth, (int) outHeight);
				Bitmap temp = BitmapFactory.decodeStream(bs, null, options);
				return temp;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					bs.close();
					fs.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			return null;
		}
		return null;
	}
	private static BitmapFactory.Options setBitmapOption(String file,
			int width, int height) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		opt.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
		opt.inScaled = true;
		// ����ֻ�ǽ���ͼƬ�ı߾࣬�˲���Ŀ���Ƕ���ͼƬ��ʵ�ʿ�Ⱥ͸߶�
		BitmapFactory.decodeFile(file, opt);
		int outWidth = opt.outWidth; // ���ͼƬ��ʵ�ʸߺͿ�
		int outHeight = opt.outHeight;
		/*
		 * Log.i("display", "height " + outHeight + " width " + outWidth);
		 * Log.i("display", "wheight " + height + " wwidth " + width);
		 */
		opt.inDither = false;
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		// ���ü���ͼƬ����ɫ��Ϊ16bit��Ĭ����RGB_8888����ʾ24bit��ɫ��͸��ͨ������һ���ò���
		opt.inSampleSize = 1;
		// �������ű�,1��ʾԭ������2��ʾԭ�����ķ�֮һ....
		// �������ű�
		if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
			int sampleSize = (outWidth / width + outHeight / height) / 2;
			opt.inSampleSize = sampleSize;
		}
		opt.inJustDecodeBounds = false;// ���ѱ�־��ԭ
		return opt;
	}
}
