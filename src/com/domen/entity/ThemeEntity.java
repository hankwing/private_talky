package com.domen.entity;


/**
 * 话题实体类
 * @author hankwing
 *
 */
public class ThemeEntity {
	private String name;
	private String bitmapUrl;
	private String temp;
	private String id;
	public ThemeEntity(String id, String name,String bitmapUrl) {
		// TODO Auto-generated constructor stub
		this.id = id;
		this.name = name;
		this.bitmapUrl = bitmapUrl;
		
	}
	public ThemeEntity(){
		this.id = null;
		this.name = null;
		this.bitmapUrl = null;
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
	 * @return the picture
	 */
	public String getBitmapUrl() {
		return bitmapUrl;
	}
	/**
	 * @param picture the picture to set
	 */
	public void setPicture(String bitmapUrl) {
		this.bitmapUrl = bitmapUrl;
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
