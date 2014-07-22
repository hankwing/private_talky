package com.domen.lettalk;

import com.wxl.lettalk.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DecideActivity extends Activity implements OnClickListener{

	Button btn_pos;
	Button btn_neg;
	Button btn_discuss_home;
	String theme;
	ProgressDialog findProcess = null;
	Intent intent=null;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_decide);
		btn_neg = (Button) this.findViewById(R.id.btn_negative);
		btn_neg.setOnClickListener(this);
		btn_pos = (Button) this.findViewById(R.id.btn_positive);
		btn_pos.setOnClickListener(this);
		btn_discuss_home = (Button) this.findViewById(R.id.btn_discuss_home);
		btn_discuss_home.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(), GroupActivity.class);
				startActivity(intent);
			}
		});
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		theme = bundle.getString("theme");


		super.onCreate(savedInstanceState);
	}
	/**
	 * ���Ѱ�ұ���
	 * �����߳̽��м�������ȷ����Ӧ�������������ȴ�
	 * �û�Ҳ�����ú��˼��˳�ƥ��
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		intent = new Intent(getApplicationContext(), ChatActivity.class);
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.btn_negative:
			bundle.putString("side", "negative");
			break;
		case R.id.btn_positive:
			bundle.putString("side", "positive");

		default:
			break;
		}
		bundle.putString("theme", theme);
		intent.putExtras(bundle);

		//��ʼ��Ѱ�ҵȴ�
		findProcess = ProgressDialog.show(DecideActivity.this, "����ƥ��", "�ȴ�...",true,false);
		new Thread(){

			public void run() {
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendEmptyMessage(0);
			}

		}.start();

	}


	/**
	 * ��Handle����UI
	 */
	private Handler handler = new Handler(){  

		@Override  
		public void handleMessage(Message msg) {  

			//�ر�ProgressDialog  
			findProcess.dismiss();  
			startActivity(intent);

			//����UI  

		}
	}; 

}
