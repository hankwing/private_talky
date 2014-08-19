package com.domen.openfire;

import org.jivesoftware.smack.packet.IQ;


public class RequestSync extends IQ{

	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		
        StringBuilder sb = new StringBuilder();      
        sb.append("<").append(getElementName()).append(" xmlns=\"").
        append(getNamespace()).append("\">").
        append("</").
        append(getElementName()).append(">");      
        return sb.toString();         
	}
	
	private String body;      
    public String getElementName() {      
        return "syncTopics";    
    }      
      
    public String getNamespace() {      
        return "com:talky:syncTopics";      
    }
      
    public void setBody(String body) {      
        this.body = body;      
    }      
      
    public String getBody() {      
        return body;      
    }      

}
