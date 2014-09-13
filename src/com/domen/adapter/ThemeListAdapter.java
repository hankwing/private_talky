package com.domen.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.domen.tools.BitmapSingleton;
import com.domen.tools.TopicsContract.TopicsEntryContract;
import com.wxl.lettalk.R;

/**
 * 定义了单个话题的界面适配器
 * @author hankwing
 *
 */
public class ThemeListAdapter extends CursorAdapter {

	private LayoutInflater theme_inflater;
	private ImageLoader mImageLoader;
	private Context context;
	/**
	 * flag可填0
	 * @param context
	 * @param c
	 * @param flags
	 */
	public ThemeListAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		// TODO Auto-generated constructor stub
		this.context = context;
		theme_inflater = LayoutInflater.from(this.context);
	}
	
	static class ViewHolder{
		public TextView tvName;
		private NetworkImageView mNetworkImageView;
	}

//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		
//		ViewHolder viewHolder = null;
//		if( convertView == null ) {
//			
//			convertView = theme_inflater.inflate(R.layout.single_topic_view, parent, false);
//			viewHolder = new ViewHolder();
//			viewHolder.tvName = (TextView) convertView.findViewById(R.id.theme_name);
//			viewHolder.mNetworkImageView = (NetworkImageView) convertView.findViewById(R.id.theme_back);	
//			convertView.setTag(viewHolder);		
//		}
//		else {
//			viewHolder = (ViewHolder) convertView.getTag();
//		}
//		ThemeEntity themeEnitiy = themelist.get(position);
//		viewHolder.tvName.setText(themeEnitiy.getName());
//		viewHolder.tvName.setTextColor(Color.WHITE);
//		mImageLoader = BitmapSingleton.getInstance(context).getImageLoader();
//		viewHolder.mNetworkImageView.setImageUrl(themeEnitiy.getBitmapUrl(), mImageLoader);
//		return convertView;
//	}
//
//	public void updateData( List<ThemeEntity> newData) {
//		themelist = newData;
//	}

	/**
	 * The cursor is already moved to the correct position.
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = (ViewHolder) view.getTag();
		viewHolder.tvName.setText(cursor.getString(cursor.getColumnIndex(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME)));
		viewHolder.tvName.setTextColor(Color.WHITE);
		mImageLoader = BitmapSingleton.getInstance(context).getImageLoader();
		viewHolder.mNetworkImageView.setImageUrl(
				cursor.getString(cursor.getColumnIndex(TopicsEntryContract.COLUMN_NAME_TOPIC_URL)), mImageLoader);
	}

	/**
	 * The cursor from which to get the data. The cursor is already moved to the correct position.
	 */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		View convertView;
		convertView = theme_inflater.inflate(R.layout.single_topic_view, parent, false);
		viewHolder = new ViewHolder();
		viewHolder.tvName = (TextView) convertView.findViewById(R.id.theme_name);
		viewHolder.mNetworkImageView = (NetworkImageView) convertView.findViewById(R.id.theme_back);
		convertView.setTag(viewHolder);
		return convertView;
	}
	
}
