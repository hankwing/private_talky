package com.domen.openfire;

import org.jivesoftware.smack.packet.IQ;

/**
 * request sync user info IQ packet
 * @author hankwing
 *
 */
public class RequestSyncUserInfo extends IQ{

	public static final String TOPICTYPE = "topicType";
	public static final String REQUESTTYPE = "requestType";
	public static final String POSITION = "position";
	
	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		
        StringBuilder sb = new StringBuilder();      
        sb.append("<").append(getElementName()).append(" xmlns=\"").
        append(getNamespace()).append("\">").
        append("</").
        append(getElementName()).append(">");     
        //Log.i("message", sb.toString());
        return sb.toString();
	}
	
	private String body;      
    public String getElementName() {      
        return "syncUserInfo";    
    }      
      
    public String getNamespace() {      
        return "com:talky:syncUserInfo";      
    }
      
    public void setBody(String body) {      
        this.body = body;      
    }      
      
    public String getBody() {      
        return body;      
    }      

}
