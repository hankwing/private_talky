package com.domen.entity;

import android.graphics.drawable.Drawable;

public class MsgEntity {
	String content;
	String date;
	//�ж����������Ƿ���
	Boolean isPositive;
	//�ж��Ƿ����ı�����ͼƬ
	Boolean isText;
	String name;
	Drawable head;
	Drawable image;

	public MsgEntity(){
		content = null;
		date =null;
		name =null;
		isPositive=false;
		isText=true;
	}

	public MsgEntity(String content,String date,String name,Boolean isPositive){
		this.content = content;
		this.date = date;
		this.name = name;
		this.isPositive = isPositive;
		
	}

	public MsgEntity(String content,String date,String name,Drawable head,Boolean isPositive){
		this.content = content;
		this.date = date;
		this.name = name;
		this.head = head;
		this.isText = true;
		isPositive = false;
	}
	
	public MsgEntity(String content,String date,String name,Drawable head,Boolean isPositive,Boolean isText){
		this.content = content;
		this.date = date;
		this.name = name;
		this.head = head;
		this.isText = isText;
		isPositive = false;
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
	public Boolean getIsPositive() {
		return isPositive;
	}
	/**
	 * @param isPositive the isPositive to set
	 */
	public void setIsPositive(Boolean isPositive) {
		this.isPositive = isPositive;
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

	
	
}
