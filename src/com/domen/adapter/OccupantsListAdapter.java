package com.domen.adapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.domen.entity.ConstantVariable;
import com.domen.start.R;

public class OccupantsListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater chat_inflater;
	private List<Map<String, String>> mData; // room info
	private List<String> allOccupants;

	public OccupantsListAdapter(Context context,
			List<String> allOccupantsData ) {
		mData = new ArrayList<Map<String, String>>();
		mContext = context;
		allOccupants = allOccupantsData;
		chat_inflater = LayoutInflater.from(mContext);
	}

	static class ViewHolder {
		public ImageView positive1rank;
		public ImageView negative1rank;
		public TextView occupant0;
		public TextView occupant1;
	}

	/**
	 * 1 now ( may be 3 in the future)
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = new ViewHolder();
		if (convertView == null) {
			convertView = chat_inflater.inflate(R.layout.single_roomlist_view, null); // 在左边显示
			viewHolder.positive1rank = (ImageView) convertView
					.findViewById(R.id.positive1rank);
			viewHolder.negative1rank = (ImageView) convertView
					.findViewById(R.id.negative1rank);
			viewHolder.occupant0 = (TextView) convertView
					.findViewById(R.id.occupant0);
			viewHolder.occupant1 = (TextView) convertView
					.findViewById(R.id.occupant1);

			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Map<String, String> tempMap = mData.get(position);
		//String positive1BareJID = tempMap.get("positive1BAREJID");
		//String negative1BareJID = tempMap.get("negative1BAREJID");
		String positive1NickName = tempMap.get("positive1NickName");
		String negative1NickName = tempMap.get("negative1NickName");
		if( !allOccupants.contains(positive1NickName) ) {
			viewHolder.occupant0.setText(positive1NickName+"(离开)");
		}
		else {
			viewHolder.occupant0.setText(positive1NickName);
		}
		if( !allOccupants.contains(negative1NickName)) {
			viewHolder.occupant1.setText(negative1NickName+"(离开)");
		}
		else {
			viewHolder.occupant1.setText(negative1NickName);
		}
		
		//Log.i("message", "adapter rank: " + tempMap.get("positive1" + ConstantVariable.Rank));
		if( tempMap.get("positive1" + ConstantVariable.Rank) != null) {
			int rank = Integer.valueOf( tempMap.get("positive1" + ConstantVariable.Rank));
			
			int resID = mContext.getResources().getIdentifier("rank"+rank, 
					"drawable", mContext.getPackageName());
			viewHolder.positive1rank.setImageResource(resID);
			//viewHolder.positive1rank.setVisibility(View.VISIBLE);
		}
		if( tempMap.get("negative1" + ConstantVariable.Rank) != null) {
			int rank = Integer.valueOf( tempMap.get("negative1" + ConstantVariable.Rank));
			
			int resID = mContext.getResources().getIdentifier("rank"+rank, 
					"drawable", mContext.getPackageName());
			viewHolder.negative1rank.setImageResource(resID);
			//viewHolder.negative1rank.setVisibility(View.VISIBLE);
		}
		
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
	
	/**
	 * add data
	 * @param data
	 */
	public void addOccupantsData( Map<String, String> data) {
		mData.add(data);
	}

}
