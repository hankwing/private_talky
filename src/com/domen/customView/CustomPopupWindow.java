package com.domen.customView;

import com.domen.start.R;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

public class CustomPopupWindow extends PopupWindow {
	
	public CustomPopupWindow( Context context) {
		super(context);
	}
	/**
     * 在指定控件上方显示，默认x座标与指定控件的中点x座标相同
     * @param anchor
     * @param xoff
     * @param yoff
     */
    public void showAsPullUp(View anchor, int xoff, int yoff)
    {
    	//保存anchor在屏幕中的位置
    	int[] location=new int[2];
    	//保存anchor上部中点
    	int[] anchorCenter=new int[2];
    	//读取位置anchor座标
    	anchor.getLocationOnScreen(location);
    	//计算anchor中点
    	anchorCenter[0]=location[0]+anchor.getWidth()/2;
    	anchorCenter[1]=location[1];
    	super.showAtLocation(anchor, Gravity.TOP|Gravity.LEFT, 
    			anchorCenter[0]+xoff, anchorCenter[1]-anchor.getContext().getResources().getDimensionPixelSize(R.dimen.show_attitude_popwindow_height)+yoff);
    }
    
}
