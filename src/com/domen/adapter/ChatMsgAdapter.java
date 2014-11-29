package com.domen.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.domen.activities.ChatActivity;
import com.domen.customView.CustomPopupWindow;
import com.domen.entity.MsgEntity;
import com.domen.tools.BitmapMemAndDiskCache;
import com.wxl.lettalk.R;

/**
 * 聊天记录listview的adapter
 * 
 * @author hankwing
 * 
 */
public class ChatMsgAdapter extends BaseAdapter {

	private LayoutInflater chat_inflater;
	private List<MsgEntity> chat_list; // 聊天内容表
	private Context context;
	private ImageView ibtn_like;
	private ImageView ibtn_shit;
	private CustomPopupWindow popuoWindow;
	private boolean isSpectator;
	private BitmapMemAndDiskCache avatarCache;
	private Map<String, VCard> vcardList; // 保存房间成员的VCard
	private int mSelectedPosition; // 点赞和点屎的message的position

	// private int[] expressionImages;
	// //private String[] expressionImageNames;
	// private int[] expressionImages1;
	// //private String[] expressionImageNames1;
	// private int[] expressionImages2;
	// //private String[] expressionImageNames2;
	// private Bitmap face;
	static class ViewHolder {
		public RelativeLayout contentLayout;
		public ImageView ivHead;
		public TextView tvContent;
		public TextView tvTime;
		public ImageView ivPicture;
		public Boolean isComMsg;
	}

	public ChatMsgAdapter(Context context, List<MsgEntity> chat_list,
			boolean isSpectator) {
		this.context = context;
		this.chat_list = chat_list;
		this.isSpectator = isSpectator;
		chat_inflater = LayoutInflater.from(context);
		vcardList = new HashMap<String, VCard>();
		avatarCache = BitmapMemAndDiskCache.getInstance(context);
		// expressionImages = Expressions.expressionImgs;
		// //expressionImageNames = Expressions.expressionImgNames;
		// expressionImages1 = Expressions.expressionImgs1;
		// //expressionImageNames1 = Expressions.expressionImgNames1;
		// expressionImages2 = Expressions.expressionImgs2;
		// //expressionImageNames2 = Expressions.expressionImgNames2;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return chat_list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return chat_list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(final int arg0, View arg1, ViewGroup arg2) {
		MsgEntity msgEnitiy = chat_list.get(arg0);
		ViewHolder viewHolder = new ViewHolder();

		if (arg1 == null) {
			if (msgEnitiy.getIsLeft()) {
				arg1 = chat_inflater.inflate(R.layout.chat_msg_left, null); // 在左边显示
				viewHolder.tvContent = (TextView) arg1
						.findViewById(R.id.chat_msg_left_content);
				viewHolder.ivHead = (ImageView) arg1
						.findViewById(R.id.chat_msg_left_name);
				// viewHolder.tvTime = (TextView)
				// arg1.findViewById(R.id.chat_msg_right_time);
				viewHolder.ivPicture = (ImageView) arg1
						.findViewById(R.id.chat_msg_left_image);
				viewHolder.contentLayout = (RelativeLayout) arg1
						.findViewById(R.id.chat_content_left);
			} else {
				arg1 = chat_inflater.inflate(R.layout.chat_msg_right, null);

				viewHolder.tvContent = (TextView) arg1
						.findViewById(R.id.chat_msg_right_content);
				viewHolder.ivHead = (ImageView) arg1
						.findViewById(R.id.chat_msg_right_name);
				// viewHolder.tvTime = (TextView)
				// arg1.findViewById(R.id.chat_msg_left_time);
				viewHolder.ivPicture = (ImageView) arg1
						.findViewById(R.id.chat_msg_right_image);
				viewHolder.contentLayout = (RelativeLayout) arg1
						.findViewById(R.id.chat_content_right);

			}

			arg1.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) arg1.getTag();
		}

