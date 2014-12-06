package com.domen.tools;

import java.io.Serializable;
import java.util.List;

public class MUserChatManager implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private List<String> occupantsData;
	
	public static void joinRoom( String roomJID ) {
		
	}
	
	public List<String> getListData( ) {
		return occupantsData;
	}
	
	public void setData( List<String> data) {
		occupantsData = data;
	}
}
