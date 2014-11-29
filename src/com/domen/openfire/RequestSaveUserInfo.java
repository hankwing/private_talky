package com.domen.openfire;

import org.jivesoftware.smack.packet.IQ;

/**
 * 用户资料包
 * @author hankwing
 *
 */
public class RequestSaveUserInfo extends IQ{

	private String account;
	private String userJID;
	private int rank;
	private String subtitle;				//称号
	private int favour;
	private int shit;
	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();      
        sb.append("<").append(getElementName()).append(" xmlns=\"").
        append(getNamespace()).append("\">").
        append("<account>" + account +
        		"</account>").
        append("<userJID>" + userJID + "</userJID>").
        append("<rank>"+ rank + "</rank>").
        append("<subtitle>" + subtitle +
        		"</subtitle>") .
        append("<favour>" + favour +
        		"</favour>") .
        append("<shit>" + shit +
                "</shit>") .
        append("</").
        append(getElementName()).append(">");    
		
        return sb.toString();         
	}
	
	public RequestSaveUserInfo(String account,String userJID, int rank, String subtitle,int favour,int shit ) {
		this.account = account;
		this.userJID = userJID;
		this.rank = rank;
		this.subtitle = subtitle;
		this.favour = favour;
		this.shit = shit;
	}
	      
    public String getElementName() {      
        return "saveUserInfo";    
    }      
      
    public String getNamespace() {      
        return "com:talky:saveUserInfo";      
    }

}