		// BitmapDrawable bd = (BitmapDrawable) msgEnitiy.getHead();
		// viewHolder.ivHead.setImageBitmap(bd.getBitmap());
		String nickName = msgEnitiy.getName();
		if (vcardList.containsKey(msgEnitiy.getName())) {
			// 显示头像
			avatarCache.loadAvatarBitmap(nickName, vcardList.get(nickName),
					viewHolder.ivHead, false);
		}
		viewHolder.ivHead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Toast.makeText(context, "click on headImageView",
				// Toast.LENGTH_SHORT).show();
				/*
				 * Intent intent = new Intent(context,InfomaActivity.class);
				 * context.startActivity(intent);
				 */
			}
		});

		viewHolder.tvContent.setText(msgEnitiy.getContent());
		viewHolder.tvContent.setVisibility(View.VISIBLE);
		viewHolder.tvContent.setFocusable(true);
		viewHolder.ivPicture.setVisibility(View.GONE);

		// }else {
		//
		// viewHolder.ivPicture.setVisibility(View.VISIBLE);
		// viewHolder.ivPicture.setFocusable(true);
		// viewHolder.ivPicture.setBackgroundDrawable(msgEnitiy.getImage());
		// viewHolder.tvContent.setVisibility(View.GONE);
		//
		// }
		if (isSpectator) {
			// 如果是观众的话 那么长按消息框出现点赞画面
			viewHolder.tvContent
					.setOnLongClickListener(new OnLongClickListener() {

						@Override
						public boolean onLongClick(View v) {
							// TODO Auto-generated method stub
							// 显示点赞点屎界面 change the background of message
							//MsgEntity message = (MsgEntity) getItem(mSelectedPosition);
							//if (msgEnitiy.getIsLeft()) {
//								viewHolder.contentLayout
//										.setBackgroundResource(R.drawable.chatfrom_bg_normal);
//							} else {
//								viewHolder.contentLayout
//										.setBackgroundResource(R.drawable.chatto_bg_normal);
//							}
							//v.setBackgroundResource(R.drawable.chatfrom_bg_normal_pressed);

							if (popuoWindow == null) {
								conPopupWindow(context);
							}
							popuoWindow.showAsPullUp(v, 0, 0);
							return true;
						}
					});
		}

		// viewHolder.ivPicture.setOnLongClickListener(new OnLongClickListener()
		// {
		//
		// @Override
		// public boolean onLongClick(View v) {
		// PopupWindow popupWindow = conPopupWindow(context);
		// popupWindow.showAsDropDown(v, 0, 0);
		// return false;
		// }
		// });

		return arg1;
	}

	// 点赞点屎界面
	private CustomPopupWindow conPopupWindow(Context context) {

		popuoWindow = new CustomPopupWindow(context);
		View contentView = LayoutInflater.from(context).inflate(
				R.layout.con_comment_layout, null);
		popuoWindow.setFocusable(true);
		popuoWindow.setContentView(contentView);
		popuoWindow.setWidth(LayoutParams.WRAP_CONTENT);
		popuoWindow.setHeight(LayoutParams.WRAP_CONTENT);
		popuoWindow.setBackgroundDrawable(new ColorDrawable(context
				.getResources().getColor(android.R.color.transparent)));

		ibtn_like = (ImageView) contentView.findViewById(R.id.ibtn_like);
		ibtn_shit = (ImageView) contentView.findViewById(R.id.ibtn_shit);

		ibtn_like.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 点赞
				MsgEntity msgEnitiy = chat_list.get(mSelectedPosition);
				ChatActivity.addUserAgree(msgEnitiy.getUserJID());
				if (popuoWindow.isShowing()) {
					popuoWindow.dismiss();
				}
			}
		});
		ibtn_shit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 点屎
				MsgEntity msgEnitiy = chat_list.get(mSelectedPosition);
				ChatActivity.addUserShit(msgEnitiy.getUserJID());
				if (popuoWindow.isShowing()) {

					popuoWindow.dismiss();
				}
			}
		});
		popuoWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
//				if (msgEnitiy.getIsLeft()) {
//					viewHolder.contentLayout
//							.setBackgroundResource(R.drawable.chatfrom_bg_normal);
//				} else {
//					viewHolder.contentLayout
//							.setBackgroundResource(R.drawable.chatto_bg_normal);
//				}
			}

		});

		return popuoWindow;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		if (chat_list.get(position).getIsLeft()) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * 共两种类型
	 */
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	// 找到字符串里的表情
	// public SpannableString findFace(String content){
	// SpannableString spString = new SpannableString(content);
	// Pattern facePattern = Pattern.compile("Face:f" + "\\w{3}");
	// Matcher faceMatcher = facePattern.matcher(content);
	// while (faceMatcher.find())
	// {
	// String idString = faceMatcher.group();
	// idString = idString.substring(idString.indexOf(":") + 2,
	// idString.length());
	// face = null;
	// switch (Integer.parseInt(idString)
	// / expressionImages1.length)
	// {
	// case 0:
	// face = BitmapFactory.decodeResource(context.getResources(),
	// expressionImages[Integer.parseInt(idString)
	// % expressionImages.length]);
	// break;
	// case 1:
	// face = BitmapFactory.decodeResource(context.getResources(),
	// expressionImages1[Integer.parseInt(idString)
	// % expressionImages1.length]);
	// break;
	// case 2:
	// face = BitmapFactory.decodeResource(context.getResources(),
	// expressionImages2[Integer.parseInt(idString)
	// % expressionImages2.length]);
	// break;
	// }
	//
	// Drawable drawable = new BitmapDrawable(context.getResources(), face);
	// drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
	// drawable.getIntrinsicHeight());
	// ImageSpan faceSpan = new ImageSpan(context, face,
	// ImageSpan.ALIGN_BASELINE);
	//
	//
	// spString.setSpan(faceSpan, faceMatcher.start(), faceMatcher.end(),
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	//
	// }
	// return spString;
	// }

	public Map<String, VCard> getVcardList() {
		return vcardList;
	}

	/**
	 * set new position
	 * 
	 * @param selectedPosition
	 */
	public void setSelectedPosition(int selectedPosition) {
		mSelectedPosition = selectedPosition;

	}

}
