package com.domen.adapter;


import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.domen.activities.DecideActivity;
import com.domen.activities.MainActivity;
import com.domen.openfire.RequestSync;
import com.domen.tools.TopicsContract.TopicsEntryContract;
import com.domen.start.R;

public class ThemeTabAdapter extends FragmentStatePagerAdapter {

	private List<ThemeListAdapter> adapterslist;
	private int number = 0;
	private String[] titles;
	private static Context context;
	public ThemeTabAdapter(Context context, FragmentManager fm, int number, String[] titles,
			List<ThemeListAdapter> adapterslist ) {
		super(fm);
		// TODO Auto-generated constructor stub
		ThemeTabAdapter.context = context;
		this.adapterslist = adapterslist;
		this.number = number;
		this.titles = titles;
	}

	@Override
	public Fragment getItem(int i) {
		// TODO Auto-generated method stub
		ListFragment fragment = new MainActivity.SwipeRefreshListFragmentFragment();
		//fragment.setListAdapter(adapterslist.get(i));
		Bundle args = new Bundle();
		args.putInt("itemPosition", i);				//放入页面号
		//为了区别在不同话题类别下的刷新操作
		switch(i) {
		case 0:
			// society
			args.putInt(RequestSync.REQUESTTYPE, -1);
			args.putString(RequestSync.TOPICTYPE, "society");
			args.putInt(RequestSync.POSITION, 0);
			break;
		case 1:
			args.putInt(RequestSync.REQUESTTYPE, -1);
			args.putString(RequestSync.TOPICTYPE, "science");
			args.putInt(RequestSync.POSITION, 1);
			break;
		case 2:
			args.putInt(RequestSync.REQUESTTYPE, -1);
			args.putString(RequestSync.TOPICTYPE, "environment");
			args.putInt(RequestSync.POSITION, 2);
			break;
		case 3:
			args.putInt(RequestSync.REQUESTTYPE, -1);
			args.putString(RequestSync.TOPICTYPE, "women");
			args.putInt(RequestSync.POSITION, 3);
			break;
		}
        fragment.setArguments(args);
        return fragment;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return number;
	}
	
