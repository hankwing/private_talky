package com.domen.lettalk;

import java.util.ArrayList;
import java.util.List;

import com.domen.adapter.GroupAdapter;
import com.domen.entity.GroupEntity;
import com.wxl.lettalk.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;


public class GroupActivity extends Activity {

	List<GroupEntity> group_list = new ArrayList<GroupEntity>();;
	GridView groupView;
	GroupAdapter groupAdapter;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_group);
		groupView = (GridView) this.findViewById(R.id.group_view);
		groupView.setFocusable(true);
		groupView.setClickable(true);
		groupView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Toast.makeText(GroupActivity.this, "haha", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(GroupActivity.this,ChatActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("side", "negative");
				bundle.putString("theme", "theme");
				intent.putExtras(bundle);
				startActivity(intent);
			}

		});
		initGroup();
		groupAdapter = new GroupAdapter(getApplicationContext(), group_list);
		groupView.setAdapter(groupAdapter);

		super.onCreate(savedInstanceState);

	}


	public void initGroup(){
		Drawable dra_fif = getResources().getDrawable(R.drawable.icon_temp_fif);
		Drawable dra_fir = getResources().getDrawable(R.drawable.icon_temp_fir);
		Drawable dra_fou = getResources().getDrawable(R.drawable.icon_temp_fou);
		Drawable dra_sec = getResources().getDrawable(R.drawable.icon_temp_sec);
		Drawable dra_six = getResources().getDrawable(R.drawable.icon_temp_six);
		Drawable dra_thr = getResources().getDrawable(R.drawable.icon_temp_thr);

		GroupEntity groupEnitiy = new GroupEntity(dra_fir, dra_sec, dra_thr, dra_fou, dra_fif, dra_six);
		group_list.add(groupEnitiy);
	}

}
