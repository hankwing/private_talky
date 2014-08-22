package com.domen.openfire;

import org.jivesoftware.smack.packet.IQ;

/**
 * 请求组队IQ包
 * @author hankwing
 *
 */
public class RequestATeam extends IQ{

	private String username;
	private String topicID;
	private String isPositive;
	private String body;
	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();      
        sb.append("<").append(getElementName()).append(" xmlns=\"").
        append(getNamespace()).append("\">").
        append("<topicID>" + topicID +
        		"</topicID>").append("<jid>"+ username + "</jid>").
        append("<ispositive>" + isPositive +
        		"</ispositive>").
        append("</").
        append(getElementName()).append(">");      
        return sb.toString();         
	}
	
	public RequestATeam(String userJID, String topicID, String isPositive) {
		this.username = userJID;
		this.topicID = topicID;
		this.isPositive = isPositive;
	}
	      
    public String getElementName() {      
        return "requestATeam";    
    }      
      
    public String getNamespace() {      
        return "com:talky:requestATeam";      
    }
      
    public void setBody(String body) {      
        this.body = body;      
    }      
      
    public String getBody() {      
        return body;      
    }      

}