	@Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
	
	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return POSITION_NONE;
	}

	public void updateData( List<ThemeListAdapter> adapterslist ) {
		this.adapterslist = adapterslist;
	}
	
	/**
	 * 定义了显示在Tab中的listFragment
	 * @author hankwing
	 *
	 */
	public static class ThemeFragment extends ListFragment {
	    public static final String ARG_OBJECT = "object";
	    private SwipeRefreshLayout mSwipeRefreshLayout;
	    
	    @Override
	    public View onCreateView(LayoutInflater inflater,
	            ViewGroup container, Bundle savedInstanceState) {
	        // The last two arguments ensure LayoutParams are inflated
	        // properly.
	    	//Log.i("message", "create the fragment view");
	        View rootView = inflater.inflate(
	                R.layout.list_layout, container, false);
	        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());
	        mSwipeRefreshLayout.addView(rootView,
	                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

	        // Make sure that the SwipeRefreshLayout will fill the fragment
	        mSwipeRefreshLayout.setLayoutParams(
	                new ViewGroup.LayoutParams(
	                        ViewGroup.LayoutParams.MATCH_PARENT,
	                        ViewGroup.LayoutParams.MATCH_PARENT));
	        setColorScheme(R.color.color_scheme_1_1, R.color.color_scheme_1_2,
                    R.color.color_scheme_1_3, R.color.color_scheme_1_4);
	        return mSwipeRefreshLayout;
	    }

	    /**
	     * listview的点击事件
	     */
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			// TODO Auto-generated method stub
			Cursor c = ((ThemeListAdapter) getListView().getAdapter()).getCursor();
			c.moveToPosition(position);
			Intent intent = new Intent(context, DecideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME, 
					c.getString(c.getColumnIndex(TopicsEntryContract.COLUMN_NAME_TOPIC_NAME)));
			bundle.putString(TopicsEntryContract.COLUMN_NAME_OF_ID,
					String.valueOf( c.getInt(c.getColumnIndex(TopicsEntryContract.COLUMN_NAME_OF_ID))));
			bundle.putString(TopicsEntryContract.COLUMN_NAME_TOPIC_URL,
					String.valueOf( c.getString(c.getColumnIndex(TopicsEntryContract.COLUMN_NAME_TOPIC_URL))));
			//Log.i("message", "topicID: " + themeEnitiy.getID());
			intent.putExtras(bundle);
			context.startActivity(intent);
			super.onListItemClick(l, v, position, id);
		}
	    
		 /**
	     * Set the {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener} to listen for
	     * initiated refreshes.
	     *
	     * @see android.support.v4.widget.SwipeRefreshLayout#setOnRefreshListener(android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener)
	     */
	    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
	        mSwipeRefreshLayout.setOnRefreshListener(listener);
	    }

	    /**
	     * Returns whether the {@link android.support.v4.widget.SwipeRefreshLayout} is currently
	     * refreshing or not.
	     *
	     * @see android.support.v4.widget.SwipeRefreshLayout#isRefreshing()
	     */
	    public boolean isRefreshing() {
	        return mSwipeRefreshLayout.isRefreshing();
	    }

	    /**
	     * Set whether the {@link android.support.v4.widget.SwipeRefreshLayout} should be displaying
	     * that it is refreshing or not.
	     *
	     * @see android.support.v4.widget.SwipeRefreshLayout#setRefreshing(boolean)
	     */
	    public void setRefreshing(boolean refreshing) {
	        mSwipeRefreshLayout.setRefreshing(refreshing);
	    }

	    /**
	     * Set the color scheme for the {@link android.support.v4.widget.SwipeRefreshLayout}.
	     *
	     * @see android.support.v4.widget.SwipeRefreshLayout#setColorScheme(int, int, int, int)
	     */
	    public void setColorScheme(int colorRes1, int colorRes2, int colorRes3, int colorRes4) {
	        mSwipeRefreshLayout.setColorScheme(colorRes1, colorRes2, colorRes3, colorRes4);
	    }

	    /**
	     * @return the fragment's {@link android.support.v4.widget.SwipeRefreshLayout} widget.
	     */
	    public SwipeRefreshLayout getSwipeRefreshLayout() {
	        return mSwipeRefreshLayout;
	    }
	    
		/**
	     * Sub-class of {@link android.support.v4.widget.SwipeRefreshLayout} for use in this
	     * {@link android.support.v4.app.ListFragment}. The reason that this is needed is because
	     * {@link android.support.v4.widget.SwipeRefreshLayout} only supports a single child, which it
	     * expects to be the one which triggers refreshes. In our case the layout's child is the content
	     * view returned from
	     * {@link android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
	     * which is a {@link android.view.ViewGroup}.
	     *
	     * <p>To enable 'swipe-to-refresh' support via the {@link android.widget.ListView} we need to
	     * override the default behavior and properly signal when a gesture is possible. This is done by
	     * overriding {@link #canChildScrollUp()}.
	     */
	    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

	        public ListFragmentSwipeRefreshLayout(Context context) {
	            super(context);
	        }

	        /**
	         * As mentioned above, we need to override this method to properly signal when a
	         * 'swipe-to-refresh' is possible.
	         *
	         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
	         */
	        @Override
	        public boolean canChildScrollUp() {
	            final ListView listView = getListView();
	            if (listView.getVisibility() == View.VISIBLE) {
	                return canListViewScrollUp(listView);
	            } else {
	                return false;
	            }
	        }

	    }
	    
	 // BEGIN_INCLUDE (check_list_can_scroll)
	    /**
	     * Utility method to check whether a {@link ListView} can scroll up from it's current position.
	     * Handles platform version differences, providing backwards compatible functionality where
	     * needed.
	     */
	    private static boolean canListViewScrollUp(ListView listView) {
	        if (android.os.Build.VERSION.SDK_INT >= 14) {
	            // For ICS and above we can call canScrollVertically() to determine this
	            return ViewCompat.canScrollVertically(listView, -1);
	        } else {
	            // Pre-ICS we need to manually check the first visible item and the child view's top
	            // value
	            return listView.getChildCount() > 0 &&
	                    (listView.getFirstVisiblePosition() > 0
	                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
	        }
	    }
	    
	}

}
