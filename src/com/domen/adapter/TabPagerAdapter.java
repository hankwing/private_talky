package com.domen.adapter;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.domen.activities.DecideActivity;
import com.domen.entity.ThemeEntity;
import com.wxl.lettalk.R;

/**
 * ViewPager的适配器 定义了每个标签下的adapter
 * @author hankwing
 *
 */
public class TabPagerAdapter extends PagerAdapter {
	
	List<ThemeListAdapter> adapterslist;
	List<View> viewsList;
	Context context;
	public ListView listView;

	public TabPagerAdapter(Context context , List<View> viewsList,List<ThemeListAdapter> adapterslist) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.viewsList = viewsList;
		this.adapterslist = adapterslist;
		
	}
	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#instantiateItem(android.view.View, int)
	 */
	@Override
	public Object instantiateItem(View container, int position) {
		
		((ViewPager)container).addView(viewsList.get(position),0);
		//ThemeEntity entity = new ThemeEntity();
		ThemeListAdapter adapter;
		switch (position) {
		case 0:
			adapter = adapterslist.get(position);
			listView = (ListView) container.findViewById(R.id.comic_list);
			listView.setClickable(true);
			listView.setFocusable(true);
			listView.setOnItemClickListener(new ThemeItemClickListener((ThemeEntity)adapter.getItem(position)));
			listView.setAdapter(adapter);

			break;
		case 1:
			adapter = adapterslist.get(position);
			listView = (ListView) container.findViewById(R.id.science_list);
			listView.setOnItemClickListener(new ThemeItemClickListener((ThemeEntity)adapter.getItem(position)));
			listView.setAdapter(adapter);

			break;
		case 2:
			break;	
		case 3:

			break;	
		case 4:

			break;	
		case 5:

			break;
		default:
			break;

		}
		

		return viewsList.get(position);
		
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return viewsList.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub

		return arg0==(arg1);
	}
	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#destroyItem(android.view.View, int, java.lang.Object)
	 */
	@Override
	public void destroyItem(View container, int position, Object object) {
		// TODO Auto-generated method stub
		((ViewPager) container).removeView(viewsList.get(position));
	}
	
	class ThemeItemClickListener implements OnItemClickListener{
		
		ThemeEntity themeEnitiy;
		
		public ThemeItemClickListener(ThemeEntity themeEnitiy) {
			// TODO Auto-generated constructor stub
			this.themeEnitiy = themeEnitiy;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(context, DecideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("theme", themeEnitiy.getName());
			bundle.putString("topicID", themeEnitiy.getID());
			//Log.i("message", "topicID: " + themeEnitiy.getID());
			intent.putExtras(bundle);
			context.startActivity(intent);
		}
		
	}

}
