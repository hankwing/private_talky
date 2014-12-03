package com.domen.openfire;

import org.jivesoftware.smack.packet.IQ;

/**
 * get occupants list
 * @author hankwing
 *
 */
public class RequestOccupantsList extends IQ{

	private String id;
	private String roomJID;
	
	public RequestOccupantsList( String id , String roomJID) {
		this.id = id;
		this.roomJID = roomJID;
	}
	
	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		
        StringBuilder sb = new StringBuilder();      
        sb.append("<").append(getElementName()).append(" xmlns=\"").
        append(getNamespace()).append("\">").
        append("<topicID>" + id +
        		"</topicID>").
        append("<roomJID>" + roomJID +
                		"</roomJID>").
        append("</").
        append(getElementName()).append(">");     
        //Log.i("message", sb.toString());
        return sb.toString();
	}
	
	private String body;      
    public String getElementName() {      
        return "requestOccupantsList";    
    }      
      
    public String getNamespace() {      
        return "com:talky:requestOccupantsList";      
    }
      
    public void setBody(String body) {      
        this.body = body;      
    }      
      
    public String getBody() {      
        return body;      
    }      

}