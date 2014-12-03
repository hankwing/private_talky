package com.domen.tools;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.util.Log;

public class MUserChatManager {
	private static MultiUserChat mChat;
	
	public static void joinRoom( String roomJID ) {
		
	}
	
	public static MultiUserChat getChat( ) {
		return mChat;
	}
	
	public static void setChat( MultiUserChat chat) {
		mChat = chat;
	}
}
