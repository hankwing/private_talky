package com.domen.openfire;

import java.util.Collection;

import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.domen.start.LoginActivity;
import com.domen.tools.CurrentActivity;
import com.domen.tools.MXMPPConnection;
import com.domen.start.R;

public class MultipleLoginPresenceListener implements RosterListener {

	Activity mContext;
	static String mulResource = null;

	@Override
	public void entriesAdded(Collection<String> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void entriesDeleted(Collection<String> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void entriesUpdated(Collection<String> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void presenceChanged(Presence arg0) {
		// TODO Auto-generated method stub
		if( arg0.getType() == Presence.Type.available) {
			//好友上线通知
			String userJID = arg0.getFrom();
			//String userAccount = userJID.substring(0, userJID.indexOf("@"));
			String resource = userJID.substring(userJID.indexOf("/")+1);
			mulResource = resource;
			if( !mulResource.equals(Build.MODEL)) {
				//与自己资源不相同 说明同一个帐号在不同地方登录
				//Log.i("message", "conflict: " + mulResource + " " + Build.MODEL);
				mContext = CurrentActivity.getCurrentActivity();
				new MultiLoginDialog().show(mContext.getFragmentManager(),
						"multipleLogin");
			}
		}
	}

	/**
	 * 通知用户重复登录
	 * 
	 * @author hankwing
	 * 
	 */
	public static class MultiLoginDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// TODO Auto-generated method stub

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("您的帐号在别处登录: " + mulResource + ", 请确认是您本人操作")
					.setPositiveButton(R.string.dialog_positive,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// 进入login界面 同时主界面消失
									new LoginActivity.DisConnection().execute(
											MXMPPConnection.getInstance());		
									Context context = getActivity();
									Intent intent = new Intent(context,
											LoginActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
											Intent.FLAG_ACTIVITY_CLEAR_TASK);
									context.startActivity(intent);
								}

							});
			Dialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false); // 设置点击屏幕其他部分不消失
			return dialog;
		}

	}

}
