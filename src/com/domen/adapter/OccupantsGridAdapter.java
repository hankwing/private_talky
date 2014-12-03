package com.domen.adapter;

import java.lang.reflect.Field;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.domen.start.R;
import com.domen.tools.BitmapMemAndDiskCache;

public class OccupantsGridAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater chat_inflater;
	private List<String> allOccupants;			//full JID

	public OccupantsGridAdapter(Context context,
			List<String> allOccupantsData ) {
		mContext = context;
		allOccupants = allOccupantsData;
		chat_inflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return allOccupants.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return allOccupants.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView ivAvatar;
		if (convertView == null) {
			convertView = chat_inflater.inflate(R.layout.single_inactive_avatar, null);
			ivAvatar = (ImageView) convertView.findViewById(R.id.singleuser_avatar);

			convertView.setTag(ivAvatar);

		} else {
			ivAvatar = (ImageView) convertView.getTag();
		}
		String userJID = allOccupants.get(position);
		BitmapMemAndDiskCache.getInstance(mContext).loadAvatarBitmap(this, ivAvatar, 
				StringUtils.parseName(userJID), StringUtils.parseBareAddress(userJID), false);
		
		return convertView;
	}
	
	/**
	 * obtain the resource id
	 * @param variableName
	 * @param c
	 * @return
	 */
	public static int getResId(String variableName, Class<?> c) {

	    try {
	        Field idField = c.getDeclaredField(variableName);
	        return idField.getInt(idField);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return -1;
	    } 
	}

}
