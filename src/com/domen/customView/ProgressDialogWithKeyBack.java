package com.domen.customView;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.KeyEvent;

public class ProgressDialogWithKeyBack extends ProgressDialog {

	AsyncTask<String, Void, Integer> task = null;
	public ProgressDialogWithKeyBack(Context context , AsyncTask<String, Void, Integer> task) {
		super(context);
		// TODO Auto-generated constructor stub
		setCanceledOnTouchOutside(false);
		this.task = task;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if( keyCode == KeyEvent.KEYCODE_BACK) {
			task.cancel(true);
			dismiss();
		}
		return super.onKeyDown(keyCode, event);
	}

}
