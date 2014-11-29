package com.domen.openfire;

import org.jivesoftware.smack.packet.IQ;

/**
 * 请求话题详情IQ
 * @author hankwing
 *
 */
public class RequestTopicInfo extends IQ{

	private String id;
	public RequestTopicInfo( String id ) {
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
        return "requestTopicInfo";    
    }      
      
    public String getNamespace() {      
        return "com:talky:requestTopicInfo";      
    }
      
    public void setBody(String body) {      
        this.body = body;      
    }      
      
    public String getBody() {      
        return body;      
    }      

}