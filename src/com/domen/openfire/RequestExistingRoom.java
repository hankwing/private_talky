package com.domen.openfire;

import org.jivesoftware.smack.packet.IQ;

/**
 * 请求话题已有的房间JID
 * @author hankwing
 *
 */
public class RequestExistingRoom extends IQ{

	private String id;
	public RequestExistingRoom( String id ) {
		this.id = id;
	}
	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		
        StringBuilder sb = new StringBuilder();      
        sb.append("<").append(getElementName()).append(" xmlns=\"").
        append(getNamespace()).append("\">").
        append("<topicID>" + id +
        		"</topicID>").
        append("</").
        append(getElementName()).append(">");     
        //Log.i("message", sb.toString());
        return sb.toString();
	}
	
	private String body;      
    public String getElementName() {      
        return "requestExistingRoom";    
    }      
      
    public String getNamespace() {      
        return "com:talky:requestExistingRoom";      
    }
      
    public void setBody(String body) {      
        this.body = body;      
    }      
      
    public String getBody() {      
        return body;      
    }      

}