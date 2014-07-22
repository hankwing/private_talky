package com.domen.lettalk;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import com.wxl.lettalk.R;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

public class RecordActivity extends Activity {

	// ================================¼���ı�=======================
	private String record_path = null;
	private MediaRecorder mediaRecorder = null;
	private File myFile = null; // ָ�����ļ�
	private File mydirFile = null; // ָ�����ļ���
	private Button recordStart = null;
	private String name = null;
	private Vibrator vibrator;

	// ============================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_record);
		// Make dictionary!
		record_path = Environment.getExternalStorageDirectory().getPath() + "/"
				+ "Talky" + "/" + "record" + "/";
		mydirFile = new File(record_path);
		mydirFile.mkdirs();

		// Button Listener!
		recordStart = (Button) this.findViewById(R.id.record_start);
		recordStart.setOnTouchListener(new RecordListener());
		super.onCreate(savedInstanceState);
	}

	private class RecordListener implements OnTouchListener {
		@SuppressWarnings("deprecation")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				try {
					vibrator.vibrate(200);
					new DateFormat();
					name = DateFormat.format("yyyyMMdd_hhmmss",
							Calendar.getInstance(Locale.CHINA))
							+ ".amr";
					myFile = new File(record_path + name);
					myFile.createNewFile();

					mediaRecorder = new MediaRecorder();
					if (mediaRecorder != null) {
						mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
						mediaRecorder
						.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
						mediaRecorder
						.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
						mediaRecorder.setOutputFile(myFile.getAbsolutePath());
						mediaRecorder.prepare();
						mediaRecorder.start();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			case MotionEvent.ACTION_UP:
				if ( mediaRecorder != null) {

					mediaRecorder.stop();
					mediaRecorder.release();
					mediaRecorder = null;
					RecordActivity.this.finish();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			}
			return false;
		}
	}

	@Override
	public void finish() {
		if (myFile == null) {
			RecordActivity.this.setResult(0);
		} else {
			Bundle bundle = new Bundle();
			bundle.putString("name", myFile.getAbsolutePath());
			RecordActivity.this.setResult(14, RecordActivity.this.getIntent()
					.putExtras(bundle));
		}
		// TODO Auto-generated method stub
		super.finish();
	}

}
