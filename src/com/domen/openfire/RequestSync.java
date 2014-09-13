package com.domen.openfire;

import org.jivesoftware.smack.packet.IQ;

/**
 * requestType -1:请求最新数据 非-1:当前话题列表最后一条话题id
 * @author hankwing
 *
 */
public class RequestSync extends IQ{

	public static final String TOPICTYPE = "topicType";
	public static final String REQUESTTYPE = "requestType";
	public static final String POSITION = "position";
	private String topicType;
	private int requestType;
	private int position;
	
	/**
	 * 构造函数 请求话题同步时应该带上的信息
	 * @param topicType
	 * @param requestType
	 * @param position	适配器编号 方便话题返回时界面的刷新
	 */
	public RequestSync( String topicType, int requestType, int position) {
		this.topicType = topicType;
		this.requestType = requestType;
		this.position = position;
	}
	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		
        StringBuilder sb = new StringBuilder();      
        sb.append("<").append(getElementName()).append(" xmlns=\"").
        append(getNamespace()).append("\">").
        append("<requestType>" + requestType +
        		"</requestType>").
        		append("<type>"+ topicType + "</type>").
        		append("<position>" + position + "</position>").
        append("</").
        append(getElementName()).append(">");     
        //Log.i("message", sb.toString());
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
