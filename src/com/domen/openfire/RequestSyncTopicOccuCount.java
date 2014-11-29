package com.domen.openfire;

import org.jivesoftware.smack.packet.IQ;

/**
 * request sync user info IQ packet
 * @author hankwing
 *
 */
public class RequestSyncTopicOccuCount extends IQ{
	
	int position;
	
	public RequestSyncTopicOccuCount( int position) {
		this.position = position;
	}
	
	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		
        StringBuilder sb = new StringBuilder();      
        sb.append("<").append(getElementName()).append(" xmlns=\"").
        append(getNamespace()).append("\">").
        append("<position>" + position + "</position>").
        append("</").
        append(getElementName()).append(">");     
        //Log.i("message", sb.toString());
        return sb.toString();
	}
	
	private String body;      
    public String getElementName() {      
        return "requestTopicOccuCount";    
    }      
      
    public String getNamespace() {      
        return "com:talky:requestTopicOccuCount";      
    }
      
    public void setBody(String body) {      
        this.body = body;      
    }      
      
    public String getBody() {      
        return body;      
    }      

}
