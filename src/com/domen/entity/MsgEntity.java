package com.domen.entity;

import org.jivesoftware.smack.packet.Message;

import android.graphics.drawable.Drawable;

/**
 * message实体类
 * @author hankwing
 *
 */
public class MsgEntity {
	
	private Message message;
	private String content;
	private String userJID;				//用户的JID
	private String date;
	private Boolean isLeft;
	private Boolean isText;
	private String name;
	private Drawable head;
	private Drawable image;				//聊天图片
	private String messageID;			//control progressbar and message confrim

	public MsgEntity(){
		content = null;
		date =null;
		name =null;
		isLeft=false;
		isText=true;
	}

	public MsgEntity(String content,String date,String name,Boolean isLeft, String messageID){
		this.content = content;
		this.date = date;
		this.name = name;
		this.isLeft = isLeft;
		this.messageID = messageID;
		
	}

	public MsgEntity(String content,String date,String name,Drawable head,Boolean isLeft){
		this.content = content;
		this.date = date;
		this.name = name;
		this.head = head;
		this.isText = true;
		this.isLeft = isLeft;
	}
	
	public MsgEntity(String content,String date,String name,Drawable head,Boolean isLeft,Boolean isText){
		this.content = content;
		this.date = date;
		this.name = name;
		this.head = head;
		this.isText = isText;
		this.isLeft = isLeft;
	}
	
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}
	/**
	 * @return the isPositive
	 */
	public Boolean getIsLeft() {
		return isLeft;
	}
	/**
	 * @param isPositive the isPositive to set
	 */
	public void setIsLeft(Boolean isLeft) {
		this.isLeft = isLeft;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the head
	 */
	public Drawable getHead() {
		return head;
	}

	/**
	 * @param head the head to set
	 */
	public void setHead(Drawable head) {
		this.head = head;
	}

	/**
	 * @return the image
	 */
	public Drawable getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(Drawable image) {
		this.image = image;
	}

	/**
	 * @return the isText
	 */
	public Boolean getIsText() {
		return isText;
	}

	/**
	 * @param isText the isText to set
	 */
	public void setIsText(Boolean isText) {
		this.isText = isText;
	}
	
	public void setUserJID( String userJID) {
		this.userJID = userJID;
	}
	
	public String getUserJID() {
		return userJID;
	}
	
	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}
	
	public String getMessageId() {
		return messageID;
	}
	
	public void setMessage( Message message ) {
		this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
	
	
}
