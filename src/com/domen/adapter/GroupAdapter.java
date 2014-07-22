package com.domen.adapter;

import java.util.ArrayList;
import java.util.List;

import com.domen.entity.GroupEntity;
import com.domen.lettalk.ChatActivity;
import com.domen.start.InitApp;
import com.wxl.lettalk.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GroupAdapter extends BaseAdapter {

	List<GroupEntity> group_list = new ArrayList<GroupEntity>();
	private Context context;
	private LayoutInflater group_inflater;



	public GroupAdapter(Context context,List<GroupEntity> group_list) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.group_list = group_list;
		group_inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return group_list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	static class ViewHolder{
		public ImageView iv_fir;
		public ImageView iv_sec;
		public ImageView iv_thr;
		public ImageView iv_fou;
		public ImageView iv_fif;
		public ImageView iv_six;
		public TextView ivText;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		GroupEntity groupEnitiy = group_list.get(arg0);
		ViewHolder viewHolder = new ViewHolder();

		arg1 = group_inflater.inflate(R.layout.group_item, null);
		viewHolder.iv_fir = (ImageView) arg1.findViewById(R.id.group_fir);
		viewHolder.iv_sec = (ImageView) arg1.findViewById(R.id.group_sec);
		viewHolder.iv_thr = (ImageView) arg1.findViewById(R.id.group_thr);
		viewHolder.iv_fou = (ImageView) arg1.findViewById(R.id.group_fou);
		viewHolder.iv_fif = (ImageView) arg1.findViewById(R.id.group_fif);
		viewHolder.iv_six = (ImageView) arg1.findViewById(R.id.group_six);


		viewHolder.iv_fir.setBackgroundDrawable(groupEnitiy.getGroup_fir());
		viewHolder.iv_sec.setBackgroundDrawable(groupEnitiy.getGroup_sec());
		viewHolder.iv_thr.setBackgroundDrawable(groupEnitiy.getGroup_thi());
		viewHolder.iv_fou.setBackgroundDrawable(groupEnitiy.getGroup_fou());
		viewHolder.iv_fif.setBackgroundDrawable(groupEnitiy.getGroup_fif());
		viewHolder.iv_six.setBackgroundDrawable(groupEnitiy.getGroup_six());
		// TODO Auto-generated method stub
		return arg1;
	}

}
