package com.domen.activities;

import android.app.Activity;
import android.os.Bundle;

import com.wxl.lettalk.R;

public class RanksActivity extends Activity {

	/* (non-Javadoc)
	 * 显示当前队伍的比分排名情况
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank_layout);
	}

}
