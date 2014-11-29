package com.domen.tools;

import android.app.Activity;

public class CurrentActivity {
	
	private static Activity currentActivity = null;
	
	public static Activity getCurrentActivity() {
		return currentActivity;
	}
	
	/**
	 * 每次打开一个应用时调用
	 * @param context
	 */
	public static void setCurrentActivity( Activity context) {
		currentActivity = context;
	}
	
	
}
