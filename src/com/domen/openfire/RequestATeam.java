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
	private String isJoin = "1";				//代表用户是否请求组队 如果否 则在相应队列中删除该用户
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
        append("<isjoin>" + isJoin +
                "</isjoin>").
        append("</").
        append(getElementName()).append(">");      
        return sb.toString();         
	}
	
	public RequestATeam(String userJID, String topicID, String isPositive,String isJoin) {
		this.username = userJID;
		this.topicID = topicID;
		this.isPositive = isPositive;
		this.isJoin = isJoin;
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
    
    public void setIsJoin( String isJoin) {
    	this.isJoin = isJoin;
    }

}
