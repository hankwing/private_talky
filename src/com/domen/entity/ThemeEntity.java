package com.domen.entity;

import android.graphics.drawable.Drawable;

/**
 * 话题实体类
 * @author hankwing
 *
 */
public class ThemeEntity {
	private String name;
	private String introduce;
	private Drawable picture;
	private String temp;
	private String id;
	public ThemeEntity(String id, String name,String introduce,Drawable picture) {
		// TODO Auto-generated constructor stub
		this.id = id;
		this.name = name;
		this.introduce = introduce;
		this.picture = picture;
		
	}
	public ThemeEntity(){
		this.id = null;
		this.introduce = null;
		this.name = null;
		this.picture = null;
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
	 * @return the introduce
	 */
	public String getIntroduce() {
		return introduce;
	}
	/**
	 * @param introduce the introduce to set
	 */
	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}
	/**
	 * @return the picture
	 */
	public Drawable getPicture() {
		return picture;
	}
	/**
	 * @param picture the picture to set
	 */
	public void setPicture(Drawable picture) {
		this.picture = picture;
	}
	/**
	 * @return the temp
	 */
	public String getTemp() {
		return temp;
	}
	/**
	 * @param temp the temp to set
	 */
	public void setTemp(String temp) {
		this.temp = temp;
	}
	
	public String getID() {
		return id;
	}
	
	public void setID( String id) {
		this.id = id;
	}
	
}
