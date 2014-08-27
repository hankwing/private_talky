package com.domen.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.domen.entity.ThemeEntity;
import com.wxl.lettalk.R;

/**
 * 定义了单个话题的界面适配器
 * @author hankwing
 *
 */
public class ThemeListAdapter extends BaseAdapter {

	private LayoutInflater theme_inflater;
	private List<ThemeEntity> themelist;
	private Context context;
	
	public ThemeListAdapter(Context context,List<ThemeEntity> themelist) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.themelist = themelist;
		theme_inflater = LayoutInflater.from(this.context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return themelist.size();
	}

	/**
	 * 获得点击的对象
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return themelist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	static class ViewHolder{
		public TextView tvName;
		public TextView tvBack;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if( convertView == null ) {
			ThemeEntity themeEnitiy = themelist.get(position);
			convertView = theme_inflater.inflate(R.layout.single_topic_view, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.tvName = (TextView) convertView.findViewById(R.id.theme_name);
			viewHolder.tvBack = (TextView) convertView.findViewById(R.id.theme_back);
			viewHolder.tvName.setText(themeEnitiy.getName());
			viewHolder.tvName.setTextColor(Color.WHITE);
			viewHolder.tvBack.setBackgroundDrawable(themeEnitiy.getPicture());
			convertView.setTag(viewHolder);
			
		}
		return convertView;
	}

	public void updateData( List<ThemeEntity> newData) {
		themelist = newData;
	}
}
