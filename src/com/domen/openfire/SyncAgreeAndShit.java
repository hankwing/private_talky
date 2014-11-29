package com.domen.openfire;

import java.util.ArrayList;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

import android.util.Log;

/**
 * 同步用户的点赞和点屎数据IQ包
 * @author hankwing
 *
 */
public class SyncAgreeAndShit extends IQ{
	
	private ArrayList<String> userList;
	private int topicID;
	private String roomJID;
	private Map<String, Integer> userAgreeCache;
	private Map<String, Integer> userShitCache;
	
	public SyncAgreeAndShit( int topicID, String roomJID, ArrayList<String> userList,
			Map<String, Integer> userAgree, Map<String, Integer> userShit) {
		this.userList = userList;
		this.topicID = topicID;
		this.roomJID = roomJID;
		userAgreeCache = userAgree;
		userShitCache = userShit;
	}
	
	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		
		StringBuffer agreeAndShitJson = new StringBuffer();				//建立一个字符串创建对象
		agreeAndShitJson.append("[");
		for( int i = 0; i < userList.size(); i++ ) {
			String userJID = userList.get(i);
			if( userAgreeCache.get(userJID) != 0 || 
					userShitCache.get(userJID) != 0) {
				//该用户被点赞或者点屎了 需要发送同步包
				agreeAndShitJson.append("{").
				append("\"userJID\":\"" + userJID + "\",").
				append("\"agree\":" + userAgreeCache.get(userJID) + ",").
				append("\"shit\":" + userShitCache.get(userJID) + "},");
			}
			
		}
		//善后工作
		agreeAndShitJson.deleteCharAt(agreeAndShitJson.length() - 1);
		agreeAndShitJson.append("]");
		//System.out.println("roomlistJSON: " + roomListJson);
        StringBuilder sb = new StringBuilder();      
        sb.append("<").append(getElementName()).append(" xmlns=\"").
        append(getNamespace()).append("\">").
        append("<topicID>" + topicID + "</topicID>").
        append("<roomJID>" + roomJID + "</roomJID>").
        append("<agreeAndShitJson>" + agreeAndShitJson.toString() +
        		"</agreeAndShitJson>").
        append("</").
        append(getElementName()).append(">");     
        Log.i("message", sb.toString());
        return sb.toString();
	}
	
	private String body;      
    public String getElementName() {      
        return "syncAgreeAndShit";    
    }      
      
    public String getNamespace() {      
        return "com:talky:syncAgreeAndShit";      
    }
      
    public void setBody(String body) {      
        this.body = body;      
    }      
      
    public String getBody() {      
        return body;      
    }      

}